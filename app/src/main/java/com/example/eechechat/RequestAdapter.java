package com.example.eechechat;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends FirebaseRecyclerAdapter<Request, RequestAdapter.RequestViewHolder> {
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    public RequestAdapter(@NonNull FirebaseRecyclerOptions<Request> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request model) {
        String user_id = getRef(position).getKey();
        Log.d("USER_ID",user_id);
       mUserDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue().toString();
                 //String image =  snapshot.child("image").getValue().toString();
                 String status = snapshot.child("status").getValue().toString();
            /*    if (snapshot.hasChild("request_type")){
                    String requestType = snapshot.child("request_type").getValue().toString();
                    Log.d("TYPE",requestType);
                    if (requestType.equals("sent")){
                       holder.name.setText(userName);
                       holder.status.setText(status);
                       Picasso.get().load(image).placeholder(R.drawable.userprofile).into(holder.image);
                    }else {
                        holder.status.setText("NO FRIEND LIST");
                    }
                }*/

               holder.name.setText(userName);
               holder.status.setText(status);
              // Picasso.get().load(image).placeholder(R.drawable.userprofile).into(holder.image);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(v.getContext(),ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        v.getContext().startActivity(profileIntent);
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
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
        return new RequestAdapter.RequestViewHolder(view);
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView name,status;
        CircleImageView image;
        FirebaseAuth mAuth;
        String mCurrentUser;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            name=(TextView) itemView.findViewById(R.id.user_name);
            status=(TextView) itemView.findViewById(R.id.user_status);
            image = (CircleImageView) itemView.findViewById(R.id.user_img);

            mAuth = FirebaseAuth.getInstance();
            mCurrentUser = mAuth.getCurrentUser().getUid();

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUser);
        }
    }
}
