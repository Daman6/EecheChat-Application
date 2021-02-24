package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLanguage;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayname;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private CircleImageView mProfileImg;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ProgressDialog RegProg;
    private DatabaseReference mDatabase;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mDisplayname = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateBtn = findViewById(R.id.reg_btn);
        mAuth = FirebaseAuth.getInstance();

        mProfileImg = findViewById(R.id.register_profileimg);
        //
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //
        RegProg = new ProgressDialog(this);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayname.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                if ( mProfileImg.getDrawable() == null ||!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    RegProg.setTitle("Registering User");
                    RegProg.setMessage("Please wait while we creating your account");
                    RegProg.setCanceledOnTouchOutside(false);
                    RegProg.show();
                    registerUser(display_name, password, email);
                }else{
                    Toast.makeText(RegisterActivity.this, "Please Fill All The Above", Toast.LENGTH_SHORT).show();
                }

            }

        });

        mProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileImg = new Intent();
                profileImg.setType("image/*");
                profileImg.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(profileImg,0);
            }
        });
    }
    private Uri selectedphotoUrl;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
           selectedphotoUrl = (Uri) data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedphotoUrl);
                //BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                mProfileImg.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerUser(String display_name, String password, String email) {
    mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = currentUser.getUid();

                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                        HashMap<String,String> userMap = new HashMap<>();
                        userMap.put("name",display_name);
                        userMap.put("status","Hi there Im using Echee Chat app :)");
                        userMap.put("image",location);
                        mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    RegProg.dismiss();
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                    uploadImagetoFirebaseStorage();
                                }
                            }
                        });

                    }else{
                        RegProg.hide();
                        Toast.makeText(RegisterActivity.this,"You Got Some error",Toast.LENGTH_LONG);
                    }
                }
            });
    }

    private void uploadImagetoFirebaseStorage() {

        String filename = UUID.randomUUID().toString();
       StorageReference ref = FirebaseStorage.getInstance().getReference("/profile_images/"+filename);
       ref.putFile(selectedphotoUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String URl = taskSnapshot.getMetadata().getPath();
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        location = uri.toString();
                    savedatatoFirebase();
                    }
                });
           }
       });
    }

    private void savedatatoFirebase() {
        mDatabase.child("image").setValue(location).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterActivity.this, "Successfully done ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}