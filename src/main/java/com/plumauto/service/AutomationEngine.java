package com.plumauto.service;

import com.plumauto.entity.BuildDetails;
import com.plumauto.entity.JobDetail;
import com.plumauto.entity.RunDetails;
import com.plumauto.repository.Job;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class     AutomationEngine {
    @Autowired
    Job jobRepository;

    @Autowired
    JobScanner runner;

    private final Path rootPath;
    public AutomationEngine(@Value("${app.auto-engine.path}") String rootPath) {
        this.rootPath = Paths.get(rootPath);

    }

    public boolean createJob(JobDetail jobDetail) throws IOException, InterruptedException {

        List<String> validateUsername = List.of(jobDetail.getJobName().split(" "));
        if (validateUsername.size() > 1) {
            throw new RuntimeException("Invalid Job Name: Job name should not contain spaces");
        }
        if (jobRepository.findByJobName(jobDetail.getJobName()) != null) {
            log.error("Job already exists with name: " + jobDetail.getJobName());
            throw new RuntimeException("Job already exists with name: " + jobDetail.getJobName());
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
        if (job == null) {
            throw new RuntimeException("Job not found: " + jobName);
        } else {
            FileUtils.deleteDirectory(new File(rootPath + "/" + job.getJobName()));
            jobRepository.delete(job);
            return true;
        }
    }


    public boolean editJob(JobDetail jobDetail) throws IOException, InterruptedException {
        Optional<JobDetail> job = jobRepository.findById(jobDetail.getJobId());
        if (job.isEmpty()) {
            throw new RuntimeException("Job not found: " + jobDetail);
        } else if (jobDetail.getJobName().split(" ").length > 1) {
            throw new RuntimeException("Invalid Job Name: Job name should not contain spaces");
        } else {
            File oldDirName = new File(rootPath + "/" + job.get().getJobName());
            File newDirName = new File(rootPath + "/" + jobDetail.getJobName());
            if (oldDirName.renameTo(newDirName)) {
                job.get().setJobName(jobDetail.getJobName().trim());
                job.get().setDescription(jobDetail.getDescription());
                job.get().setBuildStep(jobDetail.getBuildStep());
                jobRepository.save(job.get());
                return true;
            } else {
                throw new RuntimeException("Failed to rename directory for job: " + jobDetail.getJobName());
            }
        }
    }

    public boolean runjob(String jobName) throws IOException, InterruptedException {
        JobDetail job = jobRepository.findByJobName(jobName);
        if (job == null) {
            throw new RuntimeException("Job not found: " + jobName);
        }
        String BuildNumber = String.valueOf(job.getBuildNumber().size());

        RunDetails runDetails = new RunDetails();
        runDetails.setJobDetail(job);
        runDetails.setBuildNumber(BuildNumber);
        runner.getPendingTaskQueue().add(runDetails);

        BuildDetails buildInfo = new BuildDetails();
        buildInfo.setBuildNumber(BuildNumber);
        buildInfo.setStatus("in-progress");
        buildInfo.setCreatedAt(LocalDateTime.now());
        job.getBuildNumber().add(buildInfo);
        jobRepository.save(job);
        return true;
    }

}

