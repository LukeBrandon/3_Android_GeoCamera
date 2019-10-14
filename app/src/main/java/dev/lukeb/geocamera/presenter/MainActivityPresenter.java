package dev.lukeb.geocamera.presenter;

import dev.lukeb.geocamera.contract.MainActivityContract;

public class MainActivityPresenter implements MainActivityContract.Presenter{

    MainActivityContract.View mainActivityView;

    public MainActivityPresenter(MainActivityContract.View view){
        this.mainActivityView = view;
    }

}
