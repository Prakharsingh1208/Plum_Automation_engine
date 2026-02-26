package com.plumauto.entity;

import com.mongodb.lang.NonNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "organizations")
public class OrganizationDetails {
    @Id
    private ObjectId orgId;

    @Indexed(unique = true)
    @NonNull
    private String orgName;

    @DBRef
    private List<UserDetails> members = new ArrayList<>();

    @NonNull
    private String passKey;

}
