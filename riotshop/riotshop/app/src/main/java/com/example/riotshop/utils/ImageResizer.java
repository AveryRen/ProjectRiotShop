package com.example.riotshop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Utility class to resize and crop images before upload
 * Optimized for small phone emulator (target: 800x450 or 16:9 aspect ratio)
 */
public class ImageResizer {
    
    private static final String TAG = "ImageResizer";
    
    // Target dimensions for product images (16:9 aspect ratio)
    private static final int TARGET_WIDTH = 800;
    private static final int TARGET_HEIGHT = 450;
    private static final int MAX_FILE_SIZE_KB = 500; // Max 500KB
    
    /**
     * Resize and compress image from URI
     * @param context Context
     * @param imageUri Original image URI
     * @return Resized and compressed image file, or null if error
     */
    public static File resizeAndCompressImage(Context context, Uri imageUri) {
        try {
            // Read original image
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Cannot open input stream from URI");
                return null;
            }
            
            // Decode with options to get dimensions first
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            Log.d(TAG, "Original image size: " + originalWidth + "x" + originalHeight);
            
            // Calculate sample size to reduce memory usage
            int sampleSize = calculateSampleSize(originalWidth, originalHeight, TARGET_WIDTH, TARGET_HEIGHT);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory
            
            // Decode image with sample size
            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap");
                return null;
            }
            
            Log.d(TAG, "Decoded bitmap size: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
            
            // Calculate crop dimensions (center crop to maintain aspect ratio)
            Bitmap croppedBitmap = centerCrop(originalBitmap, TARGET_WIDTH, TARGET_HEIGHT);
            originalBitmap.recycle(); // Free memory
            
            if (croppedBitmap == null) {
                Log.e(TAG, "Failed to crop bitmap");
                return null;
            }
            
            Log.d(TAG, "Cropped bitmap size: " + croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
            
            // Compress and save to temp file
            File tempFile = File.createTempFile("resized_upload", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            int quality = 85;
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.close();
            
            // Check file size and reduce quality if needed
            long fileSize = tempFile.length() / 1024; // Size in KB
            Log.d(TAG, "Compressed file size: " + fileSize + " KB");
            
            if (fileSize > MAX_FILE_SIZE_KB) {
                // Recompress with lower quality
                quality = 70;
                outputStream = new FileOutputStream(tempFile);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.close();
                fileSize = tempFile.length() / 1024;
                Log.d(TAG, "Recompressed file size: " + fileSize + " KB");
            }
            
            croppedBitmap.recycle(); // Free memory
            
            Log.d(TAG, "Image resized and saved to: " + tempFile.getAbsolutePath());
            return tempFile;
            
        } catch (Exception e) {
            Log.e(TAG, "Error resizing image: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Calculate sample size for bitmap decoding to reduce memory usage
     */
    private static int calculateSampleSize(int originalWidth, int originalHeight, int targetWidth, int targetHeight) {
        int sampleSize = 1;
        
        if (originalHeight > targetHeight || originalWidth > targetWidth) {
            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;
            
            while ((halfHeight / sampleSize) >= targetHeight && (halfWidth / sampleSize) >= targetWidth) {
                sampleSize *= 2;
            }
        }
        
        return sampleSize;
    }
    
    /**
     * Center crop bitmap to target dimensions
     */
    private static Bitmap centerCrop(Bitmap source, int targetWidth, int targetHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        
        // Calculate scale to fill target dimensions
        float scaleX = (float) targetWidth / sourceWidth;
        float scaleY = (float) targetHeight / sourceHeight;
        float scale = Math.max(scaleX, scaleY); // Use max to fill
        
        // Calculate scaled dimensions
        int scaledWidth = Math.round(sourceWidth * scale);
        int scaledHeight = Math.round(sourceHeight * scale);
        
        // Create scaled bitmap
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, sourceWidth, sourceHeight, matrix, true);
        
        // Calculate crop position (center)
        int cropX = (scaledWidth - targetWidth) / 2;
        int cropY = (scaledHeight - targetHeight) / 2;
        
        // Ensure crop coordinates are valid
        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        
        // Ensure we don't exceed bitmap bounds
        if (cropX + targetWidth > scaledWidth) {
            cropX = scaledWidth - targetWidth;
        }
        if (cropY + targetHeight > scaledHeight) {
            cropY = scaledHeight - targetHeight;
        }
        
        // Create cropped bitmap
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, cropX, cropY, targetWidth, targetHeight);
        
        // Recycle scaled bitmap if different from source
        if (scaledBitmap != source) {
            scaledBitmap.recycle();
        }
        
        return croppedBitmap;
    }
}

