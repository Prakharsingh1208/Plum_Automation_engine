package com.plumauto.repository;

import com.plumauto.entity.JobDetail;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Job extends MongoRepository<JobDetail, ObjectId> {
    JobDetail findByJobName(String JobName);
}
