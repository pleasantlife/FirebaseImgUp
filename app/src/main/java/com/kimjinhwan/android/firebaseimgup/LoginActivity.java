package com.kimjinhwan.android.firebaseimgup;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText inputID, inputPassword;
    String id, password;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authListener;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this).setTitle("로그인 중입니다.")
                .setMessage("잠시만 기다려주세요").create();

        initView();
        firebaseAuth = FirebaseAuth.getInstance();
        loginInputValidate(dialog);


    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        inputID = (EditText) findViewById(R.id.inputID);
        inputPassword = (EditText) findViewById(R.id.inputPassword);
    }

    private void loginInputValidate(final AlertDialog dialog){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = inputID.getText().toString();
                password = inputPassword.getText().toString();
                Log.e("id=====", id);
                Log.e("pw=====", password);
                if( id.equals("") || password.equals("")) {
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                } else {
                    boolean emailPattern = Patterns.EMAIL_ADDRESS.matcher(id).matches();
                    if(emailPattern == true){
                        if(password.equals("")){
                            Toast.makeText(LoginActivity.this, "패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        } else {

                            dialog.show();
                            loginValidate(id, password, dialog);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "이메일 형식이 바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void loginValidate(String id, String password, final AlertDialog dialog){
        firebaseAuth.signInWithEmailAndPassword(id, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    String userEmail = user.getEmail();
                    dialog.dismiss();
                    loginSuccess(userEmail);
                } else {
                    Toast.makeText(LoginActivity.this, "ID 또는 패스워드가 바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginSuccess(String userEmail){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userEmail", userEmail);
        startActivity(intent);
        finish();
    }

}
