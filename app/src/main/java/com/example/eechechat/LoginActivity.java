package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private TextView mForgetpass;
    private Button mLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoginProg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        mEmail = (TextInputLayout)findViewById(R.id.login_email);
        mForgetpass = findViewById(R.id.login_forgetpass);
        mPassword = (TextInputLayout)findViewById(R.id.login_password);
        mLogin = (Button) findViewById(R.id.login_btn);
        mAuth = FirebaseAuth.getInstance();
        mLoginProg = new ProgressDialog(this);
        //

        mForgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String email = mEmail.getEditText().getText().toString();
                if (!TextUtils.isEmpty(email)){
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(LoginActivity.this, "Please provide us with your register email in above Email Field", Toast.LENGTH_LONG).show();
                }
            }
        });
        //
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Please fill all above fields", Toast.LENGTH_SHORT).show();
                }else {
                    mLoginProg.setTitle("Loging In");
                    mLoginProg.setMessage("Please wait while we are logging your account");
                    mLoginProg.setCanceledOnTouchOutside(false);
                    mLoginProg.show();
                    loginUser(email,password);
                }
            }
        });

        //
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loginUser(String email, String password) {
    mAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mLoginProg.dismiss();
                        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    }else {
                        mLoginProg.hide();
                        Toast.makeText(LoginActivity.this, "Please check your credentials :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}