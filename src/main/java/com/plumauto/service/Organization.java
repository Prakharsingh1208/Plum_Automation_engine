package com.plumauto.service;

import com.plumauto.entity.OrganizationDetails;
import com.plumauto.entity.UserDetails;
import com.plumauto.repository.OrganizationRepository;
import com.plumauto.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class Organization {
    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    public ResponseEntity<?> createOrganization(OrganizationDetails organizationDetails){
        if(organizationDetails.getOrgName().split(" ").length>1){
            log.info("OrganizationDetails name should not contain spaces: " + organizationDetails.getOrgName());
            return new ResponseEntity<>("OrganizationDetails name should not contain spaces", HttpStatus.BAD_REQUEST);
        }
        try {
            organizationDetails.setPassKey(Objects.requireNonNull(passwordEncoder.encode(organizationDetails.getPassKey())));
            organizationRepository.save(organizationDetails);
            log.info("OrganizationDetails created successfully: " + organizationDetails.getOrgName());
            return new ResponseEntity<>(organizationDetails, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error creating organizationDetails: " + e.getMessage());
            return new ResponseEntity<>("Error creating organizationDetails", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<OrganizationDetails> getOrganization(String orgName){
        OrganizationDetails organizationDetails = organizationRepository.findByOrgName(orgName);
        if(organizationDetails ==null){
            log.info("OrganizationDetails not found: " + orgName);
            return new ResponseEntity<>((HttpHeaders) null, HttpStatus.NOT_FOUND);
        }
        log.info("OrganizationDetails found: " + orgName);
        return new ResponseEntity<>(organizationDetails, HttpStatus.OK);
    }

    public ResponseEntity<?> addUserInOrg(OrganizationDetails organizationDetails, String userName){
        UserDetails user = userRepository.findByUsername(userName);
        OrganizationDetails organization = organizationRepository.findByOrgName(organizationDetails.getOrgName());
        if(user==null){
            log.info("User not found: " + userName);
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        if(organization.getMembers().contains(user)){
            log.info("User already a member of the organization: " + userName);
            return new ResponseEntity<>("User already a member of the organization", HttpStatus.BAD_REQUEST);
        }
        if(passwordEncoder.matches(organizationDetails.getPassKey(), organization.getPassKey())){
            organization.getMembers().addFirst(user);
            organizationRepository.save(organization);
            return new ResponseEntity<>("User added to organization successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid passkey", HttpStatus.BAD_REQUEST);
    }
}
