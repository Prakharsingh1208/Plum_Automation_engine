package com.plumauto.entity;

import lombok.Data;

@Data
public class RunDetails {
    private String buildNumber;
    private JobDetail jobDetail;
    private long pid;
}
