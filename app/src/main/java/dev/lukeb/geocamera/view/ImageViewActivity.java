package dev.lukeb.geocamera.view;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import dev.lukeb.geocamera.R;
import dev.lukeb.geocamera.contract.ImageViewContract;
import dev.lukeb.geocamera.presenter.ImageViewPresenter;

public class ImageViewActivity extends AppCompatActivity implements ImageViewContract.View {

    private static final String TAG = "ImageViewActivity";

    ImageViewPresenter presenter;

    ImageView imageView;
    Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        // Set backgroud to black
        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.black));

        this.presenter = new ImageViewPresenter(getApplicationContext(), this);

        // Init the UI components
        this.imageView = findViewById(R.id.imageView);
        this.doneButton = findViewById(R.id.done);

        // Done button cancels the activity
        this.doneButton.setOnClickListener(v -> {
            finish();
        });
    }

     /*
      * This method is called when the activity gets focus
      * This has to be called here because the imageView does not have its dimensions in onCreate
      */
     @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Intent intent = getIntent();
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        imageView.setImageBitmap(presenter.getImageFromStorage(intent, targetW, targetH));
    }

}

