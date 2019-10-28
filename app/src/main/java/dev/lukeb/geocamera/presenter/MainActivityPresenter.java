package dev.lukeb.geocamera.presenter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import dev.lukeb.geocamera.contract.MainActivityContract;

public class MainActivityPresenter implements MainActivityContract.Presenter{
    private static final String TAG = "MainActivityPresenter";

    Context context;
    MainActivityContract.View mainActivityView;

    public MainActivityPresenter(Context context, MainActivityContract.View view){
        this.context = context;
        this.mainActivityView = view;
    }

    /*
        * Method for creating all of the MarkerOptions that are going to be placed on the map
        * from all of the images in the directory for this app
     */
    public ArrayList<MarkerOptions> getMarkersFromPhotos(){
        Log.d(TAG, "getMarkersFromPhotos: Called getMarkerFromPhotos");
        ArrayList<MarkerOptions> markerOptions = new ArrayList<>();

        // Gets all of the image files in the folder
        File[] files = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        if (files != null) {

            // Loop through all of the image files and add them to the ArrayList or MarkerOptions
            for(int i = 0; i < files.length; i++) {

                // Init values for each image
                File currentImage = files[i];
                String photoPath = currentImage.getPath();
                //currentImage.delete();
                double[] latLong;
                LatLng loc = new LatLng(0.0, 0.0);
                Log.d(TAG, "getMarkersFromPhotos: Adding marker for photo: " + photoPath);

                // Get GPS Location from ExifInterface
                try {
                    ExifInterface exifInterface = new ExifInterface(photoPath);
                    latLong = exifInterface.getLatLong();
                    if(latLong != null)
                        loc = new LatLng(latLong[0], latLong[1]);

                    Log.d(TAG, "getMarkersFromPhotos: location from image file:  lat " + loc.latitude + " //  long " + loc.longitude);
                } catch (IOException e) {
                    Toast.makeText(context, "Could not load previous photo information", Toast.LENGTH_SHORT);
                }

                // Create MarkerOption
                MarkerOptions markerOption = new MarkerOptions()
                        .position(loc)
                        .snippet(photoPath);

                // Add to list
                markerOptions.add(markerOption);
            }

        }

        return markerOptions;
    }

    /*
        * Method for saving the GPS location as metadata of the photo using ExifInterface
     */
    public void savePhotoMetadata(String currentPhotoPath, double lat, double lon){
        try {

            ExifInterface exifInterface = new ExifInterface(currentPhotoPath);
            exifInterface.setLatLong(lat, lon);
            exifInterface.saveAttributes();

        } catch (IOException e){
            Toast.makeText(context, "Error writing metadata of photo", Toast.LENGTH_SHORT);
        }
    }

    /*
        * This method clears all of the photos from the directory
        *   - Mainly used for testing purposes
     */
    public void clearPhotos(){
        // Gets all of the image files in the folder
        File[] files = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        // Loop through all of the image files and add them to the ArrayList or MarkerOptions
        for(int i = 0; i < files.length; i++) {

            // Init values for each image
            File currentImage = files[i];
            currentImage.delete();
        }
    }

}
