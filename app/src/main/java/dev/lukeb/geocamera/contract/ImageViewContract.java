package dev.lukeb.geocamera.contract;

import android.content.Intent;
import android.graphics.Bitmap;

public interface ImageViewContract {

    interface View {

    }

    interface Presenter {
        Bitmap getImageFromStorage(Intent intent, int targetW, int targetH);
    }
}


