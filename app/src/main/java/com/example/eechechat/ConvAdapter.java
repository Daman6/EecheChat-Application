package com.example.eechechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConvAdapter extends FirebaseRecyclerAdapter<Conv,ConvAdapter.ConvViewholder> {
    private DatabaseReference mUserdatabase;
    private DatabaseReference mMessagedatabase;
    private DatabaseReference mConvDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private Context context;



    public ConvAdapter(@NonNull FirebaseRecyclerOptions<Conv> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ConvViewholder holder, int position, @NonNull Conv model) {
        String user_id = getRef(position).getKey();
        mUserdatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                if (snapshot.hasChild("online")){
                    String userOnline = snapshot.child("online").getValue().toString();
                    // Log.d("Boo",userOnline.toString());
                    if(userOnline.equals("true")){
                        holder.onlineIcon.setVisibility(View.VISIBLE);
                    }else {
                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                    }
                }
                Log.d("Username",userName);
                holder.UserName.setText(userName);
                Picasso.get().load(image).placeholder(R.drawable.userprofile).into(holder.userImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent messageIntent = new Intent(v.getContext(),ChatActivity.class);
                        messageIntent.putExtra("user_id",user_id);
                        messageIntent.putExtra("user_name",userName);
                        //Toast.makeText(v.getContext(),user_id, Toast.LENGTH_SHORT).show();
                        v.getContext().startActivity(messageIntent);
                        //DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
                        //mAuth = FirebaseAuth.getInstance();
                        //String currentUserId = mAuth.getCurrentUser().getUid();

                        //String mChatUser = intent.getStringExtra("user_id");
                        //mRootRef.child("Chat").child(user_id).child(currentUserId).child("seen").setValue(false);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Conv c = getItem(position);
        boolean seen = c.isSeen();
        long time = c.getTimestamp();
        GetTimeAgo getTimeAgo = new GetTimeAgo();
        String lastTime = getTimeAgo.getTimeAgo(time,context);
        holder.UserMessagetime.setText(lastTime);

        if (!seen){
            holder.UserMessage.setTypeface(holder.UserMessage.getTypeface(), Typeface.NORMAL);

        }else {
            holder.UserMessage.setTypeface(holder.UserMessage.getTypeface(), Typeface.BOLD);
        }
        Query lastMessageQuery = mMessagedatabase.child(user_id).limitToLast(1);
        lastMessageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               String data = snapshot.child("message").getValue().toString();
               boolean seen  = (Boolean) snapshot.child("seen").getValue();
                //long time = (long) snapshot.child("time").getValue();
               // Log.d("Value1", String.valueOf(seen));
               // Log.d("Value2",data);
                holder.UserMessage.setText(data);
                //DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
                //mAuth = FirebaseAuth.getInstance();
                //String currentUserId = mAuth.getCurrentUser().getUid();

                //String mChatUser = intent.getStringExtra("user_id");
                //mRootRef.child("Chat").child(user_id).child(currentUserId).child("seen").setValue(false);
               // Log.d("mchat",mchatuser);
                //GetTimeAgo getTimeAgo = new GetTimeAgo();
                //String lastSeenTime = getTimeAgo.getTimeAgo(time,context);
                //holder.UserMessage.setText(String.valueOf(seen));
               // Log.d("stamp", String.valueOf(time));
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

    @NonNull
    @Override
    public ConvViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
        return new ConvAdapter.ConvViewholder(view);
    }

    class ConvViewholder extends RecyclerView.ViewHolder{
        TextView UserName,UserMessage,UserMessagetime;
        CircleImageView userImage;
        ImageView onlineIcon;

        public ConvViewholder(@NonNull View itemView) {
            super(itemView);
            UserName = itemView.findViewById(R.id.user_name);
            UserMessage = itemView.findViewById(R.id.user_status);
            UserMessagetime= itemView.findViewById(R.id.user_messagetime);
            userImage = itemView.findViewById(R.id.user_img);
            onlineIcon = itemView.findViewById(R.id.useronline_icon);
            mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mUserdatabase.keepSynced(true);

            mAuth = FirebaseAuth.getInstance();
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mMessagedatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        }

        /*public void setMessage(String message,boolean isSeen) {
            UserMessage.setText(message);
            if(!isSeen){
                Log.d("tag", String.valueOf(!isSeen));
                UserMessage.setTypeface(UserMessage.getTypeface(), Typeface.NORMAL);
            }else {
                Log.d("tag1", String.valueOf(isSeen));
                UserMessage.setTypeface(UserMessage.getTypeface(), Typeface.BOLD);
            }
        }*/
    }

}
