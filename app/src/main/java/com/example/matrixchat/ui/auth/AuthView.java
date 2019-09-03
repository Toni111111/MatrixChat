package com.example.matrixchat.ui.auth;

import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.rest.model.login.Credentials;

public interface AuthView {
    void showRoomScreen();
    void initSession(HomeServerConnectionConfig hsConfig, Credentials credentials);
}
