package com.udhayanidhi.fitly.analysis;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FactsExtractor {

    private static final int MAX_CHARS = 100_000;

    private static final List<String> MONTHS =
        List.of("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec");

    // ---------- resume patterns ----------
    private static final Pattern ENGINEERING_DEGREE = Pattern.compile(
        "\\bBE\\b\\s{0,3}[-:|(,\u2013\u2014]|\\bB\\.\\s?E\\b"
        + "|(?i:\\bb\\.?\\s?tech\\b|\\bm\\.?\\s?tech\\b|\\bm\\.\\s?e\\b"
        + "|bachelor of (?:engineering|technology)|master of (?:engineering|technology))");

    private static final Pattern COMPUTER_APPS = Pattern.compile(
        "(?i)\\b(?:bca|mca|bsc|b\\.\\s?sc|msc|m\\.\\s?sc|bachelor of computer applications"
        + "|master of computer applications|bachelor of science)\\b");

    private static final Pattern OTHER_DEGREE = Pattern.compile(
        "(?i)\\b(?:bba|mba|bcom|b\\.\\s?com|mcom|m\\.\\s?com|diploma|bachelor of arts|bachelor of commerce)\\b");

    private static final Pattern CS_FIELD = Pattern.compile(
        "(?i)computer|information technology|\\bcse\\b|\\bit\\b|software|data science|artificial intelligence|\\bai\\b|cyber");

    private static final Pattern YEAR = Pattern.compile("\\b(?:19|20)\\d{2}\\b");

    private static final Pattern EXPLICIT_EXP = Pattern.compile(
        "(?i)(\\d{1,2}(?:\\.\\d)?)\\s{0,3}\\+?\\s{0,3}(?:years|yrs|year)\\s+(?:of\\s+)?"
        + "(?:(?:professional|relevant|total|work|industry|hands-on)\\s+)?experience");

    private static final Pattern EXP_HEADER = Pattern.compile(
        "(?i)(?:(?:work|professional|employment|relevant|industry)\\s+)?"
        + "(?:experience|work history|employment history)\\s*:?");

    private static final Pattern END_WORDS = Pattern.compile(
        "(?i)\\b(?:projects?|education|skills?|certifications?|achievements|summary|internships?"
        + "|training|awards|languages|interests)\\b");

    private static final String MON =
        "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?,?\\s+";

    private static final Pattern RANGE = Pattern.compile(
        "(?i)(?:" + MON + ")?((?:19|20)\\d{2})\\s*(?:-|to|\u2013|\u2014)\\s*"
        + "(?:(?:" + MON + ")?((?:19|20)\\d{2})|(present|current|till date|now|ongoing))");

    private static final Pattern INTERN = Pattern.compile("(?i)intern|trainee|training|apprentice");

    // ---------- job patterns ----------
    private static final Pattern EXP_RANGE = Pattern.compile(
        "(\\d{1,2})\\s{0,3}(?:-|to)\\s{0,3}(\\d{1,2})\\s{0,3}\\+?\\s{0,3}(?:years|yrs|year)");

    private static final Pattern EXP_MIN_A = Pattern.compile(
        "(\\d{1,2})\\s{0,3}\\+?\\s{0,3}(?:years|yrs|year)\\b[^.\\n]{0,60}experience");

    private static final Pattern EXP_MIN_B = Pattern.compile(
        "experience[^.\\n]{0,40}?(\\d{1,2})\\s{0,3}\\+?\\s{0,3}(?:years|yrs)");

    private static final Pattern FRESHER = Pattern.compile(
        "\\bfreshers?\\b|entry[- ]level|new grad(?:uate)?s?\\b|recent graduates?|campus hire|\\b0 years");

    private static final Pattern DEGREE_WORDS = Pattern.compile(
        "bachelor|\\bb\\.\\s?e\\b|\\bb\\.?\\s?tech\\b|\\bm\\.?\\s?tech\\b|\\bdegree\\b|\\bmaster'?s?\\b|\\bgraduat\\w*");

    private static final Pattern ANY_DEGREE = Pattern.compile(
        "any (?:degree|graduate|graduation|discipline|stream|bachelor)|degree in any|any field");

    private static final Pattern CS_WORDS = Pattern.compile(
        "(?i:computer science|\\bcse\\b|information technology)|\\bIT\\b");

    private static final Pattern BATCH_KEYWORD = Pattern.compile(
        "\\bbatch(?:es)?\\b|pass-?outs?\\b|passed[- ]?out|graduat\\w*|class of|year of (?:passing|graduation)");

    // =====================================================
    // Resume
    // =====================================================
    public ResumeFacts fromResume(String text) {
        String safe = limit(text);
        String[] lines = safe.split("\\R");

        int idx = firstLine(lines, ENGINEERING_DEGREE);
        String type = "ENGINEERING";
        if (idx < 0) {
            idx = firstLine(lines, COMPUTER_APPS);
            type = "COMPUTER_APPLICATIONS";
        }
        if (idx < 0) {
            idx = firstLine(lines, OTHER_DEGREE);
            type = "OTHER";
        }
        if (idx < 0) type = "UNKNOWN";

        String degreeLine = "";
        boolean cs = false;
        Integer gradYear = null;
        if (idx >= 0) {
            String line = lines[idx].trim();
            degreeLine = line.length() > 150 ? line.substring(0, 150) : line;
            String field = line;
            int minor = field.toLowerCase().indexOf("minor");
            if (minor >= 0) field = field.substring(0, minor);
            cs = CS_FIELD.matcher(field).find();
            gradYear = graduationYear(lines, idx);
        }
        return new ResumeFacts(type, degreeLine, cs, gradYear, experienceYears(safe, lines));
    }

    private int firstLine(String[] lines, Pattern pattern) {
        for (int i = 0; i < lines.length; i++) {
            if (pattern.matcher(lines[i]).find()) return i;
        }
        return -1;
    }

    private Integer graduationYear(String[] lines, int idx) {
        int[] order = {idx, idx - 1, idx + 1};
        for (int i : order) {
            if (i < 0 || i >= lines.length) continue;
            Integer year = maxYear(lines[i]);
            if (year != null) return year;
        }
        return null;
    }

    private Integer maxYear(String line) {
        int limit = LocalDate.now().getYear() + 7;
        Integer best = null;
        Matcher m = YEAR.matcher(line);
        while (m.find()) {
            int y = Integer.parseInt(m.group());
            if (y >= 1990 && y <= limit && (best == null || y > best)) best = y;
        }
        return best;
    }

    private double experienceYears(String text, String[] lines) {
        double explicit = 0;
        Matcher e = EXPLICIT_EXP.matcher(text);
        while (e.find()) {
            explicit = Math.max(explicit, Double.parseDouble(e.group(1)));
        }
        if (explicit > 0) return explicit;

        int start = -1;
        for (int i = 0; i < lines.length; i++) {
            if (EXP_HEADER.matcher(lines[i].trim()).matches()) {
                start = i + 1;
                break;
            }
        }
        if (start < 0) return 0;

        int totalMonths = 0;
        for (int i = start; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isEndHeader(line)) break;
            Matcher m = RANGE.matcher(line);
            while (m.find()) {
                String context = line.toLowerCase() + " " + (i > start ? lines[i - 1].toLowerCase() : "");
                if (INTERN.matcher(context).find()) continue;
                totalMonths += months(m);
            }
        }
        return Math.round(totalMonths / 12.0 * 10) / 10.0;
    }

    private boolean isEndHeader(String line) {
        if (line.isEmpty() || line.length() > 40 || !END_WORDS.matcher(line).find()) return false;
        for (String word : line.split("\\s+")) {
            char c = word.charAt(0);
            if (Character.isLetter(c) && !Character.isUpperCase(c)) return false;
        }
        return true;
    }

    private int months(Matcher m) {
        int startMonth = monthIndex(m.group(1), 1);
        int startYear = Integer.parseInt(m.group(2));
        int endYear;
        int endMonth;
        if (m.group(5) != null) {
            LocalDate now = LocalDate.now();
            endYear = now.getYear();
            endMonth = now.getMonthValue();
        } else {
            endYear = Integer.parseInt(m.group(4));
            endMonth = monthIndex(m.group(3), 12);
        }
        int months = (endYear * 12 + endMonth) - (startYear * 12 + startMonth) + 1;
        return months > 0 && months < 600 ? months : 0;
    }

    private int monthIndex(String name, int fallback) {
        if (name == null || name.length() < 3) return fallback;
        int i = MONTHS.indexOf(name.toLowerCase().substring(0, 3));
        return i < 0 ? fallback : i + 1;
    }

    // =====================================================
    // Job description
    // =====================================================
    public JobRequirements fromJob(String text) {
        String original = limit(text);
        String t = original.toLowerCase().replace('\u2013', '-').replace('\u2014', '-');

        Integer min = null;
        Integer max = null;
        Matcher range = EXP_RANGE.matcher(t);
        if (range.find()) {
            int a = Integer.parseInt(range.group(1));
            int b = Integer.parseInt(range.group(2));
            if (a <= b && b <= 40) {
                min = a;
                max = b;
            }
        }
        if (min == null) {
            Matcher m = EXP_MIN_A.matcher(t);
            if (m.find()) {
                min = Integer.parseInt(m.group(1));
            } else {
                m = EXP_MIN_B.matcher(t);
                if (m.find()) min = Integer.parseInt(m.group(1));
            }
        }
        boolean freshers = FRESHER.matcher(t).find() || (min != null && min == 0);
        if (freshers && min == null) min = 0;

        boolean degreeRequired = DEGREE_WORDS.matcher(t).find();
        boolean anyDegreeOk = ANY_DEGREE.matcher(t).find();
        boolean csMentioned = CS_WORDS.matcher(original).find();

        Set<Integer> batch = new TreeSet<>();
        Matcher k = BATCH_KEYWORD.matcher(t);
        while (k.find()) {
            int from = Math.max(0, k.start() - 40);
            int to = Math.min(t.length(), k.end() + 40);
            Matcher y = YEAR.matcher(t.substring(from, to));
            while (y.find()) {
                int year = Integer.parseInt(y.group());
                if (year >= 2015 && year <= 2035) batch.add(year);
            }
        }

        return new JobRequirements(degreeRequired, anyDegreeOk, csMentioned, batch, min, max, freshers);
    }

    private String limit(String text) {
        if (text == null) return "";
        return text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
    }
}