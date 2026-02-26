package com.plumauto.service;

import com.mongodb.lang.NonNull;
import com.plumauto.entity.UserDetails;
import com.plumauto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsersDetailsImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;



    @NonNull
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = userRepository.findByUsername(username);

        if(user!=null){

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                   .roles(user.getRoles())
                    .build();
        }
        System.out.println("User not found with username: " + username);
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
