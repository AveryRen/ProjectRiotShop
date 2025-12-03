package com.example.riotshop.api;

import com.example.riotshop.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static RetrofitClient instance;
    private ApiService apiService;
    private Retrofit retrofit;

    private RetrofitClient() {
        // Logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // UTF-8 encoding interceptor
        okhttp3.Interceptor utf8Interceptor = chain -> {
            okhttp3.Request originalRequest = chain.request();
            okhttp3.Request.Builder requestBuilder = originalRequest.newBuilder();
            
            // Set Accept header with UTF-8
            String acceptHeader = originalRequest.header("Accept");
            if (acceptHeader == null || !acceptHeader.contains("charset")) {
                requestBuilder.header("Accept", "application/json; charset=utf-8");
            }
            requestBuilder.header("Accept-Charset", "utf-8");
            
            // Ensure Content-Type has charset=utf-8 if it's JSON
            String contentType = originalRequest.header("Content-Type");
            if (contentType != null && contentType.contains("application/json") && !contentType.contains("charset")) {
                requestBuilder.header("Content-Type", contentType + "; charset=utf-8");
            }
            
            return chain.proceed(requestBuilder.build());
        };

        // OkHttpClient (AuthInterceptor will be added per-request if needed)
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(utf8Interceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .build();

        // Gson
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
    
    public static String BASE_URL = Constants.BASE_URL;
}

