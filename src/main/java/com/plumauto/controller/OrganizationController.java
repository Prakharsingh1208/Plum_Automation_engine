package com.plumauto.controller;

import com.plumauto.entity.OrganizationDetails;
import com.plumauto.service.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/organization")
public class OrganizationController {
    @Autowired
    Organization organization;

    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationDetails organizationDetails) throws IOException {
        return organization.createOrganization(organizationDetails);
    }

    @GetMapping
    public ResponseEntity<OrganizationDetails> getOrganization(@RequestParam String orgName){
        return organization.getOrganization(orgName);
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addUserToOrganization(@RequestBody OrganizationDetails organizationDetails){
        return organization.addUserInOrg(organizationDetails);
    }
}
