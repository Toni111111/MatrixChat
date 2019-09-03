package com.example.matrixchat.di;


import io.reactivex.Observable;
import org.matrix.androidsdk.rest.model.login.Credentials;

public interface LocalStorage {
    void writeMessage(String key,String value);
    Observable<String> readMessage(String key);

    void saveCredentials(Credentials credentials);
    Credentials getCredentials();
}
