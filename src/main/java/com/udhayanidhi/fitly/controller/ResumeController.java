package com.udhayanidhi.fitly.controller;

import com.udhayanidhi.fitly.model.Resume;
import com.udhayanidhi.fitly.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resume upload(@RequestParam Long userId, @RequestParam MultipartFile file) throws IOException {
        return resumeService.upload(userId, file);
    }
}