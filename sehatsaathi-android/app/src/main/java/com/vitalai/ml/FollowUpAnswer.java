package com.vitalai.ml;

public class FollowUpAnswer {
    private final String questionId;
    private final String answer;

    public FollowUpAnswer(String questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getAnswer() {
        return answer;
    }
}
