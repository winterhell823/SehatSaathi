package com.sehatsaathi.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Local heuristic fallback when backend RAG is unreachable.
 */
public final class LocalRagEngine {

    private LocalRagEngine() {
    }

    public static RagModels.ChatResponse initialResponse(String symptomText) {
        RagModels.ChatResponse response = new RagModels.ChatResponse();
        response.status = "follow_up";
        response.predictedDisease = inferConditionName(symptomText);
        response.initialConfidence = "62";
        response.questionNumber = 1;
        response.message = "Server unavailable. Continuing with local analysis.";
        response.followUpQuestion = "Is there fever or body ache?";
        return response;
    }

    public static RagModels.ChatResponse nextResponse(String symptomText, List<Object> history) {
        int followUpAnswers = Math.max(0, countUserTurns(history) - 1);

        if (followUpAnswers == 1) {
            RagModels.ChatResponse response = new RagModels.ChatResponse();
            response.status = "follow_up";
            response.questionNumber = 2;
            response.message = "Do symptoms worsen at night or in cold weather?";
            return response;
        }

        if (followUpAnswers == 2) {
            RagModels.ChatResponse response = new RagModels.ChatResponse();
            response.status = "follow_up";
            response.questionNumber = 3;
            response.message = "Any red-flag signs like breathlessness, chest pain, persistent vomiting, or confusion?";
            return response;
        }

        return buildDiagnosedResponse(symptomText, history);
    }

    private static RagModels.ChatResponse buildDiagnosedResponse(String symptomText, List<Object> history) {
        String condition = inferConditionName(symptomText);
        int confidence = inferConfidence(symptomText, history);

        RagModels.ChatResponse response = new RagModels.ChatResponse();
        response.status = "diagnosed";
        response.message = "Local analysis complete. Please verify clinically.";
        response.recommendedTreatment = treatmentFor(condition);
        response.referralNeeded = confidence < 60 || hasRedFlagsInHistory(history);
        response.referralReason = response.referralNeeded
                ? "Urgent symptoms or low confidence in offline mode. Clinical review recommended."
                : "No immediate red flags detected from provided answers.";

        RagModels.DiagnosedCondition diagnosed = new RagModels.DiagnosedCondition();
        diagnosed.name = condition;
        diagnosed.confidencePercentage = confidence;
        diagnosed.reasoning = "Generated using local fallback rules because network RAG was unavailable.";
        diagnosed.differentialDiagnoses = new ArrayList<>();
        diagnosed.differentialDiagnoses.add("Viral Upper Respiratory Infection");
        diagnosed.differentialDiagnoses.add("Seasonal Allergy");
        diagnosed.differentialDiagnoses.add("Acute Gastroenteritis");
        response.condition = diagnosed;

        response.precautions = new ArrayList<>();
        response.precautions.add("Maintain hydration and adequate rest.");
        response.precautions.add("Monitor temperature and symptom progression every 6-8 hours.");
        response.precautions.add("Use mask hygiene if cough/cold symptoms are present.");

        response.nextSteps = new ArrayList<>();
        response.nextSteps.add("Continue supportive care for 24-48 hours.");
        response.nextSteps.add("Visit nearest PHC if symptoms worsen.");
        response.nextSteps.add("Seek emergency care immediately if red flags appear.");

        response.redFlags = new ArrayList<>();
        response.redFlags.add("Severe breathlessness");
        response.redFlags.add("Persistent chest pain");
        response.redFlags.add("Altered consciousness");

        response.medications = new ArrayList<>();
        RagModels.Medication med = new RagModels.Medication();
        med.name = "Paracetamol";
        med.dose = "500 mg";
        med.duration = "3 days if needed";
        med.instructions = "After food; do not exceed advised daily limit.";
        response.medications.add(med);

        return response;
    }

    private static int countUserTurns(List<Object> history) {
        int count = 0;
        if (history == null) return 0;
        for (Object item : history) {
            if (item instanceof Map) {
                Object role = ((Map<?, ?>) item).get("role");
                if (role != null && "user".equalsIgnoreCase(String.valueOf(role))) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean hasRedFlagsInHistory(List<Object> history) {
        if (history == null) return false;
        for (Object item : history) {
            if (item instanceof Map) {
                Object content = ((Map<?, ?>) item).get("content");
                if (content == null) continue;
                String text = String.valueOf(content).toLowerCase(Locale.ROOT);
                if (text.contains("chest pain") || text.contains("breath") || text.contains("confusion") || text.contains("vomit")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int inferConfidence(String symptomText, List<Object> history) {
        String s = safeLower(symptomText);
        int confidence = 58;
        if (s.contains("fever") || s.contains("cough") || s.contains("cold") || s.contains("throat")) confidence += 10;
        if (s.contains("rash") || s.contains("allergy") || s.contains("itch")) confidence += 8;
        if (s.contains("vomit") || s.contains("diarrhea") || s.contains("loose motion")) confidence += 8;
        if (hasRedFlagsInHistory(history)) confidence -= 15;
        if (confidence < 35) confidence = 35;
        if (confidence > 88) confidence = 88;
        return confidence;
    }

    private static String inferConditionName(String symptomText) {
        String s = safeLower(symptomText);
        if (s.contains("rash") || s.contains("itch") || s.contains("skin")) {
            return "Allergic Dermatitis";
        }
        if (s.contains("vomit") || s.contains("diarrhea") || s.contains("stomach") || s.contains("abdomen")) {
            return "Acute Gastroenteritis";
        }
        if (s.contains("cough") || s.contains("cold") || s.contains("throat") || s.contains("fever")) {
            return "Viral Upper Respiratory Infection";
        }
        return "General Viral Syndrome";
    }

    private static String treatmentFor(String condition) {
        if ("Allergic Dermatitis".equals(condition)) {
            return "Use soothing topical care, avoid triggers, and take anti-allergic medication if prescribed.";
        }
        if ("Acute Gastroenteritis".equals(condition)) {
            return "Use ORS, maintain hydration, light diet, and monitor for dehydration signs.";
        }
        if ("Viral Upper Respiratory Infection".equals(condition)) {
            return "Symptomatic care with hydration, steam inhalation, rest, and fever control medication if needed.";
        }
        return "Supportive care with hydration, rest, and close symptom monitoring.";
    }

    private static String safeLower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
