package com.plumauto.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class JobConfig {
    private String memory;
    private String cpu;
    private String timeLimit="15";
    private String dockerImage = "alpine:latest";
    private String allowedOrganization;
    private List<String> allowedUsers = new ArrayList<>();
}
