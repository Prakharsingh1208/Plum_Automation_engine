package com.plumauto.service;

import com.plumauto.entity.JobDetail;
import com.plumauto.repository.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DockerService {
    private final Path rootPath;
    public DockerService(@Value("${app.auto-engine.path}")String rootPath) {
        this.rootPath = Paths.get(rootPath);

    }

    public static Map<String, String> runningTasks = new java.util.concurrent.ConcurrentHashMap<>();



    @Autowired
    Job jobRepository;
    public void abortBuild(String jobName, String buildId) throws IOException, InterruptedException {
        JobDetail job = jobRepository.findByJobName(jobName);
        String containerName = runningTasks.get(job.getJobConfig().getAllowedOrganization()+"-"+jobName+"-"+buildId);
        if(containerName==null){
            log.error("No running container found for job: " + jobName + " Build Number: " + buildId);
            throw new RuntimeException("No running container found for job: " + jobName + " Build Number: " + buildId);
        }
        String dockerKillCommand = String.format("docker kill %s",containerName);
        ProcessBuilder ps = new ProcessBuilder("sh", "-c", dockerKillCommand);
        ps.inheritIO();
        int status = ps.start().waitFor();
        if (status != 0) {
            log.error("Failed to abort job: " + jobName + " Build Number: " + buildId);
            throw new RuntimeException("Failed to abort build: " + jobName + " Build Number: " + buildId);
        }else {
            log.info("Successfully aborted job: " + jobName + " Build Number: " + buildId);
            runningTasks.remove(job.getJobConfig().getAllowedOrganization()+"-"+jobName+"-"+buildId);
            job.getBuildNumber().get(Integer.parseInt(buildId)).setStatus("aborted");
            job.getBuildNumber().get(Integer.parseInt(buildId)).setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    @Async
    public void runCommand(JobDetail jobDetail, String buildNumber) throws IOException, InterruptedException {
        Path dirPath = Paths.get(rootPath.toAbsolutePath().toString() + "/" + jobDetail.getJobName() + "/" + buildNumber);
        Files.createDirectories(dirPath);

        Path path = rootPath.resolve(jobDetail.getJobName());
        Path logPath = path.resolve(buildNumber+"/build.logs");
        StringBuilder memoryLog = new StringBuilder();


        Path runPath = path.resolve(buildNumber+"/"+"run.sh");
        String buildSH = "#!/bin/sh\n" + jobDetail.getBuildStep();
        Files.writeString(runPath, buildSH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Set<PosixFilePermission> ownerFull = PosixFilePermissions.fromString("rwxr-xr-x");
        Files.setPosixFilePermissions(runPath, ownerFull);

        Process process = getProcess(jobDetail, buildNumber);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() ->{
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter writer = Files.newBufferedWriter(logPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Write to Memory (for immediate use)
                    memoryLog.append(line).append("\n");

                    // Write to File (for persistence)
                    writer.write(line);
                    writer.newLine();

                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                executor.shutdown();
            }

        });

        boolean finished = process.waitFor(Long.parseLong(jobDetail.getJobConfig().getTimeLimit()), TimeUnit.MINUTES);

        if(!finished){
            process.destroyForcibly();
            abortBuild(jobDetail.getJobName(), buildNumber);
            memoryLog.append("Command timed out and was terminated.\n");
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setStatus("failed");
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setCompletedAt(LocalDateTime.now());
            jobRepository.save(jobDetail);
            return;
        }
        int exitCode = process.exitValue();
        if(exitCode!=0){
            memoryLog.append("Command failed with exit code: ").append(exitCode).append("\n");
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setStatus("failed");
        }else{
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setStatus("Completed");
        }
        jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setCompletedAt(LocalDateTime.now());
        jobRepository.save(jobDetail);
        runningTasks.remove(jobDetail.getJobConfig().getAllowedOrganization()+"-"+jobDetail.getJobName()+"-"+buildNumber);
        memoryLog.append("\nProcess finished with exit code: ").append(exitCode);
        memoryLog.toString();

    }

    private Process getProcess(JobDetail jobDetail, String buildNumber) throws IOException {
        String containerName = UUID.randomUUID().toString();
        String dockerCmd = String.format(
                "docker run --rm -v %s:/src -w /src --memory=%s --cpus=%s --name %s alpine:latest sh -c \"%s\"",
                rootPath.toAbsolutePath().toString() + "/" + jobDetail.getJobName() + "/" + buildNumber,
                jobDetail.getJobConfig().getMemory()+"m",
                jobDetail.getJobConfig().getCpu(),
                containerName,
                "./run.sh"
        );

        log.info("Executing command: " + dockerCmd);
        ProcessBuilder ps = new ProcessBuilder("sh","-c",dockerCmd);
        ps.directory(rootPath.toFile());
        ps.redirectErrorStream(true);
        runningTasks.put(jobDetail.getJobConfig().getAllowedOrganization()+"-"+jobDetail.getJobName()+"-"+buildNumber,containerName);
        return ps.start();
    }
}
