package com.example.eechechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager2 mViewpagger;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTablayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Echee Chat");

        mTablayout = (TabLayout)findViewById(R.id.main_tabs);
        if (mAuth.getCurrentUser() != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //tabs
        mViewpagger = (ViewPager2) findViewById(R.id.main_pagger);
        mViewpagger.setAdapter(new SectionPagerAdapter(this));
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                mTablayout, mViewpagger, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText("Request");
                        break;
                    } case 1:{
                        tab.setText("Chats");
                        break;
                    } case 2:{
                        tab.setText("Friends");
                        break;
                    }
                }
            }
        }
        );
        tabLayoutMediator.attach();
        //


    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser ==  null){
            sendToStart();
        }else {
            mUserRef.child("online").setValue("true");
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            sendToStart();
        }
        if (item.getItemId() == R.id.main_setting_btn){
            Intent settingIntent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(settingIntent);
        }
        if (item.getItemId() == R.id.main_all_btn){
            Intent userIntent = new Intent(MainActivity.this,UserActivity.class);
            startActivity(userIntent);

        }
        return true;
    }
}