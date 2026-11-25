package com.udhayanidhi.fitly.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AtsScorer {

    public record Check(String name, boolean passed, int points, int max, String tip) {}

    public record Report(int score, List<Check> checks) {}

    private static final int MAX_CHARS = 100_000;

    private static final Pattern EMAIL =
        Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE =
        Pattern.compile("\\+?\\d(?:[\\d().]|[ -](?=\\d)){8,16}\\d");
    private static final Pattern LINKS =
        Pattern.compile("(?i)linkedin\\.com|github\\.com");
    private static final Pattern BULLET =
        Pattern.compile("^\\s*(?:[\\u2022\\u25AA\\u25E6\\u2023\\u2043\\u00B7*-])\\s*\\S");
    private static final Pattern METRIC = Pattern.compile(
        "(?i)(?<![\\d/-])\\d+(?:\\.\\d+)?\\s?(?:%|\\+|x\\b|k\\b|ms\\b|users|customers|requests|hours"
            + "|days|members|teammates|screens|features|records|apis|endpoints)");
    private static final Pattern YEAR = Pattern.compile("\\b(?:19|20)\\d{2}\\b");

    private static final Set<String> ACTION_VERBS = Set.of(
        "developed", "built", "designed", "implemented", "led", "created", "optimized", "delivered",
        "deployed", "managed", "automated", "integrated", "fixed", "improved", "reduced", "launched",
        "collaborated", "containerized", "configured", "refactored", "tested", "analyzed", "migrated",
        "engineered", "mentored", "resolved", "established", "coordinated");

    private final SkillCatalog skillCatalog;

    public AtsScorer(SkillCatalog skillCatalog) {
        this.skillCatalog = skillCatalog;
    }

    public Report score(String resumeText) {
        String text = resumeText == null ? "" : resumeText;
        if (text.length() > MAX_CHARS) text = text.substring(0, MAX_CHARS);
        String[] lines = text.split("\\R");
        int words = text.isBlank() ? 0 : text.trim().split("\\s+").length;

        List<Check> checks = new ArrayList<>();

        // Contact details (16)
        checks.add(check("Email address", EMAIL.matcher(text).find() ? 6 : 0, 6,
            "Add a professional email address at the top of your resume."));
        checks.add(check("Phone number", hasPhone(text) ? 6 : 0, 6,
            "Add a phone number at the top so recruiters can call you."));
        checks.add(check("LinkedIn or GitHub link", LINKS.matcher(text).find() ? 4 : 0, 4,
            "Add your LinkedIn and GitHub links. Recruiters check them."));

        // Standard sections (28)
        checks.add(check("Skills section", hasSection(lines, "skills|competencies|technologies|tech stack") ? 8 : 0, 8,
            "Add a clearly named 'Skills' section. ATS software looks for that exact heading."));
        checks.add(check("Education section", hasSection(lines, "education|academic|qualification") ? 8 : 0, 8,
            "Add an 'Education' section with degree, college and year."));
        boolean work = hasSection(lines, "experience|employment|work history|internship|training|projects?");
        checks.add(check("Experience or Projects section", work ? 8 : 0, 8,
            "Add a 'Projects' or 'Experience' section. It is the most important part for freshers."));
        checks.add(check("Summary section", hasSection(lines, "summary|objective|profile|about me") ? 4 : 0, 4,
            "Add a 2-3 line 'Professional Summary' at the top with your target role."));

        // Length (10) and readability (8)
        int lengthPoints;
        String lengthTip;
        if (words >= 300 && words <= 1000) {
            lengthPoints = 10;
            lengthTip = "";
        } else if (words >= 150 && words < 300) {
            lengthPoints = 5;
            lengthTip = "Your resume is quite short. Add project details and what you achieved.";
        } else if (words > 1000) {
            lengthPoints = 5;
            lengthTip = "Your resume is long. Keep it to one page if you have under 3 years of experience.";
        } else {
            lengthPoints = 0;
            lengthTip = "Very little text could be read from your PDF. Make sure it is not a scanned image.";
        }
        checks.add(check("Resume length", lengthPoints, 10, lengthTip));
        checks.add(check("Text is machine-readable", words >= 100 ? 8 : 0, 8,
            "ATS software could not read enough text. Export as a text-based PDF, not an image or scan."));

        // Writing quality (30)
        int bullets = 0;
        for (String line : lines) {
            if (BULLET.matcher(line).find()) bullets++;
        }
        checks.add(check("Bullet points", bullets >= 5 ? 6 : bullets >= 2 ? 3 : 0, 6,
            "Describe projects and experience in bullet points, not long paragraphs."));

        Set<String> verbsFound = new HashSet<>();
        for (String word : text.toLowerCase().split("[^a-z]+")) {
            if (ACTION_VERBS.contains(word)) verbsFound.add(word);
        }
        checks.add(check("Strong action verbs", verbsFound.size() >= 6 ? 10 : verbsFound.size() >= 3 ? 5 : 0, 10,
            "Start bullets with action verbs like Built, Designed, Implemented, Reduced, Led."));

        int metrics = 0;
        Matcher m = METRIC.matcher(text);
        while (m.find()) metrics++;
        checks.add(check("Numbers and results", metrics >= 3 ? 10 : metrics >= 1 ? 5 : 0, 10,
            "Add numbers to your achievements, like '5 teammates', '30% faster' or '10+ API endpoints'."));

        Set<String> years = new HashSet<>();
        Matcher y = YEAR.matcher(text);
        while (y.find()) years.add(y.group());
        checks.add(check("Dates present", years.size() >= 2 ? 4 : 0, 4,
            "Add dates (month and year) for your education, training and experience."));

        // Skills keywords (8)
        int skillCount = skillCatalog.extract(text).size();
        checks.add(check("Technical skills listed", skillCount >= 8 ? 8 : skillCount >= 4 ? 4 : 0, 8,
            "List at least 8 relevant technical skills, using the standard names (for example PostgreSQL, not 'Postgres DB')."));

        int total = 0;
        for (Check c : checks) total += c.points();
        return new Report(total, checks);
    }

    private Check check(String name, int points, int max, String tip) {
        boolean passed = points == max;
        return new Check(name, passed, points, max, passed ? "" : tip);
    }

    private boolean hasPhone(String text) {
        Matcher m = PHONE.matcher(text);
        while (m.find()) {
            int digits = m.group().replaceAll("\\D", "").length();
            if (digits >= 10 && digits <= 13) return true;
        }
        return false;
    }

    private boolean hasSection(String[] lines, String keywords) {
        Pattern p = Pattern.compile("(?i)\\b(?:" + keywords + ")\\b");
        for (String line : lines) {
            String t = line.trim();
            if (!t.isEmpty() && t.length() <= 45 && p.matcher(t).find()) return true;
        }
        return false;
    }
}