package com.example.eechechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private MyAdapter adapter;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth2;
    private FirebaseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mToolbar = findViewById(R.id.user_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //
        mUserList = findViewById(R.id.user_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(option);
        mUserList.setAdapter(adapter);
      mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mAuth2 = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth2.getCurrentUser().getUid());
    }
    FirebaseRecyclerOptions<Users> option =
            new FirebaseRecyclerOptions.Builder<Users>()
                    .setQuery(FirebaseDatabase.getInstance().getReference().child("Users"), Users.class)
                    .build();


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        mUserRef.child("online").setValue(true);

    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        if (mCurrentUser != null){
            mUserRef.child("online").setValue(false);
        }
    }
}