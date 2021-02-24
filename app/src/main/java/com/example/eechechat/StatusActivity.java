package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    public EditText mStatus;
    private Button mChangeBtn;
    private DatabaseReference mStatusdata;
    private FirebaseUser mUser;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mStatus= findViewById(R.id.status_input);
        mChangeBtn=findViewById(R.id.status_changebtn);

        mUser= FirebaseAuth.getInstance().getCurrentUser();
        mProgress = new ProgressDialog(StatusActivity.this);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus.setText(status_value);
        //
        String current_uid = mUser.getUid();
        mStatusdata = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait for a while ;0  ");
                mProgress.show();
                mStatus= findViewById(R.id.status_input);
                String status = mStatus.getText().toString();
                Log.d("Status",status);
                mStatusdata.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgress.dismiss();
                            Toast.makeText(StatusActivity.this, "Successfully change :)", Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getApplicationContext(), "Got some error plz try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        //
        mToolbar =(Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Set Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}