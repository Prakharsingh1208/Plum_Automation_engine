package com.plumauto.entity;

import com.mongodb.lang.NonNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class UserDetails {

    @Id
    private ObjectId userId;

    @Indexed(unique = true)
    @NonNull
    private String username;

    @Indexed(unique = true)
    @NonNull
    private String email;

    @NonNull
    private String password;

    @NonNull
    private String firstName;

    @NonNull
    private String roles = "member";

    @NonNull
    private boolean active = true;


    private String OrganisationName;

}
