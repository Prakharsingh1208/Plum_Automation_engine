package com.plumauto.controller;

import com.plumauto.entity.JobDetail;
import com.plumauto.repository.Job;
import com.plumauto.service.AutomationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/job")
@Slf4j
public class Runner {

    @Autowired
    AutomationEngine automationEngine;

    @PostMapping
    public boolean createJob(@RequestBody JobDetail jobDetail) throws IOException, InterruptedException {
        log.info("POST /job ");
        log.info("jobName: " + jobDetail.getJobName());
        log.info("buildStep: " + jobDetail.getBuildStep());
        log.info("description: " + jobDetail.getDescription());
        return automationEngine.createJob(jobDetail);
    }

    @GetMapping("/{jobName}")
    public boolean runJob(@PathVariable String jobName) throws IOException, InterruptedException {
        return automationEngine.runjob(jobName);
    }

    @DeleteMapping
    public boolean deleteJob(@RequestBody JobDetail job) throws IOException, InterruptedException {
        return automationEngine.deleteJob(job.getJobName());
    }

    @PutMapping
    public boolean editJob(@RequestBody JobDetail job) throws IOException, InterruptedException {
        return automationEngine.editJob(job);
    }

    @DeleteMapping("/abort/{jobName}/{buildNumber}")
    public boolean abortJob(@PathVariable String jobName,@PathVariable String buildNumber) throws IOException, InterruptedException {
        automationEngine.abortBuild(jobName,buildNumber);
        return true;
    }
}
