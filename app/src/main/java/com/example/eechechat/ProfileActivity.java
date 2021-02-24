package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import java.text.DateFormat;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView mUserName,mUserStatus,mUserFriend,mTotalFriend;
    private Button mSendRequest,mDeclineRequest;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserdatabase,mFriendreqDatabase,mFriendDatabase,mNotificationDatabase;
    private FirebaseUser mCurrentUser;
    private ImageView mUserpic;
    private ProgressDialog mProgress;
    private String mCurrent_state;
    private String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        //
        Intent profileIntent = getIntent();
        user_id = profileIntent.getStringExtra("user_id");
        Log.d("ProfileUUID",user_id);
        //
        mUserName = findViewById(R.id.profile_username);
        mUserStatus = findViewById(R.id.profile_userstatus);
        mTotalFriend = findViewById(R.id.total_friend);
        mUserpic = findViewById(R.id.profile_userimg);
        mSendRequest = findViewById(R.id.sendrequest);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDeclineRequest = findViewById(R.id.declinerequest);
        String currentUser = mCurrentUser.getUid();
        Log.d("ProfileCurrentuser", String.valueOf(currentUser));
        mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                mUserName.setText(name);
                mUserStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.userprofile).into(mUserpic);
                mProgress.dismiss();
                mDeclineRequest.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mFriendreqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrent_state = "not_friends";//not friend
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading Profile");
        mProgress.setMessage("Please wait for while");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        //-----------------------friend list /friend request---------------//////////
        mFriendreqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(user_id)){

                String req_type = snapshot.child(user_id).child("request_type").getValue().toString();
                if (req_type.equals("recieved")){
                    mCurrent_state = "req_recieved";
                    mSendRequest.setText("Accept Friend Request");
                    mDeclineRequest.setVisibility(View.VISIBLE);
                    mDeclineRequest.setEnabled(true);
                } else if (req_type.equals("sent")){
                    mCurrent_state="req_sent";
                    mSendRequest.setText("Cancel Friend Request");
                    mDeclineRequest.setVisibility(View.INVISIBLE);
                    mDeclineRequest.setEnabled(false);
                }
                mProgress.dismiss();
                }else {
                    mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(user_id)){
                                mSendRequest.setEnabled(true);
                                mSendRequest.setText("UnFriend This Person");
                                mCurrent_state = "friends";//---------------------------------------------------change kia h abhi
                                mProgress.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            mProgress.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //
        //loadData();
        //loaddata2();
        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendRequest.setEnabled(false);
                ///--------------------not freind------------/
                if(mCurrent_state.equals("not_friends")){
                    mFriendreqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFriendreqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                                .setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                HashMap<String,String> Notification_data = new HashMap<>();
                                                Notification_data.put("from",mCurrentUser.getUid());
                                                Notification_data.put("type","request");
                                                mNotificationDatabase.child(user_id).push().setValue(Notification_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mCurrent_state = "req_sent";//friends
                                                        mSendRequest.setEnabled(true);
                                                        mSendRequest.setText("Cancel Friend request");
                                                        mDeclineRequest.setVisibility(View.INVISIBLE);
                                                        mDeclineRequest.setEnabled(false);
                                                        Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                        });

                                    }else{
                                        Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );
                }
                ////-------------cancel request state----//////////
                if (mCurrent_state=="req_sent"){
                    mFriendreqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendreqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mCurrent_state= "not_friends";//friends
                                    mSendRequest.setEnabled(true);
                                    mSendRequest.setText("Send Friend request");
                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                    mDeclineRequest.setEnabled(false);
                                }
                            });
                        }
                    });
                }
                if (mCurrent_state=="req_recieved"){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                   mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("Date").setValue(currentDate)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("Date").setValue(currentDate)
                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   mFriendreqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void aVoid) {
                                                           mFriendreqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                               @Override
                                                               public void onSuccess(Void aVoid) {
                                                                   mCurrent_state= "friends";//friends
                                                                   mSendRequest.setEnabled(true);
                                                                   mSendRequest.setText("UnFriend This Person :( ");
                                                                   mDeclineRequest.setVisibility(View.INVISIBLE);
                                                                   mDeclineRequest.setEnabled(false);
                                                              }
                                                           });
                                                       }
                                                   });
                                               }
                                           });
                               }
                           });
                }
                if (mCurrent_state=="friends"){
                    /*Map unfriendMap = new HashMap();
                        unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id , null);
                        unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() , null);*/
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("Date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("Date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mCurrent_state= "not_friends";//friends
                                    mSendRequest.setEnabled(true);
                                    mSendRequest.setText("Send Request");
                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                    mDeclineRequest.setEnabled(false);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

   /* private void loadData() {
        Intent profileIntent = getIntent();
        //String uuid = profileIntent.getStringExtra("uuid");
        String user_name = profileIntent.getStringExtra("user_name");
        String user_status = profileIntent.getStringExtra("user_status");
        String user_image = profileIntent.getStringExtra("user_pic");

        mUserName.setText(user_name);
        mUserStatus.setText(user_status);
       // Log.d("url",user_image );
        Picasso.get().load(user_image).placeholder(R.drawable.userprofile).into(mUserpic);
        mProgress.dismiss();
    }*/
   /* private void loaddata2(){
        Intent profileIntent2 = getIntent();
        String userid = profileIntent2.getStringExtra("user_id2");
        Log.d("DATDDTDA",userid);
        mUserName.setText(user_id);
    }*/
    @Override
    protected void onStart() {
        super.onStart();
        mUserRef.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCurrentUser != null){
            mUserRef.child("online").setValue(true);
        }
    }
}