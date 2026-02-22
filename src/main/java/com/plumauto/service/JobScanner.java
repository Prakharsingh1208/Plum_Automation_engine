package com.plumauto.service;

import com.plumauto.entity.JobDetail;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Component
public class JobScanner implements CommandLineRunner {
    AutomationEngine automationEngine = new AutomationEngine("/Users/marrow/Desktop");
    private Queue<JobDetail> taskQueue = new ConcurrentLinkedQueue<>();


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Job Scanner started in background...");
        while (true){
            JobDetail currentJob = taskQueue.poll();
            if(currentJob!=null){
                try {
                    automationEngine.runCommand(currentJob);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Thread.sleep(1000);
        }
    }
}
