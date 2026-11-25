package com.udhayanidhi.fitly.analysis;

import java.util.Set;

public record JobRequirements(
    boolean degreeRequired,
    boolean anyDegreeOk,
    boolean csMentioned,
    Set<Integer> batchYears,
    Integer minYears,
    Integer maxYears,
    boolean freshersWelcome) {}