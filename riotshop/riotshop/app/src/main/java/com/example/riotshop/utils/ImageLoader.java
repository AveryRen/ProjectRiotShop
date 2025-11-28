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
        
        // Try to add Cloudinary transformations, but fallback to original if it fails
        final String finalUrl;
        if (convertedUrl.contains("cloudinary.com")) {
            String transformedUrl = addCloudinaryTransformations(convertedUrl, imageView);
            // If transformation failed or returned same URL, use original
            if (transformedUrl != null && !transformedUrl.equals(convertedUrl)) {
                finalUrl = transformedUrl;
            } else {
                // Use original URL if transformation failed
                Log.w("ImageLoader", "Transformation may have failed, using original URL");
                finalUrl = convertedUrl;
            }
        } else {
            finalUrl = convertedUrl;
        }
        
        // Ensure ImageView is visible
        imageView.setVisibility(View.VISIBLE);
        
        // Try Picasso first
        try {
            Log.d("ImageLoader", "Loading image with Picasso: " + finalUrl);
            
            // Get ImageView dimensions for proper sizing
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    int width = imageView.getWidth();
                    int height = imageView.getHeight();
                    Log.d("ImageLoader", "ImageView dimensions: " + width + "x" + height);
                }
            });
            
            com.squareup.picasso.Picasso.get()
                .load(finalUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .resize(800, 450) // Match resized image dimensions
                .centerCrop()
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("ImageLoader", "Picasso loaded image successfully from: " + finalUrl);
                        // Ensure ImageView is visible after successful load
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setVisibility(View.VISIBLE);
                                // Force layout to ensure ImageView displays correctly
                                imageView.requestLayout();
                                imageView.invalidate();
                                Log.d("ImageLoader", "ImageView visibility set to VISIBLE and layout requested");
                            }
                        });
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        Log.e("ImageLoader", "Picasso failed for URL: " + finalUrl);
                        Log.e("ImageLoader", "Error: " + e.getMessage(), e);
                        
                        // If transformed URL failed, try original URL
                        if (finalUrl.contains("cloudinary.com") && !finalUrl.equals(convertedUrl)) {
                            Log.d("ImageLoader", "Trying original URL without transformations: " + convertedUrl);
                            com.squareup.picasso.Picasso.get()
                                .load(convertedUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .resize(800, 450) // Match resized image dimensions
                                .centerCrop()
                                .into(imageView, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("ImageLoader", "Picasso loaded original URL successfully");
                                        imageView.setVisibility(View.VISIBLE);
                                    }
                                    
                                    @Override
                                    public void onError(Exception e2) {
                                        Log.e("ImageLoader", "Picasso failed for original URL too, using AsyncTask fallback");
                                        new LoadImageTask(imageView).execute(convertedUrl);
                                    }
                                });
                        } else {
                            // Fallback to OkHttp
                            new LoadImageTask(imageView).execute(finalUrl);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e("ImageLoader", "Picasso exception: " + e.getMessage(), e);
            // Fallback to OkHttp
            new LoadImageTask(imageView).execute(finalUrl);
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
            String transformations = "w_" + targetWidth + ",h_" + targetHeight + ",c_fill,q_auto,f_auto";
            
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
