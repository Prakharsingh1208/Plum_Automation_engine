package com.plumauto.entity;

import lombok.Data;


@Data
public class JobConfig {
    private String memory;
    private String cpu;
    private String timeLimit="15";
    private String allowedOrganization;
    private String allowedUsers;
}
