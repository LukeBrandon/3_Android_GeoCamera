package dev.lukeb.geocamera.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import dev.lukeb.geocamera.R;
import dev.lukeb.geocamera.contract.MainActivityContract;
import dev.lukeb.geocamera.presenter.MainActivityPresenter;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {

    MainActivityPresenter mainActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivityPresenter = new MainActivityPresenter(this);

        initComponents();
    }

    public void initComponents(){

    }
}
