package com.sehatsaathi.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RagModels {

    // ── Request ──────────────────────────────────────────────────────────────
    public static class ChatRequest {
        public String message;
        @SerializedName("conversation_history")
        public List<Object> conversationHistory;
        @SerializedName("patient_id")
        public String patientId;
        @SerializedName("initial_message")
        public String initialMessage;

        public ChatRequest(String message, List<Object> history, String patientId, String initialMessage) {
            this.message = message;
            this.conversationHistory = history;
            this.patientId = patientId;
            this.initialMessage = initialMessage;
        }
    }

    // ── Generic response wrapper (status can be initial / follow_up / diagnosed) ──
    public static class ChatResponse {
        public String status;
        public String message;

        // STATUS = initial
        @SerializedName("predicted_disease")
        public String predictedDisease;
        @SerializedName("initial_confidence")
        public String initialConfidence;
        @SerializedName("follow_up_question")
        public String followUpQuestion;
        @SerializedName("question_number")
        public int questionNumber;

        // STATUS = follow_up
        // (uses message and question_number above)

        // STATUS = diagnosed
        public DiagnosedCondition condition;
        @SerializedName("further_predictions")
        public String furtherPredictions;
        public List<String> precautions;
        @SerializedName("recommended_treatment")
        public String recommendedTreatment;
        public List<Medication> medications;
        @SerializedName("next_steps")
        public List<String> nextSteps;
        @SerializedName("referral_needed")
        public boolean referralNeeded;
        @SerializedName("referral_reason")
        public String referralReason;
        @SerializedName("red_flags")
        public List<String> redFlags;
        @SerializedName("follow_up_days")
        public int followUpDays;
    }

    public static class DiagnosedCondition {
        public String name;
        @SerializedName("confidence_percentage")
        public int confidencePercentage;
        public String reasoning;
        @SerializedName("differential_diagnoses")
        public List<String> differentialDiagnoses;
    }

    public static class Medication {
        public String name;
        public String dose;
        public String duration;
        public String instructions;
    }
}
