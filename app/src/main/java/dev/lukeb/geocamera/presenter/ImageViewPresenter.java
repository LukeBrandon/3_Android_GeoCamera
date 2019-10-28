package dev.lukeb.geocamera.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

import dev.lukeb.geocamera.contract.ImageViewContract;
import dev.lukeb.geocamera.view.MainActivity;

public class ImageViewPresenter implements ImageViewContract.Presenter {
    private static final String TAG = "ImageViewPresenter";

    Context context;
    ImageViewContract.View imageViewActivityView;

    public ImageViewPresenter(Context context, ImageViewContract.View view) {
        this.context = context;
        this.imageViewActivityView = view;
    }

    /*
     * Retrieves the image form storage and sends it back to the view as a bitmap to be displayed
     */
    public Bitmap getImageFromStorage(Intent intent, int targetW, int targetH) {
        String imagePath = null;
        if (intent.hasExtra(MainActivity.PHOTO_PATH)) {
            imagePath = intent.getStringExtra(MainActivity.PHOTO_PATH);
        }

        if (imagePath != null) {

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            // Used to rotate the image upright
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap bitmap =  BitmapFactory.decodeFile(imagePath, bmOptions);

            // Rotate upright
            Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            return finalBitmap;
        }
        return null;
    }
}
