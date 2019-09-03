package com.example.matrixchat.di;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.matrix.androidsdk.rest.model.login.Credentials;


public class SharedPrefStorage implements LocalStorage {

    private Context context;

    public SharedPrefStorage(Context context) {
        this.context = context;
    }

    @Override
    public void writeMessage(String key,String value) {
        context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
                .edit().putString(key, value).apply();
    }

    @Override
    public Observable<String> readMessage(String key) {
        return Observable.fromCallable(() -> context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
                .getString(key, ""));
    }

    @Override
    public void saveCredentials(Credentials credentials){
       SharedPreferences.Editor prefsEditor =  context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(credentials);
        prefsEditor.putString("Credentials", json);
        prefsEditor.commit();
    }

    @Override
    public Credentials getCredentials(){
        Gson gson = new Gson();
        String json = context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE).getString("Credentials", "");
        Credentials credentials = gson.fromJson(json, Credentials.class);
        return credentials;
    }

}
