package com.udhayanidhi.fitly.service;

import com.udhayanidhi.fitly.model.Resume;
import com.udhayanidhi.fitly.model.User;
import com.udhayanidhi.fitly.repository.ResumeRepository;
import com.udhayanidhi.fitly.repository.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    public Resume upload(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String text;
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            text = new PDFTextStripper().getText(doc);
        }
        Resume resume = new Resume();
        resume.setUser(user);
        resume.setFileName(file.getOriginalFilename());
        resume.setContent(text);
        return resumeRepository.save(resume);
    }
}