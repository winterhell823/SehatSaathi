package com.vitalai.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowUpQuestionEngine {

    private static final int MAX_QUESTIONS = 5;

    private final List<FollowUpQuestion> allQuestions = new ArrayList<>();
    private final List<FollowUpQuestion> selectedQuestions = new ArrayList<>();
    private final List<FollowUpAnswer> givenAnswers = new ArrayList<>();

    private String predictedDisease;
    private int currentScore;
    private int questionsAsked;
    private boolean referralNeeded;

    public FollowUpQuestionEngine() {
        seedQuestions();
    }

    public void start(String predictedDisease, int baseScore) {
        this.predictedDisease = predictedDisease;
        this.currentScore = baseScore;
        this.questionsAsked = 0;
        this.referralNeeded = false;
        this.selectedQuestions.clear();
        this.givenAnswers.clear();

        for (FollowUpQuestion q : allQuestions) {
            if (q.getDisease().equalsIgnoreCase(predictedDisease)) {
                selectedQuestions.add(q);
            }
        }
    }

    public boolean hasMoreQuestions() {
        return questionsAsked < MAX_QUESTIONS && questionsAsked < selectedQuestions.size();
    }

    public FollowUpQuestion getNextQuestion() {
        if (!hasMoreQuestions()) {
            return null;
        }
        return selectedQuestions.get(questionsAsked);
    }

    public void submitAnswer(FollowUpQuestion question, String answer) {
        if (question == null || answer == null) {
            return;
        }

        givenAnswers.add(new FollowUpAnswer(question.getId(), answer));
        currentScore += question.getImpactForAnswer(answer);

        if (question.isCritical() && isReferralTrigger(question, answer)) {
            referralNeeded = true;
        }

        questionsAsked++;
        currentScore = Math.max(0, Math.min(100, currentScore));
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public boolean isReferralNeeded() {
        return referralNeeded;
    }

    public List<FollowUpAnswer> getGivenAnswers() {
        return givenAnswers;
    }

    public String getFinalSummary() {
        String action = referralNeeded ? "Refer to clinic" : "Home care possible";
        return "Likely Condition: " + predictedDisease +
                "\nConfidence: " + currentScore + "%" +
                "\nRecommended Action: " + action;
    }

    private boolean isReferralTrigger(FollowUpQuestion question, String answer) {
        String qid = question.getId();

        if ("derm_fever".equals(qid) && "Yes".equalsIgnoreCase(answer)) {
            return true;
        }
        if ("fungal_spread".equals(qid) && "Rapidly spreading".equalsIgnoreCase(answer)) {
            return true;
        }
        if ("scabies_secondary".equals(qid) && "Yes".equalsIgnoreCase(answer)) {
            return true;
        }
        return false;
    }

    private void seedQuestions() {
        allQuestions.clear();

        allQuestions.add(new FollowUpQuestion(
                "derm_duration",
                "Contact Dermatitis",
                "Since when has the rash been present?",
                Arrays.asList("Less than 1 day", "1-3 days", "More than 3 days", "More than 1 week"),
                mapOf(
                        "Less than 1 day", 6,
                        "1-3 days", 10,
                        "More than 3 days", 4,
                        "More than 1 week", -3
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "derm_itching",
                "Contact Dermatitis",
                "Is the affected area itchy?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", 12,
                        "No", -8
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "derm_exposure",
                "Contact Dermatitis",
                "Was there recent exposure to soap, detergent, cream, or chemical?",
                Arrays.asList("Yes", "No", "Not sure"),
                mapOf(
                        "Yes", 15,
                        "No", -5,
                        "Not sure", 0
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "derm_fever",
                "Contact Dermatitis",
                "Do you also have fever or feel generally unwell?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", -10,
                        "No", 5
                ),
                true
        ));

        allQuestions.add(new FollowUpQuestion(
                "derm_progression",
                "Contact Dermatitis",
                "Is the rash getting worse, better, or staying the same?",
                Arrays.asList("Worse", "Same", "Better"),
                mapOf(
                        "Worse", 5,
                        "Same", 3,
                        "Better", -2
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "fungal_itching",
                "Fungal Infection",
                "Is there itching in the affected area?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", 10,
                        "No", -6
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "fungal_ring",
                "Fungal Infection",
                "Does the rash look circular or ring-shaped?",
                Arrays.asList("Yes", "No", "Not sure"),
                mapOf(
                        "Yes", 15,
                        "No", -8,
                        "Not sure", 0
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "fungal_moisture",
                "Fungal Infection",
                "Has the area remained sweaty or moist for long periods?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", 8,
                        "No", -2
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "fungal_spread",
                "Fungal Infection",
                "How is the rash spreading?",
                Arrays.asList("Not spreading", "Slowly spreading", "Rapidly spreading"),
                mapOf(
                        "Not spreading", 3,
                        "Slowly spreading", 8,
                        "Rapidly spreading", -10
                ),
                true
        ));

        allQuestions.add(new FollowUpQuestion(
                "fungal_pain",
                "Fungal Infection",
                "Is there severe pain or discharge?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", -12,
                        "No", 4
                ),
                true
        ));

        allQuestions.add(new FollowUpQuestion(
                "scabies_itching_night",
                "Scabies",
                "Does the itching become worse at night?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", 15,
                        "No", -10
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "scabies_household",
                "Scabies",
                "Do other family members also have itching or similar rash?",
                Arrays.asList("Yes", "No", "Not sure"),
                mapOf(
                        "Yes", 15,
                        "No", -5,
                        "Not sure", 0
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "scabies_burrows",
                "Scabies",
                "Are there tiny lines, bumps, or burrow-like marks on skin folds?",
                Arrays.asList("Yes", "No", "Not sure"),
                mapOf(
                        "Yes", 12,
                        "No", -8,
                        "Not sure", 0
                ),
                false
        ));

        allQuestions.add(new FollowUpQuestion(
                "scabies_secondary",
                "Scabies",
                "Is there pus, open wounds, or signs of infection due to scratching?",
                Arrays.asList("Yes", "No"),
                mapOf(
                        "Yes", -8,
                        "No", 4
                ),
                true
        ));

        allQuestions.add(new FollowUpQuestion(
                "scabies_duration",
                "Scabies",
                "How long has the itching been present?",
                Arrays.asList("1-2 days", "3-7 days", "More than 1 week"),
                mapOf(
                        "1-2 days", 2,
                        "3-7 days", 8,
                        "More than 1 week", 10
                ),
                false
        ));
    }

    private Map<String, Integer> mapOf(Object... values) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], (Integer) values[i + 1]);
        }
        return map;
    }
}