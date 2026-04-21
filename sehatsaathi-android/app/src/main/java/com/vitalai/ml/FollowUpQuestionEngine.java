package com.vitalai.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FollowUpQuestionEngine {
    private String diagnosis;
    private int score;
    private int questionIndex;
    private List<FollowUpQuestion> questions;
    private StringBuilder summary;

    public void start(String expectedDiagnosis, int initialScore) {
        this.diagnosis = expectedDiagnosis;
        this.score = initialScore;
        this.questionIndex = 0;
        this.summary = new StringBuilder("Diagnosis: " + expectedDiagnosis + "\nInitial Confidence: " + initialScore + "%\nResponses:\n");
        
        this.questions = new ArrayList<>();
        this.questions.add(new FollowUpQuestion("Are the affected areas itchy?", Arrays.asList("Yes", "No", "Not sure")));
        this.questions.add(new FollowUpQuestion("Has the rash spread swiftly?", Arrays.asList("Yes", "No", "Not sure")));
        this.questions.add(new FollowUpQuestion("Any recent exposure to new chemicals?", Arrays.asList("Yes", "No", "Not sure")));
    }

    public boolean hasMoreQuestions() {
        return questions != null && questionIndex < questions.size() && questionIndex < 5;
    }

    public FollowUpQuestion getNextQuestion() {
        if (hasMoreQuestions()) {
            return questions.get(questionIndex++);
        }
        return null;
    }

    public void submitAnswer(FollowUpQuestion question, String answer) {
        summary.append("- Q: ").append(question.getQuestionText()).append(" | A: ").append(answer).append("\n");
        if ("Yes".equalsIgnoreCase(answer)) {
            score += 5; // Mock logic 
        } else if ("No".equalsIgnoreCase(answer)) {
            score -= 2;
        }
        if (score > 99) score = 99;
    }

    public String getFinalSummary() {
        summary.append("Final Confidence Adjusted: ").append(score).append("%\n");
        return summary.toString();
    }

    public int getCurrentScore() {
        return score;
    }
}
