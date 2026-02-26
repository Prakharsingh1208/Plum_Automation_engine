package com.plumauto.controller;


import com.plumauto.entity.UserDetails;
import com.plumauto.service.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDetails user){
        return users.createUser(user);
    }
}
