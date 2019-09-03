package com.example.matrixchat.ui.auth;

import com.example.matrixchat.repository.auth.AuthRepository;
import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.rest.model.login.Credentials;

public class AuthPresenter {
    AuthRepository authRepository;
    AuthView authView;

    public AuthPresenter(AuthView authView) {
        this.authRepository = new AuthRepository(this);
        this.authView = authView;
    }

    public void isAuth(){
        authRepository.isAuth();
    }

    public void auth(String login,String password){
        authRepository.auth(login,password);
    }

    public void showChatScreen(){
        authView.showRoomScreen();
    }

    public void initSession(HomeServerConnectionConfig hsConfig, Credentials credentials){
            authView.initSession(hsConfig,credentials);
    }
}
