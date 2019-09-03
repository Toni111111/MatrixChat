package com.example.matrixchat.di;

import com.example.matrixchat.MyApplication;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AppModule {

    private MyApplication app;

    public AppModule(MyApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public MyApplication provideApp() {
        return app;
    }

    @Provides
    @Singleton
    public LocalStorage provideLocalStorage(MyApplication context){
        return new SharedPrefStorage(context);
}
}
