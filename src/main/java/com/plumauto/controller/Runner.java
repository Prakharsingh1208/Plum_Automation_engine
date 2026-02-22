package com.plumauto.controller;

import com.plumauto.entity.JobDetail;
import com.plumauto.repository.Job;
import com.plumauto.service.AutomationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/run")
public class Runner {

    @Autowired
    AutomationEngine automationEngine;

    @PostMapping
    public boolean createJob(@RequestBody JobDetail jobDetail) throws IOException, InterruptedException {
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
}
