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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ChatsFragment extends Fragment {
    private RecyclerView mConvolist;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    private ConvAdapter adapter;

    private View mMainview;

    private FirebaseRecyclerOptions options;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainview = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvolist = mMainview.findViewById(R.id.Userchat_friends);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);
        mUsersDatabase =FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvolist.setHasFixedSize(true);
        mConvolist.setLayoutManager(linearLayoutManager);

        Query ConvoQuery = mConvDatabase.orderByChild("timestamp");
        options = new FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(ConvoQuery,Conv.class).build();
        adapter = new ConvAdapter(options);
        mConvolist.setAdapter(adapter);


        return mMainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query ConvoQuery = mConvDatabase.orderByChild("timestamp");
        options = new FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(ConvoQuery,Conv.class).build();

        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
