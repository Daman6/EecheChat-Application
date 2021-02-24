package com.example.eechechat;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private Context context;
    public MessageAdapter(List<Messages> mMessageList){
            this.mMessageList = mMessageList;
           // this.context = context;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        String current_userId  =mAuth.getCurrentUser().getUid();
            Messages c = mMessageList.get(position);
            String from_user = c.getFrom();
            String message_type = c.getType();
            boolean seen= c.isSeen();

            mUserDatabase.child(from_user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue().toString();
                    String image = snapshot.child("image").getValue().toString();

                    holder.mMessageUsername.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.userprofile).into(holder.mUserImg);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if (message_type.equals("text")){
                holder.mMessageText.setText(c.getMessage());
                holder.mMessageImage.setVisibility(View.INVISIBLE);
            }else {
                holder.mMessageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(c.getMessage()).placeholder(R.drawable.userprofile).into(holder.mMessageImage);
            }
            if (from_user.equals(current_userId)){

                holder.mMessageText.setBackgroundResource(R.drawable.message2_text);
                holder.mMessageText.setTextColor(Color.WHITE);

            }else {
                holder.mMessageText.setBackgroundResource(R.drawable.message_text_shape);
                holder.mMessageText.setTextColor(Color.WHITE);
            }
            holder.mMessageText.setText(c.getMessage());
            long msg_time = c.getTime();

            GetTimeAgo getTimeAgo = new GetTimeAgo();
            String lastSeenTime = getTimeAgo.getTimeAgo(msg_time, context);
            Log.d("time", String.valueOf(msg_time));
        //   Log.d("time1","ni"+lastSeenTime);

           holder.mMessageTime.setText(lastSeenTime);
           if (lastSeenTime==null){
               holder.mMessageTime.setText("just now");
           }
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

           private  TextView mMessageText,mMessageUsername,mMessageTime,mMessageseen;
           private CircleImageView mUserImg;
           private ImageView mMessageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mMessageUsername = itemView.findViewById(R.id.message_username);
            mMessageText = itemView.findViewById(R.id.message_usermessage);
            mUserImg = itemView.findViewById(R.id.message_userimg);
            mMessageTime = itemView.findViewById(R.id.message_usermessagetime);
            mMessageImage = (ImageView) itemView.findViewById(R.id.message_usermessageimg);
            mMessageseen = itemView.findViewById(R.id.message_usermessageseen);

        }
    }
}
