package com.sehatsaathi.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RagApiService {
    @POST("/rag/chat")
    Call<RagModels.ChatResponse> chat(@Body RagModels.ChatRequest request);
}
