package com.plumauto.service;

import com.plumauto.entity.BuildDetails;
import com.plumauto.entity.JobDetail;
import com.plumauto.entity.RunDetails;
import com.plumauto.entity.UserDetails;
import com.plumauto.repository.Job;
import com.plumauto.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    UserRepository userRepository;

    private final Path rootPath;
    public AutomationEngine(@Value("${app.auto-engine.path}") String rootPath) {
        this.rootPath = Paths.get(rootPath);

    }

    public boolean isJobAccessAvailable(JobDetail jobDetail){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        String user = auth.getName();
        UserDetails userDetails = userRepository.findByUsername(user);
        return jobDetail.getJobConfig().getAllowedUsers().contains(user) &&
                userDetails.getOrganisationName() != null &&
                jobDetail.getJobConfig().getAllowedOrganization().equals(userDetails.getOrganisationName());
    }

    public boolean createJob(JobDetail jobDetail) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        String user = auth.getName();
        UserDetails userDetails = userRepository.findByUsername(user);

        if (userRepository.findByUsername(user).getOrganisationName() == null) {
            throw new Exception("No Org joined");
        }

        List<String> validateUsername = List.of(jobDetail.getJobName().split(" "));
        if (validateUsername.size() > 1) {
            throw new RuntimeException("Invalid Job Name: Job name should not contain spaces");
        }

        if (jobRepository.findByJobName(jobDetail.getJobName()) != null) {
            log.error("Job already exists with name: " + jobDetail.getJobName());
            throw new RuntimeException("Job already exists with name: " + jobDetail.getJobName());
        }

        Files.createDirectories(Path.of(rootPath + "/" + userDetails.getOrganisationName() +"/"+jobDetail.getJobName()));

        List<BuildDetails> tempBuildInfo = new ArrayList<>();
        BuildDetails buildInfo = new BuildDetails();
        buildInfo.setBuildNumber("0");
        buildInfo.setCompletedAt(LocalDateTime.now());
        buildInfo.setCreatedAt(LocalDateTime.now());
        buildInfo.setStatus("completed");
        tempBuildInfo.add(buildInfo);

        if(jobDetail.getJobConfig().getAllowedUsers()==null){
            jobDetail.getJobConfig().setAllowedUsers(new ArrayList<>());
        }
        jobDetail.setBuildNumber(tempBuildInfo);
        jobDetail.getJobConfig().getAllowedUsers().add(userDetails.getUsername());
        jobDetail.getJobConfig().setAllowedOrganization(userDetails.getOrganisationName());
        jobRepository.save(jobDetail);
        return true;
    }


    public boolean deleteJob(String jobName) throws Exception {
        JobDetail job = jobRepository.findByJobName(jobName);
        if (job == null) {
            throw new RuntimeException("Job not found: " + jobName);
        } else {
            if(!isJobAccessAvailable(job)){
                throw new Exception("You do not have access to this job");
            }
            FileUtils.deleteDirectory(new File(rootPath + "/" + job.getJobConfig().getAllowedOrganization() +"/"+job.getJobName()));
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
            File oldDirName = new File(rootPath + "/" + job.get().getJobConfig().getAllowedOrganization() +"/"+job.get().getJobConfig());
            File newDirName = new File(rootPath + "/" + jobDetail.getJobConfig().getAllowedOrganization() +"/"+jobDetail.getJobName());
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

        BuildDetails buildInfo = new BuildDetails();
        buildInfo.setBuildNumber(BuildNumber);
        buildInfo.setStatus("in-progress");
        buildInfo.setCreatedAt(LocalDateTime.now());
        job.getBuildNumber().add(buildInfo);
        jobRepository.save(job);

        RunDetails runDetails = new RunDetails();
        runDetails.setJobDetail(job);
        runDetails.setBuildNumber(BuildNumber);
        runner.getPendingTaskQueue().add(runDetails);


        return true;
    }

}

