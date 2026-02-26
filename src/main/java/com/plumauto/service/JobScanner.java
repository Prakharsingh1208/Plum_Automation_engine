package com.plumauto.service;

import com.plumauto.entity.RunDetails;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.*;

@Data
@Component
@Slf4j
public class JobScanner implements CommandLineRunner {

    @Autowired
    DockerService dockerService;

    private BlockingQueue<RunDetails> pendingTaskQueue = new LinkedBlockingQueue<>();


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Job Scanner started in background...");
        while (!Thread.currentThread().isInterrupted()){
            RunDetails currentJob = pendingTaskQueue.take();
            log.info("Starting job: " + currentJob.getJobDetail().getJobName() + " Build Number: " + currentJob.getBuildNumber());
            dockerService.runCommand(currentJob.getJobDetail(),currentJob.getBuildNumber());
        }

    }

}




