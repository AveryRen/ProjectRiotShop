package com.example.riotshop.api;

import android.content.Context;

import com.example.riotshop.utils.SharedPrefManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Get token from SharedPreferences
        String token = null;
        if (context != null) {
            SharedPrefManager manager = SharedPrefManager.getInstance(context);
            token = manager.getToken();
        }
        
        // If token exists, add it to the request header
        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token);
            return chain.proceed(requestBuilder.build());
        }
        
        return chain.proceed(originalRequest);
    }
}

