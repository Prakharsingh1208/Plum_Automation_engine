package com.plumauto.service;

import com.plumauto.entity.BuildDetails;
import com.plumauto.entity.JobDetail;
import com.plumauto.entity.RunDetails;
import com.plumauto.repository.Job;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class     AutomationEngine {
    @Autowired
    Job jobRepository;

    @Autowired
    JobScanner runner;



    private final Path rootPath;
    public AutomationEngine(@Value("${app.auto-engine.path}")String rootPath) {
        this.rootPath = Paths.get(rootPath);
    }

    public boolean createJob(JobDetail jobDetail) throws IOException, InterruptedException {

        List<String> validateUsername = List.of(jobDetail.getJobName().split(" "));
        if(validateUsername.size()>1){
            throw new RuntimeException("Invalid Job Name: Job name should not contain spaces");
        }
        Files.createDirectories(Path.of(rootPath + "/" + jobDetail.getJobName()));
        List<BuildDetails> tempBuildInfo = new ArrayList<>();
        BuildDetails buildInfo = new BuildDetails();
        buildInfo.setBuildNumber("0");
        buildInfo.setCompletedAt(LocalDateTime.now());
        buildInfo.setCreatedAt(LocalDateTime.now());
        buildInfo.setStatus("completed");
        tempBuildInfo.add(buildInfo);
        jobDetail.setBuildNumber(tempBuildInfo);
        jobRepository.save(jobDetail);
        return true;
    }


    public boolean deleteJob(String jobName) throws IOException, InterruptedException {
         JobDetail job = jobRepository.findByJobName(jobName);
         if(job==null){
             throw new RuntimeException("Job not found: " + jobName);
         }else{
             FileUtils.deleteDirectory(new File(rootPath + "/" +job.getJobName()));
             jobRepository.delete(job);
             return true;
         }
    }


    public boolean editJob(JobDetail jobDetail) throws IOException, InterruptedException {
        Optional<JobDetail> job = jobRepository.findById(jobDetail.getJobId());
        if(job.isEmpty()){
            throw new RuntimeException("Job not found: " + jobDetail);
        } else if (jobDetail.getJobName().split(" ").length>1) {
            throw new RuntimeException("Invalid Job Name: Job name should not contain spaces");
        } else {
            File oldDirName = new File(rootPath + "/" + job.get().getJobName());
            File newDirName = new File(rootPath+ "/" + jobDetail.getJobName());
            if(oldDirName.renameTo(newDirName)){
                job.get().setJobName(jobDetail.getJobName().trim());
                job.get().setDescription(jobDetail.getDescription());
                job.get().setBuildStep(jobDetail.getBuildStep());
                jobRepository.save(job.get());
                return true;
            }else {
                throw new RuntimeException("Failed to rename directory for job: " + jobDetail.getJobName());
            }
        }
    }

    public boolean runjob(String jobName) throws IOException, InterruptedException {
        JobDetail job = jobRepository.findByJobName(jobName);
        if(job==null){
             throw new RuntimeException("Job not found: " + jobName);
        }
        String BuildNumber = String.valueOf(job.getBuildNumber().size());

        RunDetails runDetails = new RunDetails();
        runDetails.setJobDetail(job);
        runDetails.setBuildNumber(BuildNumber);
        runner.getTaskQueue().add(runDetails);

        BuildDetails buildInfo = new BuildDetails();
        buildInfo.setBuildNumber(BuildNumber);
        buildInfo.setStatus("in-progress");
        buildInfo.setCreatedAt(LocalDateTime.now());
        job.getBuildNumber().add(buildInfo);
        jobRepository.save(job);

        Path dirPath = Paths.get(rootPath.toAbsolutePath().toString()+"/"+job.getJobName()+"/"+ BuildNumber);
        Files.createDirectories(dirPath);
        return true;
    }

    @Async
    public void runCommand(JobDetail jobDetail, String buildNumber) throws IOException, InterruptedException {

        Path path = rootPath.resolve(jobDetail.getJobName());
        Path logPath = path.resolve(buildNumber+"/build.logs"); //This creates a log file in the root directory of the automation engine. You can change this to a specific job directory if needed.
        StringBuilder memoryLog = new StringBuilder();


        Path runPath = path.resolve(buildNumber+"/"+"run.sh");
        String buildSH = "#!/bin/sh\n" + jobDetail.getBuildStep();
        Files.writeString(runPath, buildSH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        //Set executable permissions for the run.sh file
        Set<PosixFilePermission> ownerFull = PosixFilePermissions.fromString("rwxr-xr-x");
        Files.setPosixFilePermissions(runPath, ownerFull);

        String dockerCmd = String.format(
                "docker run --rm -v %s:/src -w /src alpine:latest sh -c \"%s\"",
                rootPath.toAbsolutePath().toString() + "/" + jobDetail.getJobName() + "/" + buildNumber,
                "./run.sh"
        );


        ProcessBuilder ps = new ProcessBuilder("sh","-c",dockerCmd);
        ps.directory(rootPath.toFile());
        ps.redirectErrorStream(true);

        Process process = ps.start();
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
        }
        int exitCode = process.waitFor();
        if(exitCode!=0){
            memoryLog.append("Command failed with exit code: ").append(exitCode).append("\n");
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setStatus("failed");
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setCompletedAt(LocalDateTime.now());
            jobRepository.save(jobDetail);
        }else{
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setStatus("Completed");
            jobDetail.getBuildNumber().get(Integer.parseInt(buildNumber)).setCompletedAt(LocalDateTime.now());
            jobRepository.save(jobDetail);
        }
        memoryLog.append("\nProcess finished with exit code: ").append(exitCode);
        memoryLog.toString();
    }
}
