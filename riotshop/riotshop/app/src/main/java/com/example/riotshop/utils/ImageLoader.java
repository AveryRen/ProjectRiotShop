package com.example.riotshop.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {
    
    /**
     * Load image from URL and set to ImageView
     * Converts localhost URLs to 10.0.2.2 for Android emulator
     * Adds Cloudinary transformations for optimal display
     */
    public static void loadImage(ImageView imageView, String imageUrl) {
        Log.d("ImageLoader", "=== loadImage called ===");
        Log.d("ImageLoader", "Input URL: " + imageUrl);
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.w("ImageLoader", "Image URL is null or empty");
            return;
        }
        
        if (imageView == null) {
            Log.w("ImageLoader", "ImageView is null");
            return;
        }
        
        // Convert URL for Android emulator
        String convertedUrl = convertUrlForEmulator(imageUrl);
        Log.d("ImageLoader", "Converted URL: " + convertedUrl);
        
        // Ensure ImageView is visible
        imageView.setVisibility(View.VISIBLE);
        
        // Try loading original URL first (simpler, more reliable)
        // Only add transformations if needed for performance
        final String urlToLoad = convertedUrl;
        
        Log.d("ImageLoader", "Attempting to load with Picasso: " + urlToLoad);
        
        // Try Picasso first - load without resize first for better compatibility
        try {
            com.squareup.picasso.Picasso.get()
                .load(urlToLoad)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("ImageLoader", "✓ Picasso loaded image successfully from: " + urlToLoad);
                        // Ensure ImageView is visible after successful load
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setVisibility(View.VISIBLE);
                                imageView.requestLayout();
                                imageView.invalidate();
                                Log.d("ImageLoader", "ImageView updated and invalidated");
                            }
                        });
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        Log.e("ImageLoader", "✗ Picasso failed for URL: " + urlToLoad);
                        Log.e("ImageLoader", "Error type: " + e.getClass().getName());
                        Log.e("ImageLoader", "Error message: " + e.getMessage());
                        if (e.getCause() != null) {
                            Log.e("ImageLoader", "Cause: " + e.getCause().getMessage());
                        }
                        e.printStackTrace();
                        
                        // Try with transformations if it's a Cloudinary URL
                        if (urlToLoad.contains("cloudinary.com") && !urlToLoad.contains("/upload/w_")) {
                            String transformedUrl = addCloudinaryTransformations(urlToLoad, imageView);
                            if (transformedUrl != null && !transformedUrl.equals(urlToLoad)) {
                                Log.d("ImageLoader", "Retrying with transformed URL: " + transformedUrl);
                                com.squareup.picasso.Picasso.get()
                                    .load(transformedUrl)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .into(imageView, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d("ImageLoader", "✓ Picasso loaded transformed URL successfully");
                                            imageView.setVisibility(View.VISIBLE);
                                        }
                                        
                                        @Override
                                        public void onError(Exception e2) {
                                            Log.e("ImageLoader", "✗ Picasso failed for transformed URL too");
                                            Log.e("ImageLoader", "Falling back to AsyncTask");
                                            new LoadImageTask(imageView).execute(urlToLoad);
                                        }
                                    });
                            } else {
                                // Fallback to AsyncTask
                                Log.e("ImageLoader", "Falling back to AsyncTask");
                                new LoadImageTask(imageView).execute(urlToLoad);
                            }
                        } else {
                            // Fallback to AsyncTask
                            Log.e("ImageLoader", "Falling back to AsyncTask");
                            new LoadImageTask(imageView).execute(urlToLoad);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e("ImageLoader", "✗ Picasso exception: " + e.getMessage(), e);
            e.printStackTrace();
            // Fallback to AsyncTask
            Log.e("ImageLoader", "Falling back to AsyncTask due to exception");
            new LoadImageTask(imageView).execute(urlToLoad);
        }
    }
    
    /**
     * Add Cloudinary transformations to optimize image for display
     * Adds width/height limits and quality settings
     * Format: https://res.cloudinary.com/{cloud_name}/image/upload/{transformations}/{version}/{path}
     */
    private static String addCloudinaryTransformations(String url, ImageView imageView) {
        if (!url.contains("cloudinary.com") || !url.contains("/upload/")) {
            return url;
        }
        
        // Get screen width for full-width display
        // For small phone emulator, use full width (typically 360-480dp)
        // Convert to pixels: 360dp * 2-3 density = 720-1080px
        // Use 800px width to match resized image, height auto for aspect ratio
        int targetWidth = 800; // Match resized image width
        int targetHeight = 450; // 16:9 aspect ratio
        
        // Check if URL already has transformations (contains comma or w_, h_, etc.)
        if (url.contains("/upload/w_") || url.contains("/upload/h_") || url.contains("/upload/c_")) {
            // Already has transformations, return as is
            Log.d("ImageLoader", "URL already has transformations: " + url);
            return url;
        }
        
        try {
            // Parse Cloudinary URL structure
            // Format: https://res.cloudinary.com/{cloud}/image/upload/v{version}/{path}
            // Or: https://res.cloudinary.com/{cloud}/image/upload/{path} (no version)
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                Log.w("ImageLoader", "URL does not contain /upload/, returning original: " + url);
                return url;
            }
            
            // baseUrl includes "/upload/" at the end (8 characters: "/upload/")
            String baseUrl = url.substring(0, uploadIndex + 8); // Everything up to and including "/upload/"
            String afterUpload = url.substring(uploadIndex + 8); // Everything after "/upload/"
            
            Log.d("ImageLoader", "Base URL: " + baseUrl);
            Log.d("ImageLoader", "After upload: " + afterUpload);
            
            if (afterUpload == null || afterUpload.isEmpty()) {
                Log.w("ImageLoader", "Nothing after /upload/, returning original: " + url);
                return url;
            }
            
            // Build transformations string
            // Use c_limit instead of c_fill to maintain aspect ratio better
            String transformations = "w_" + targetWidth + ",h_" + targetHeight + ",c_limit,q_auto,f_auto";
            
            // Build transformed URL: baseUrl + transformations + "/" + afterUpload
            String transformedUrl = baseUrl + transformations + "/" + afterUpload;
            
            Log.d("ImageLoader", "Original URL: " + url);
            Log.d("ImageLoader", "Transformed URL: " + transformedUrl);
            
            return transformedUrl;
            
        } catch (Exception e) {
            Log.e("ImageLoader", "Error adding Cloudinary transformations: " + e.getMessage(), e);
            Log.e("ImageLoader", "Returning original URL due to error: " + url);
            return url; // Return original URL on error
        }
    }
    
    /**
     * Convert localhost URLs to 10.0.2.2 for Android emulator
     */
    private static String convertUrlForEmulator(String url) {
        if (url == null || url.isEmpty()) return url;
        
        String originalUrl = url;
        
        // Replace localhost with 10.0.2.2 for emulator
        if (url.contains("localhost")) {
            url = url.replace("http://localhost:", "http://10.0.2.2:");
            url = url.replace("https://localhost:", "http://10.0.2.2:");
        }
        if (url.contains("127.0.0.1")) {
            url = url.replace("http://127.0.0.1:", "http://10.0.2.2:");
            url = url.replace("https://127.0.0.1:", "http://10.0.2.2:");
        }
        
        Log.d("ImageLoader", "Original URL: " + originalUrl);
        Log.d("ImageLoader", "Converted URL: " + url);
        return url;
    }
    
    /**
     * AsyncTask to load image using HttpURLConnection as fallback
     */
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        
        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }
        
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                String imageUrl = urls[0];
                Log.d("ImageLoader", "Loading image from URL (AsyncTask): " + imageUrl);
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(10000); // 10 seconds
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                Log.d("ImageLoader", "HTTP Response Code: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();
                    if (bitmap != null) {
                        Log.d("ImageLoader", "Bitmap loaded: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    } else {
                        Log.e("ImageLoader", "Bitmap is null after decoding");
                    }
                    return bitmap;
                } else {
                    Log.e("ImageLoader", "HTTP Error: " + responseCode);
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        // Read error stream for debugging
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(errorStream));
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        Log.e("ImageLoader", "Error response: " + errorResponse.toString());
                    }
                    return null;
                }
            } catch (Exception e) {
                Log.e("ImageLoader", "Error loading image: " + e.getMessage(), e);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
                Log.d("ImageLoader", "Image loaded successfully using AsyncTask");
            } else {
                Log.e("ImageLoader", "Failed to load image - bitmap is null or imageView is null");
            }
        }
    }
}
