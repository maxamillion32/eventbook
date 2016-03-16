package com.mhafizhasan.eventbook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by someguy233 on 07-Nov-15.
 */
public class ResizeImageRequest implements Runnable {

    private final Context context;
    private final Uri input;
    private final Uri output;
    private final int maxWidth;
    private final int maxHeight;

    private int width = -1;
    private int height = -1;
    private Throwable e = null;

    public Uri getOutput() {
        return e != null ? null : output;
    }

    public Throwable getError() {
        return e;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ResizeImageRequest(Context context, Uri input, Uri output, int maxWidth, int maxHeight) {
        this.context = context;
        this.input = input;
        this.output = output;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public void run() {
        try {
            // Decode image size
            BufferedInputStream inputStream = new BufferedInputStream(context.getContentResolver().openInputStream(input));
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                width = options.outWidth;
                height = options.outHeight;
                if (width <= 0 || height <= 0)
                    throw new RuntimeException("Unable to decode file: " + input.getPath());
            } finally {
                inputStream.close();
            }

            // Decode to approximately sized bitmap
            Bitmap bitmap;
            inputStream = new BufferedInputStream(context.getContentResolver().openInputStream(input));
            if(maxWidth > 0 && maxHeight > 0 && (maxWidth < width || maxHeight < height)) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = Math.max(width / maxWidth, height / maxHeight);
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    if (bitmap == null)
                        throw new RuntimeException("Unable to decode file: " + input.getPath());
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                } finally {
                    inputStream.close();
                }

                // Resize to exact size if needed
                float refactorSize = 1;
                if (width > maxWidth) {
                    float factor = (float) width / (float) maxWidth;
                    if (factor > refactorSize)
                        refactorSize = factor;
                }
                if (height > maxHeight) {
                    float factor = (float) height / (float) maxHeight;
                    if (factor > refactorSize)
                        refactorSize = factor;
                }
                if (refactorSize > 1) {
                    // Resize needed
                    width = Math.round((float) width / refactorSize);
                    height = Math.round((float) height / refactorSize);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    bitmap.recycle();
                    bitmap = scaledBitmap;
                }
            }
            else {
                // Else image is smaller than expected
                try {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null)
                        throw new RuntimeException("Unable to decode file: " + input.getPath());
                } finally {
                    inputStream.close();
                }
            }

            // Save to file
            BufferedOutputStream outputStream = new BufferedOutputStream(context.getContentResolver().openOutputStream(output));
            try {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream))
                    throw new RuntimeException("Unable to save file: " + output.getPath());
                outputStream.flush();
            } finally {
                outputStream.close();
            }

            bitmap.recycle();
        } catch (Throwable e) {
            this.e = e;
        }
    }
}
