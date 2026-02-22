package com.plumauto.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "buildDetails")
@Data
public class BuildDetails {
    private String buildNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String status;
}
