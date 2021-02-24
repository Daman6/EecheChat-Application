package com.example.eechechat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RequestFragment extends Fragment {

    private RecyclerView mRequestList;

    private FirebaseRecyclerOptions options;
    private RequestAdapter adapter;

    private View mView;

    private DatabaseReference mFriendreqDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentuser;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_request, container, false);
        mRequestList = mView.findViewById(R.id.request_userlist);


        mAuth = FirebaseAuth.getInstance();
        mCurrentuser = mAuth.getCurrentUser().getUid();

        mFriendreqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentuser);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(mFriendreqDatabase,Request.class)
                .build();

        adapter = new RequestAdapter(options);
        mRequestList.setAdapter(adapter);

        return mView;
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(mFriendreqDatabase,Request.class)
                .build();
        adapter.startListening();
    }
}