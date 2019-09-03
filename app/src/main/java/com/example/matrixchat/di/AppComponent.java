package com.example.matrixchat.di;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    LocalStorage localStorage();
}
