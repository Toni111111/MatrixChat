package com.example.matrixchat.repository.auth;

import android.net.Uri;
import android.util.Log;
import com.example.matrixchat.Credentialses;
import com.example.matrixchat.MyApplication;
import com.example.matrixchat.di.LocalStorage;
import com.example.matrixchat.di.SharedPrefStorage;
import com.example.matrixchat.ui.auth.AuthPresenter;
import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.model.login.Credentials;

import javax.inject.Inject;

public class AuthRepository {

    private AuthPresenter authPresenter;

    @Inject
    SharedPrefStorage storage;

    LocalStorage pref;



    public AuthRepository(AuthPresenter presenter) {
        this.authPresenter = presenter;
        pref = MyApplication.getComponent().localStorage();
    }

    public void isAuth(){
            if(pref.getCredentials()!=null){
                authPresenter.showChatScreen();
            }
    }

   public void auth(String login, String password){
        HomeServerConnectionConfig hsConfig = new HomeServerConnectionConfig.Builder()
                .withHomeServerUri(Uri.parse("https://matrix.org"))
                .build();

        new LoginRestClient(hsConfig).loginWithUser(login, password, new org.matrix.androidsdk.core.callback.ApiCallback<Credentials>() {

            @Override
            public void onSuccess(Credentials info) {
                Credentialses.INSTANCE.setCreditntialss(info);
                authPresenter.initSession(hsConfig,info);
                authPresenter.showChatScreen();
                pref.writeMessage("userId",info.userId);
               Credentialses.INSTANCE.setUserId(info.userId);

               pref.saveCredentials(info);


                Log.d("result:","auth Success");
            }

            @Override
            public void onUnexpectedError(Exception e) {
                Log.d("result:","auth onUnexpectedError");
            }

            @Override
            public void onNetworkError(Exception e) {
                Log.d("result:","auth onNetworkError");
            }

            @Override
            public void onMatrixError(MatrixError e) {
                Log.d("result:","auth onMatrixError");
            }
        });
    }
}
