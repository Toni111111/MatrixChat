package com.example.matrixchat.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.matrixchat.Credentialses;
import com.example.matrixchat.Matrix;
import com.example.matrixchat.MyApplication;
import com.example.matrixchat.R;
import com.example.matrixchat.di.LocalStorage;
import com.example.matrixchat.di.SharedPrefStorage;
import com.example.matrixchat.ui.room.RoomActivity;
import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.rest.model.login.Credentials;

public class AuthActivity extends AppCompatActivity implements AuthView {
    AuthPresenter auth = new AuthPresenter(this);
    EditText login;
    EditText password;
    Button authBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);
        login = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        authBtn = findViewById(R.id.authBtn);
        authBtn.setOnClickListener(v -> auth.auth(login.getText().toString(), password.getText().toString()));
        auth.isAuth();
    }

    @Override
    public void showRoomScreen() {
        Intent intentRoom = new Intent(this, RoomActivity.class);
        startActivity(intentRoom);
    }

    @Override
    public void initSession(HomeServerConnectionConfig hsConfig, Credentials credentials) {
        hsConfig.setCredentials(Credentialses.INSTANCE.getCreditntialss());
        MXSession mSession = Matrix.getInstance(getApplicationContext()).createSession(hsConfig);
        Matrix.getInstance(getApplicationContext()).addSession(mSession);
    }
}
