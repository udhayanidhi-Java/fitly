package com.udhayanidhi.fitly.analysis;

public record ResumeFacts(
    String degreeType,
    String degreeLine,
    boolean csRelatedField,
    Integer graduationYear,
    double experienceYears) {}