package com.plumauto.service;

import com.plumauto.entity.UserDetails;
import com.plumauto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Users {
    @Autowired
    UserRepository userRepository;

    public UserDetails getUserDetails(String username){
        return userRepository.findByUsername(username);
    }

    public ResponseEntity<String> createUser(UserDetails user){
        try {
            if(userRepository.count()==0){
                user.setUserType("admin");
            } else if (user.getUserType().isBlank()) {
                user.setUserType("user");
            }
            userRepository.save(user);
            return new ResponseEntity<>("User Successfully Created",HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error Creating User",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> deleteUser(String userName){
        UserDetails user = userRepository.findByUsername(userName);
        if(user==null){
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
        userRepository.delete(user);
        return new ResponseEntity<>("User Successfully Deleted",HttpStatus.OK);
    }

    public ResponseEntity<String> editUser(UserDetails editedUser){
        Optional<UserDetails> user = userRepository.findById(editedUser.getUserId());
        if(user.isEmpty()){
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
        if(user.get().getUserType().equals(editedUser.getUserType())){
            userRepository.save(editedUser);
            return new ResponseEntity<>("User Successfully Updated",HttpStatus.OK);
        }else {
            return new ResponseEntity<>("User Type cannot be changed",HttpStatus.BAD_REQUEST);
        }
    }




}
