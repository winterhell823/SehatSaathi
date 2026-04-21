package com.sehatsaathi.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // Change this to your backend IP when testing on a real device.
    // For Android Emulator: use http://10.0.2.2:8000
    // For real device on same Wi-Fi: use your machine's local IP e.g. http://192.168.1.5:8000
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static RagApiService instance;

    public static RagApiService getService() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            instance = retrofit.create(RagApiService.class);
        }
        return instance;
    }
}
