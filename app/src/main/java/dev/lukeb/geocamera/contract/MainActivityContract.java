package dev.lukeb.geocamera.contract;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public interface MainActivityContract {

    interface View {

    }

    interface Presenter {
        ArrayList<MarkerOptions> getMarkersFromPhotos();

        void savePhotoMetadata(String currentPhotoPath, double lat, double lon);

        void clearPhotos();
    }
}
