package com.plumauto.controller;

import com.plumauto.entity.OrganizationDetails;
import com.plumauto.service.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization")
public class OrganizationController {
    @Autowired
    Organization organization;

    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationDetails organizationDetails){
        return organization.createOrganization(organizationDetails);
    }

    @GetMapping
    public ResponseEntity<OrganizationDetails> getOrganization(@RequestParam String orgName){
        return organization.getOrganization(orgName);
    }
    @PostMapping("/addUser/{userName}")
    public ResponseEntity<?> addUserToOrganization(@PathVariable String userName,@RequestBody OrganizationDetails organizationDetails){
        return organization.addUserInOrg(organizationDetails,userName);
    }
}
