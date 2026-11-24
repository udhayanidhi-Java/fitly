package com.udhayanidhi.fitly.controller;

import com.udhayanidhi.fitly.dto.MatchRequest;
import com.udhayanidhi.fitly.model.MatchResult;
import com.udhayanidhi.fitly.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResult match(@Valid @RequestBody MatchRequest request) {
        return matchService.match(request);
    }
}