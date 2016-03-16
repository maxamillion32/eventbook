package com.mhafizhasan.eventbook.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.soundcloud.android.crop.Crop;

import java.io.File;

/**
 * Created by someguy233 on 05-Nov-15.
 */
public class PictureRequest {
    static final String TAG = "PictureRequest";

    private static PictureRequest activeRequest = null;
    private static int iteration = 1;

    public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if(activeRequest == null)
            return false;
        return activeRequest.processActivityResult(activity, requestCode, resultCode, data);
    }

    private final Activity activity;
    private final int aspectX;
    private final int aspectY;
    private final int maxWidth;
    private final int maxHeight;

    private interface Transforms {
        void resizeImage(ResizeImageRequest image);
    }

    private class TransformCallbacks implements Transforms {
        @Override
        public void resizeImage(ResizeImageRequest image) {
            if(image.getError() != null)
                onCanceled();           // Assume on cancelled
            else
                onPictureSelected(image.getOutput());
        }
    }
    private final Transforms transforms;


    public PictureRequest(Activity activity, CallChannel channel, int aspectX, int aspectY, int maxWidth, int maxHeight) {
        this.activity = activity;
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        // Transforms
        transforms = channel.transform(Transforms.class, new TransformCallbacks());
    }

    public void start() {
        activeRequest = this;
        Crop.pickImage(activity);
    }


    public boolean processActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Crop.REQUEST_PICK:
                if (resultCode != Activity.RESULT_OK) {
                    // Didn't select any picture
                    activeRequest = null;
                    onCanceled();
                    return true;
                }
                // Else selected picture, crop now if needed
                // Get output URI
                iteration++;
                Uri output = Uri.fromFile(new File(activity.getCacheDir(), "selected_" + iteration + ".jpg"));
                if (aspectX != -1 && aspectY != -1) {
                    Crop crop = Crop.of(data.getData(), output);
                    crop.withAspect(aspectX, aspectY);
                    crop.start(activity);
                    return true;
                }
                // Else just request resize
                transforms.resizeImage(new ResizeImageRequest(activity, data.getData(), output, maxWidth, maxHeight));
                activeRequest = null;       // done
                return true;

            case Crop.REQUEST_CROP:
                // Done cropping
                activeRequest = null;
                if (resultCode != Activity.RESULT_OK) {
                    onCanceled();
                    return true;        // canceled or error
                }
                // Done cropping
                output = Crop.getOutput(data);
                transforms.resizeImage(new ResizeImageRequest(activity, output, output, maxWidth, maxHeight));
                return true;

            default:
                return false;
        }
    }

    protected void onPictureSelected(Uri uri) {
        // implementation
    }

    protected void onCanceled() {
        // implementation
    }
}
