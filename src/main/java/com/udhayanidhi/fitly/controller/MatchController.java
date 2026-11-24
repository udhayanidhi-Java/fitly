package com.udhayanidhi.fitly.controller;

import com.udhayanidhi.fitly.dto.MatchRequest;
import com.udhayanidhi.fitly.dto.MatchResponse;
import com.udhayanidhi.fitly.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse match(@Valid @RequestBody MatchRequest request) {
        return MatchResponse.from(matchService.match(request));
    }

    @GetMapping("/resume/{resumeId}")
    public List<MatchResponse> byResume(@PathVariable Long resumeId) {
        return matchService.getByResume(resumeId).stream()
            .map(MatchResponse::from)
            .toList();
    }
}