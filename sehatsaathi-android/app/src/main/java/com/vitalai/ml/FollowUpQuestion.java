package com.vitalai.ml;

import java.util.List;

public class FollowUpQuestion {
    private String questionText;
    private List<String> options;

    public FollowUpQuestion(String questionText, List<String> options) {
        this.questionText = questionText;
        this.options = options;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }
}
