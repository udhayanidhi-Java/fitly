package com.udhayanidhi.fitly.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JobMatchScorer {

    private static final Set<String> STOP = new HashSet<>(Arrays.asList((
        "the,and,for,with,that,this,from,have,will,your,you,are,our,their,they,them,has,had,not,but,can,all,any,"
        + "more,most,other,such,than,then,these,those,who,what,when,where,which,while,into,over,under,also,about,"
        + "after,before,between,through,during,each,both,per,via,etc,able,ability,work,works,working,team,teams,"
        + "experience,experiences,strong,skills,skill,years,year,required,requirements,requirement,preferred,"
        + "qualifications,qualification,basic,role,roles,join,company,business,knowledge,understanding,ensure,"
        + "support,including,include,includes,responsibilities,responsibility,candidate,candidates,position,"
        + "positions,job,jobs,looking,seeking,should,must,would,could,being,been,were,well,good,great,excellent,"
        + "new,use,using,used,make,making,help,helping,across,within,without,part,time,full,day,days,related,"
        + "relevant,plus,bonus,like,opportunity,opportunities,environment,customers,customer,solutions,solution,"
        + "products,product,services,service,based,high,level,build,building,develop,developing,development,"
        + "equal,employer,employment,diversity,inclusive,inclusion,accommodation,accommodations,disability,"
        + "applicants,applicant,application,applications,process,hiring,recruiting,benefits,please,visit,more,"
        + "information,contact,partner,recruiting,every,over,here,them,other,help,need,needs,want,wants,only").split(",")));

    private final SkillCatalog catalog;
    private final FactsExtractor extractor;
    private final SoftSkillAnalyzer softSkills;
    private final AtsScorer atsScorer;

    public JobMatchScorer(SkillCatalog catalog, FactsExtractor extractor,
                          SoftSkillAnalyzer softSkills, AtsScorer atsScorer) {
        this.catalog = catalog;
        this.extractor = extractor;
        this.softSkills = softSkills;
        this.atsScorer = atsScorer;
    }

    public AnalysisReport analyze(String resumeText, String jobText) {
        ResumeFacts rf = extractor.fromResume(resumeText);
        JobRequirements jr = extractor.fromJob(jobText);

        Set<String> jobSkills = catalog.extract(jobText);
        Set<String> resumeSkills = catalog.extract(resumeText);
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String s : jobSkills) {
            if (resumeSkills.contains(s)) matched.add(s);
            else missing.add(s);
        }

        // keywords the job repeats that are not tech skills
        Set<String> skillTokens = new HashSet<>();
        for (String s : jobSkills) {
            for (String token : s.toLowerCase().split("[^a-z]+")) {
                skillTokens.add(token);
                skillTokens.add(stem(token));
            }
        }
        List<String> keywords = topKeywords(jobText, skillTokens);
        Set<String> resumeWords = new HashSet<>();
        for (String w : resumeText.toLowerCase().split("[^a-z]+")) resumeWords.add(stem(w));
        List<String> missingKeywords = new ArrayList<>();
        for (String k : keywords) {
            if (!resumeWords.contains(k)) missingKeywords.add(k);
        }

        SoftSkillAnalyzer.Result soft = softSkills.analyze(resumeText, jobText);

        List<AnalysisReport.Section> sections = new ArrayList<>();
        sections.add(skillsSection(matched, missing));
        sections.add(experienceSection(rf, jr));
        sections.add(educationSection(rf, jr));
        sections.add(yearSection(rf, jr));
        sections.add(softSection(soft));
        sections.add(keywordSection(keywords, missingKeywords));

        int total = 0;
        for (AnalysisReport.Section s : sections) total += s.points();
        total = Math.max(0, Math.min(100, total));

        // NEW: build the ATS report once and collect its tips
        AtsScorer.Report ats = atsScorer.score(resumeText);
        List<String> atsTips = new ArrayList<>();
        for (var check : ats.checks()) {
            if (check.tip() != null && !check.tip().isBlank()) atsTips.add(check.tip());
        }

        // NEW: ask the "teacher with a red pen" for resume fixes
        List<String> fixes = ResumeFixSuggester.suggest(missing, missingKeywords, atsTips, soft.missing());
        List<SkillLearningSuggester.LearnItem> learn = SkillLearningSuggester.suggest(missing);

        return new AnalysisReport(total, verdict(total), sections, matched, missing, missingKeywords,
                soft, ats, rf, jr, fixes, learn);
    }

    // ---------------- sections ----------------

    private AnalysisReport.Section skillsSection(List<String> matched, List<String> missing) {
        int total = matched.size() + missing.size();
        if (total == 0) {
            return section("Technical skills", 20, 40,
                "We found no known technical skills in the job description, so this part is scored neutrally.");
        }
        int points = (int) Math.round(40.0 * matched.size() / total);
        String note = "You match " + matched.size() + " of " + total + " skills the job asks for."
            + (missing.isEmpty() ? "" : " Missing: " + String.join(", ", missing) + ".");
        return section("Technical skills", points, 40, note);
    }

    private AnalysisReport.Section experienceSection(ResumeFacts rf, JobRequirements jr) {
        double exp = rf.experienceYears();
        Integer min = jr.minYears();
        Integer max = jr.maxYears();
        String yours = exp == 0 ? "no professional experience (fresher)" : fmt(exp) + " years";
        if (min == null) {
            return section("Experience", 15, 15, "The job does not state an experience requirement.");
        }
        String required = max != null ? min + "-" + max + " years"
            : min == 0 ? "freshers welcome" : min + "+ years";
        if (max != null) {
            if (exp >= min && exp <= max) {
                return section("Experience", 15, 15, "The job asks for " + required + ". You have " + yours + ". This fits.");
            }
            if (exp < min) {
                int points = (int) Math.round(15.0 * exp / min);
                return section("Experience", points, 15, "The job asks for " + required + ". You have " + yours
                    + ". Internships and projects help, but they do not count as professional years.");
            }
            return section("Experience", 12, 15, "The job asks for " + required + ". You have " + yours
                + ". You may be over-qualified, but it usually still works.");
        }
        if (exp >= min) {
            return section("Experience", 15, 15, "The job asks for " + required + ". You have " + yours + ". This fits.");
        }
        int points = (int) Math.round(15.0 * exp / min);
        return section("Experience", points, 15, "The job asks for " + required + ". You have " + yours
            + ". Internships and projects help, but they do not count as professional years.");
    }

    private AnalysisReport.Section educationSection(ResumeFacts rf, JobRequirements jr) {
        String type = rf.degreeType();
        if (!jr.degreeRequired() && !jr.anyDegreeOk()) {
            return section("Education", 10, 10, "No specific degree requirement was found in the job description.");
        }
        if ("UNKNOWN".equals(type)) {
            return section("Education", 2, 10,
                "We could not find your degree in your resume. Add an Education section with degree, college and year.");
        }
        if (jr.anyDegreeOk()) {
            return section("Education", 10, 10, "The job accepts any degree.");
        }
        switch (type) {
            case "ENGINEERING":
                return section("Education", 10, 10, jr.csMentioned()
                    ? "The job asks for CS, IT or a relevant engineering degree. Your engineering degree counts as a match."
                    : "Your engineering degree meets the requirement.");
            case "COMPUTER_APPLICATIONS":
                return section("Education", 9, 10,
                    "Your computer applications or science degree is relevant, but the job may prefer an engineering degree.");
            default:
                return section("Education", 3, 10,
                    "The job asks for an engineering or related degree, and your degree looks different. Highlight relevant certifications and projects.");
        }
    }

    private AnalysisReport.Section yearSection(ResumeFacts rf, JobRequirements jr) {
        Set<Integer> batch = jr.batchYears();
        if (batch.isEmpty()) {
            return section("Passout year", 5, 5, "The job does not restrict the graduation year.");
        }
        Integer year = rf.graduationYear();
        if (year == null) {
            return section("Passout year", 2, 5,
                "The job is for the " + batch + " batch. We could not find your graduation year. Add it to your Education section.");
        }
        if (batch.contains(year)) {
            return section("Passout year", 5, 5, "The job is for the " + batch + " batch, and your graduation year is " + year + ". This fits.");
        }
        return section("Passout year", 0, 5, "The job is for the " + batch + " batch, but your graduation year is " + year + ".");
    }

    private AnalysisReport.Section softSection(SoftSkillAnalyzer.Result sr) {
        String note = sr.missing().isEmpty()
            ? "Your resume shows evidence for every soft skill we checked."
            : "We found no clear evidence of: " + String.join(", ", sr.missing())
                + ". Add examples, such as 'led a team of 4' or 'presented a demo to the class'.";
        return section("Soft skills", sr.score(), 10, note);
    }

    private AnalysisReport.Section keywordSection(List<String> keywords, List<String> missingKeywords) {
        if (keywords.size() < 3) {
            return section("Keywords", 10, 20, "The job description has too few repeated keywords to compare, so this part is scored neutrally.");
        }
        int found = keywords.size() - missingKeywords.size();
        int points = (int) Math.round(20.0 * found / keywords.size());
        String note = "Your resume uses " + found + " of the " + keywords.size() + " important words the job repeats."
            + (missingKeywords.isEmpty() ? "" : " Missing: " + String.join(", ", missingKeywords) + ".");
        return section("Keywords", points, 20, note);
    }

    // ---------------- helpers ----------------

    private List<String> topKeywords(String jobText, Set<String> skillTokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String w : jobText.toLowerCase().split("[^a-z]+")) {
            if (w.length() < 4 || w.length() > 25 || STOP.contains(w) || skillTokens.contains(w)) continue;
            freq.merge(stem(w), 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() >= 2) entries.add(e);
        }
        entries.sort((a, b) -> {
            int c = Integer.compare(b.getValue(), a.getValue());
            return c != 0 ? c : a.getKey().compareTo(b.getKey());
        });
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(15, entries.size()); i++) result.add(entries.get(i).getKey());
        return result;
    }

    private String stem(String w) {
        return w.length() > 4 && w.endsWith("s") ? w.substring(0, w.length() - 1) : w;
    }

    private String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
    }

    private String verdict(int score) {
        if (score >= 80) return "Strong match";
        if (score >= 60) return "Good match";
        if (score >= 40) return "Partial match";
        return "Low match";
    }

    private AnalysisReport.Section section(String name, int points, int max, String note) {
        double ratio = max == 0 ? 1 : (double) points / max;
        String status = ratio >= 0.8 ? "good" : ratio >= 0.5 ? "partial" : "poor";
        return new AnalysisReport.Section(name, points, max, status, note);
    }
}