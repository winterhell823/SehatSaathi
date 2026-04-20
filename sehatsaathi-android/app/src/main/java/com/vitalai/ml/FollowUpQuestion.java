package com.vitalai.ml;

import java.util.List;
import java.util.Map;

public class FollowUpQuestion {
    private final String id;
    private final String disease;
    private final String questionText;
    private final List<String> options;
    private final Map<String, Integer> scoreImpact;
    private final boolean critical;

    public FollowUpQuestion(
            String id,
            String disease,
            String questionText,
            List<String> options,
            Map<String, Integer> scoreImpact,
            boolean critical
    ) {
        this.id = id;
        this.disease = disease;
        this.questionText = questionText;
        this.options = options;
        this.scoreImpact = scoreImpact;
        this.critical = critical;
    }

    public String getId() {
        return id;
    }

    public String getDisease() {
        return disease;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public Map<String, Integer> getScoreImpact() {
        return scoreImpact;
    }

    public boolean isCritical() {
        return critical;
    }

    public int getImpactForAnswer(String answer) {
        if (scoreImpact == null || !scoreImpact.containsKey(answer)) {
            return 0;
        }
        return scoreImpact.get(answer);
    }
}
