package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private DatabaseReference mUserdatabase,mUserRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentuser;
    private TextView mDisplayname,mUserStatus;
    private CircleImageView mProfileImg;
    private Button mStatusbtn,mImagebtn;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private ProgressDialog mProgress;
    private String location;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mImageStorage= FirebaseStorage.getInstance().getReference().child("profile_images");
        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        //

        mDisplayname = findViewById(R.id.setting_name);
        mUserStatus = findViewById(R.id.setting_status);
        mProfileImg = findViewById(R.id.setting_profileImg);
        mImagebtn = findViewById(R.id.setting_img_btn);
        mStatusbtn = findViewById(R.id.setting_status_btn);
        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mUserStatus.getText().toString();
                Intent statusIntent = new Intent(SettingActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
            }
        });
        //
        mCurrentuser = FirebaseAuth.getInstance().getCurrentUser();
        String currentuserUid = mCurrentuser.getUid();

        mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserUid);
        mUserdatabase.keepSynced(true);
        mUserdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue().toString();
                String status =  snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();

                //
                mDisplayname.setText(name);
                mUserStatus.setText(status);
               Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.userprofile)
                       .into(mProfileImg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileImg = new Intent();
                profileImg.setType("image/*");
                profileImg.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(profileImg,GALLERY_PICK);
                mUserdatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.userprofile).into(mProfileImg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);*/

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
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                mProfileImg.setBackgroundDrawable(bitmapDrawable);
                uploadtoFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadtoFirebase() {
        String filename = UUID.randomUUID().toString();
        StorageReference ref = FirebaseStorage.getInstance().getReference("/profile_images/"+"/thumbs/"+filename);
        ref.putFile(selectedphotoUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String URl = taskSnapshot.getMetadata().getPath();
                Log.d("SUCCESS",URl+" UPloaded");
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
        mUserdatabase.child("image").setValue(location).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SettingActivity.this, "Successfully done ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if (currentuser !=null){
        mUserRef.child("online").setValue(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserRef.child("online").setValue(true);
    }
}


/*    protected void onStart() {
    @Override
        super.onStart();
        //image intialization
        mCurrentuser = FirebaseAuth.getInstance().getCurrentUser();
        String currentuserUid = mCurrentuser.getUid();
        mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserUid);
        mUserdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.userprofile).into(mProfileImg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}*/