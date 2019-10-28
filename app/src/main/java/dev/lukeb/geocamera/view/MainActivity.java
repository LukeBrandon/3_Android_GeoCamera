package dev.lukeb.geocamera.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import dev.lukeb.geocamera.R;
import dev.lukeb.geocamera.contract.MainActivityContract;
import dev.lukeb.geocamera.presenter.MainActivityPresenter;

public class MainActivity extends FragmentActivity implements MainActivityContract.View, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final String PHOTO_PATH = "photo_path";

    MainActivityPresenter presenter;

    private GoogleMap mMap;
    private Button pictureButton;
    private Button clearButton;
    private FusedLocationProviderClient mLocationClient;
    private Boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private String currentPhotoPath;
    private LatLng lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(checkLocationPermission()){
            createLocationRequest();
            mRequestingLocationUpdates = true;
        }
        setContentView(R.layout.activity_main);

        presenter = new MainActivityPresenter(getApplicationContext(), this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Method that is called when receives location updates
        //Saves the most recent location into the lastLocation variable
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                for(Location location : locationResult.getLocations()){
                    if(mMap != null){
                        lastLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
                    }
                }
            }
        };

        initUiElements();
    }

    /*
     * Initializes the UI elements of the Main Activity
     *  - Basically gets the buttons from View and sets on click listeners
     */
    private void initUiElements(){
        this.pictureButton = findViewById(R.id.pictureButton);
        this.clearButton = findViewById(R.id.clearButton);

        this.pictureButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(), "Failed to make image file", Toast.LENGTH_SHORT).show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "dev.lukeb.geocamera.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    // Starts the activity for result
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        this.clearButton.setOnClickListener(v -> {
            presenter.clearPhotos();
            this.refreshMap(null);
        });
    }

    /*
     * onResume should start the location updates
     */
    @Override
    protected void onResume(){
        super.onResume();

        if(mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    /*
     * Invoked when the camera activity is finished
     *  - Checks that the request succeded
     *  - Calls the save photo metadata method of presenter that applies location metadata
     *  - Calls method that creates marker on the map
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            presenter.savePhotoMetadata(currentPhotoPath, lastLocation.latitude, lastLocation.longitude);

            createMarkerAtLastLocation();
        }
    }

    /*
     * Force gets the current location and once the location is gotten, a marker is created on the map
     *  - Marker contains a snippet of the photo path so that when the marker is clicked the image can opened
     */
    private void createMarkerAtLastLocation(){
        mLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .snippet(currentPhotoPath)
                );
            }
        });
    }

    /*
     * Creates an image file in the folder for the photos of this app
     *  - This file is created temporarily and then when the camera activity on result is invoked
     *      then that is saved in the file that was created here
     */
    private File createImageFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",    /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            this.currentPhotoPath = image.getAbsolutePath();
            return image;
    }

    /*
     * Tells the mLocationClient to start requesting location updates
     *  - At every interval specified in mLocationRequest, the mLocationCallBack is called
     */
    private void startLocationUpdates(){
        mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,null);
    }

    /*
     * Specifies the details of the location updates request
     *  - Interval is how often the location is gotten
     */
    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /*
     * Callback for when the map is ready
     *  - Gets all of the photos from storage and creates their markers
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                lastLocation = loc;
                mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            }
        });

        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(30);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);

        // Refreshes the map with all of the markers that should be on the map
        this.refreshMap(presenter.getMarkersFromPhotos());
    }

    /*
     * Called when a marker is clicked on
     *  - Opens the ImageViewActivity with the photo path that is on the snippet of the marker
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(PHOTO_PATH, marker.getSnippet());
        startActivity(intent);

        return true;
    }

    /*
     * Clears and sets all of the markers on the map
     */
    private void refreshMap(ArrayList<MarkerOptions> markers){
        mMap.clear();

        if(markers != null) {
            Iterator<MarkerOptions> it = markers.iterator();
            while (it.hasNext()) {
                mMap.addMarker(it.next());
            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    /*
     * Checks if permissions have been granted by the user
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                } else {
                    Toast.makeText(this, "You shouldn't be using this app if you aren't going to allow location permissions", Toast.LENGTH_LONG);
                }
                return;
            }

        }
    }
}
