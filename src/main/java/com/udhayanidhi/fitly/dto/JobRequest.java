package com.udhayanidhi.fitly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JobRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String company;

    @NotBlank
    @Size(max = 20000)
    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}