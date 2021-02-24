package com.example.eechechat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class FriendsFragment extends Fragment {
    private RecyclerView mFriends_list;
    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private FriendsAdapter adapter;
    private View mMainview;
   // private String options;
    private FirebaseRecyclerOptions options;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainview =  inflater.inflate(R.layout.fragment_friends, container, false);
        mFriends_list = (RecyclerView) mMainview.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);

        mFriends_list.setHasFixedSize(true);
        mFriends_list.setLayoutManager(new LinearLayoutManager(getContext()));

        options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id), Friends.class)
                .build();

        adapter= new FriendsAdapter(options);
        mFriends_list.setAdapter(adapter);

        return mMainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id), Friends.class)
                .build();
        adapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}