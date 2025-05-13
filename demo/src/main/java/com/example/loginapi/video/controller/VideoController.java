package com.example.loginapi.video.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @PostMapping("/upload")
    public String uploadVideo() {
        return "upload done";
    }
}