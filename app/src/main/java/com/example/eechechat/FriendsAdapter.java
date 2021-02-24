package com.example.eechechat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends FirebaseRecyclerAdapter<Friends, FriendsAdapter.FriendsViewHolder> {
    private DatabaseReference mUserdatabase;
    public FriendsAdapter(@NonNull FirebaseRecyclerOptions<Friends> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Friends model) {
        holder.UserStatus.setText(model.getName());
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
                holder.UserName.setText(userName);
                Picasso.get().load(image).placeholder(R.drawable.userprofile).into(holder.userImage);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent profileIntent = new Intent(v.getContext(),ProfileActivity.class);
                                   // profileIntent2.putExtra("user_name",model.getName());
                                  //  profileIntent.putExtra("user_status",model.);
                                    //profileIntent.putExtra("user_pic",model.getImage());
                                    profileIntent.putExtra("user_id",user_id);
                                  //  Log.d("IDDDDDD",user_id);
                                    v.getContext().startActivity(profileIntent);

                                }
                                if (which == 1){
                                    Intent chatIntent = new Intent(v.getContext(),ChatActivity.class);
                                    chatIntent.putExtra("user_id",user_id);
                                    chatIntent.putExtra("user_name",userName);
                                    v.getContext().startActivity(chatIntent);
                                }
                            }
                        });
                        builder.show();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
        return new FriendsViewHolder(view);
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder {
        TextView UserName,UserStatus;
        CircleImageView userImage;
        ImageView onlineIcon;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            UserName = itemView.findViewById(R.id.user_name);
            UserStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.user_img);
            onlineIcon = itemView.findViewById(R.id.useronline_icon);
            mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mUserdatabase.keepSynced(true);
        }
    }

}
