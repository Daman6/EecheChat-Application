package com.example.eechechat;

import android.content.Context;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends FirebaseRecyclerAdapter<Users,MyAdapter.myViewHolder> {
    Context context = new UserActivity();
    public MyAdapter(@NonNull FirebaseRecyclerOptions<Users> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Users model) {
        holder.name.setText(model.getName());
        holder.status.setText(model.getStatus());
        Picasso.get().load(model.getImage()).into(holder.image);
        final String user_id = getRef(position).getKey();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(v.getContext(),ProfileActivity.class);
             /*   profileIntent.putExtra("user_name",model.getName());
                profileIntent.putExtra("user_status",model.getStatus());
                profileIntent.putExtra("user_pic",model.getImage());  */
                profileIntent.putExtra("user_id",user_id);
                Log.d("AdapterUUID",user_id);
                v.getContext().startActivity(profileIntent);
            }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
         TextView name,status;
         CircleImageView image;
         public myViewHolder(@NonNull View itemView) {
             super(itemView);
             name=(TextView) itemView.findViewById(R.id.user_name);
             status=(TextView) itemView.findViewById(R.id.user_status);
             image = (CircleImageView) itemView.findViewById(R.id.user_img);
         }
     }
}
