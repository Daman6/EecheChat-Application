package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private Toolbar mChatAppbar;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ImageView mChatAdd, mChatSendBtn;
    private EditText mChatSend;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearlayout;
    private MessageAdapter mAdapter;

    private static final int GALLERY_PICK = 1;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;
    //
    private int itemPos = 0;
    private String mlastKey = "";
    private String mPrevkey = "";
    //
    private  String chat_username;

    private StorageReference mImageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //
        mChatAppbar = (Toolbar) findViewById(R.id.chat_Appbar);
        setSupportActionBar(mChatAppbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        //mChatUser = getIntent().getStringExtra("user_id");
        //chat_username = getIntent().getStringExtra("user_name");


        //getSupportActionBar().setTitle(chat_username);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custombar, null);

        actionBar.setCustomView(action_bar_view);
        //---------custom bar item
        TextView mTitleview = (TextView) findViewById(R.id.customchat_username);
        TextView mLastseen = (TextView) findViewById(R.id.customchat_lastseen);
        CircleImageView mUserImage = (CircleImageView) findViewById(R.id.customchat_userimage);

        //

        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearlayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearlayout);
        mMessagesList.setAdapter(mAdapter);
        loadMessages();

        mChatAdd = findViewById(R.id.chat_addimage);
        mChatSend = findViewById(R.id.chat_message);
        mChatSendBtn = findViewById(R.id.chat_sendBtn);

        // Intent messageIntent = getIntent();
        // mChatUser = messageIntent.getStringExtra("userId");
        // chat_username = messageIntent.getStringExtra("userName");

       /* Intent messageIntent = getIntent();
        mChatUser = messageIntent.getStringExtra("userId");
        chat_username = messageIntent.getStringExtra("userName");*/

        mChatUser = getIntent().getStringExtra("user_id");
        chat_username = getIntent().getStringExtra("user_name");

        mTitleview.setText(chat_username);
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String online = snapshot.child("online").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.userprofile).into(mUserImage);
                if (online.equals("true")) {
                    mLastseen.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastseen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + currentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Log.d("Chat log", error.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             sendMessages();
            }
        });
        mChatAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos =0;
                loadMoreMessages();
            }
        });
    }

    private Uri imageUri;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK ){
            imageUri = data.getData();

            String current_userref = "messages/" + currentUserId + "/" + mChatUser;
            String chat_userref = "messages/" + mChatUser + "/" + currentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(currentUserId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();
            StorageReference filepath = FirebaseStorage.getInstance().getReference("/message_images/"+ push_id);
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                       @Override
                       public void onSuccess(Uri uri) {
                           String downloadUrl = uri.toString();

                           Map messageMap = new HashMap();
                           messageMap.put("message",downloadUrl);
                           messageMap.put("seen",false);
                           messageMap.put("type","image");
                           messageMap.put("time",ServerValue.TIMESTAMP);
                           messageMap.put("from",currentUserId);

                           Map usermessageMap = new HashMap();
                           usermessageMap.put(current_userref + "/" + push_id,messageMap);
                           usermessageMap.put(chat_userref + "/" + push_id,messageMap);

                           mChatSend.setText("");
                           mRootRef.updateChildren(usermessageMap, new DatabaseReference.CompletionListener() {
                               @Override
                               public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                   if (error != null) {
                                       Log.d("Chat log", error.getMessage().toString());
                                   }
                               }
                           });
                       }
                   });

                }
            });

        }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef =  mRootRef.child("messages").child(currentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mlastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages message = snapshot.getValue(Messages.class);
                String messageKey = snapshot.getKey();
                if (!mPrevkey.equals(messageKey)){
                    messagesList.add(itemPos,message);
                }else {
                    mPrevkey = mlastKey;
                }
                if (itemPos == 1){
                    mlastKey= messageKey;
                }
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearlayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessages() {
             String message = mChatSend.getText().toString();
        if (!TextUtils.isEmpty(message)){
            String current_userref = "messages/" + currentUserId + "/" + mChatUser;
            String chat_userref = "messages/" + mChatUser + "/" + currentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(currentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);

            Map usermessageMap = new HashMap();
            usermessageMap.put(current_userref + "/" + push_id,messageMap);
            usermessageMap.put(chat_userref + "/" + push_id,messageMap);

            mChatSend.setText("");
            mRootRef.child("Chat").child(currentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(currentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(currentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(currentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);
            mRootRef.updateChildren(usermessageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null) {
                        Log.d("Chat log", error.getMessage().toString());
                    }
                }
            });
        }
    }

    private void loadMessages() {
       /* Intent messageIntent = getIntent();
        mChatUser = messageIntent.getStringExtra("userId");
        chat_username = messageIntent.getStringExtra("userName");*/

        mChatUser = getIntent().getStringExtra("user_id");
        chat_username = getIntent().getStringExtra("user_name");
        DatabaseReference messageRef =  mRootRef.child("messages").child(currentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages message = snapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1){
                    String messageKey = snapshot.getKey();
                    mlastKey= messageKey;
                    mPrevkey = messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}