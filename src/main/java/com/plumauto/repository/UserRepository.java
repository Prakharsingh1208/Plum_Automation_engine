package com.plumauto.repository;

import com.plumauto.entity.UserDetails;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<UserDetails, ObjectId> {
    public UserDetails findByUsername(String username);
    public List<UserDetails> findByUserType(String userType);
}
