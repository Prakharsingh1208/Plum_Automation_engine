package com.plumauto.entity;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "automationDetails")
@Data
public class JobDetail {

    @Id
    private ObjectId jobId;

    @Indexed(unique = true)
    private String jobName;

    private String description;
    private String buildStep;

    private List<BuildDetails> buildNumber;

    public BuildDetails findBuildDetailsByBuildNumber(String buildNumber) {
        if (this.buildNumber != null) {
            for (BuildDetails details : this.buildNumber) {
                if (details.getBuildNumber().equals(buildNumber)) {
                    return details;
                }
            }
        }
        return null; // Return null if not found
    }
}
