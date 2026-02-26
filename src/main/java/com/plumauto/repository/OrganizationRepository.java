package com.plumauto.repository;

import com.plumauto.entity.OrganizationDetails;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<OrganizationDetails, ObjectId> {
    public OrganizationDetails findByOrgName(String orgName);
}
