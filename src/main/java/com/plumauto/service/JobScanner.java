package com.plumauto.service;

import com.plumauto.entity.RunDetails;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Component
public class JobScanner implements CommandLineRunner {
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
                    automationEngine.runCommand(currentJob.getJobDetail(),currentJob.getBuildNumber());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Thread.sleep(1000);
        }
    }
}
