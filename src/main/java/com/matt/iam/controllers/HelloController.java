package com.matt.iam.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matt.iam.entities.User;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        User user = new User();
        user.setNickname("Hiraeth");

        return "Hello, world!" + user.getNickname();
    }
}
