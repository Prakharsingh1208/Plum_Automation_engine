package com.plumauto.controller;


import com.plumauto.entity.UserDetails;
import com.plumauto.service.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class User {
    @Autowired
     Users users;

    @GetMapping("/{userName}")
    public ResponseEntity<?> getUserDetails(@PathVariable String userName){
        if(users.getUserDetails(userName)==null){
            return ResponseEntity.notFound().build();
        }
        return new  ResponseEntity<>(users.getUserDetails(userName),HttpStatus.OK);
    }
}
