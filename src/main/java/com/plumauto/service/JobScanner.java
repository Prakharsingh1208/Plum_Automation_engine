package com.plumauto.service;

import com.plumauto.entity.RunDetails;
import com.plumauto.repository.Job;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Component
@Slf4j
public class JobScanner implements CommandLineRunner {

    @Autowired
    Job jobRepository;
    private final AutomationEngine automationEngine;

    // The @Lazy annotation breaks the cycle
    public JobScanner(@Lazy AutomationEngine automationEngine) {
        this.automationEngine = automationEngine;
    }

    private Queue<RunDetails> taskQueue = new ConcurrentLinkedQueue<>();


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Job Scanner started in background...");
        while (true){
            RunDetails currentJob = taskQueue.poll();
            if(currentJob!=null){
                try {
                    log.info("Starting job: " + currentJob.getJobDetail().getJobName() + " Build Number: " + currentJob.getBuildNumber());
                    automationEngine.runCommand(currentJob.getJobDetail(),currentJob.getBuildNumber());
                } catch (Exception e) {
                    currentJob.getJobDetail().findBuildDetailsByBuildNumber(currentJob.getBuildNumber()).setStatus("failed");
                    currentJob.getJobDetail().findBuildDetailsByBuildNumber(currentJob.getBuildNumber()).setCompletedAt(LocalDateTime.now());
                    jobRepository.save(currentJob.getJobDetail());
                }
            }
            Thread.sleep(1000);
        }
    }


}
