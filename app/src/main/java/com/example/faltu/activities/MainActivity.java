package com.example.faltu.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.faltu.R;
import com.example.faltu.databinding.ActivityMainBinding;
import com.example.faltu.models.User;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins = 0;
    String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;
    User user;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the notification bar and navigation bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

// Hide the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        binding.progressBar.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");

        FirebaseUser currentUser = auth.getCurrentUser();

        database.getReference().child("profiles")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        binding.progressBar.setVisibility(View.GONE);
                        user = snapshot.getValue(User.class);
                        coins = user.getCoins();

                        binding.coins.setText("You have: " + coins);

                        Glide.with(MainActivity.this)
                                .load(user.getProfile())
                                .into(binding.profilePicture);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int count = (int) dataSnapshot.getChildrenCount();

                // Set the value in the text view
                binding.txtOnlinePeople.setText("Online: " + count);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(MainActivity.this, "Online Show Error", Toast.LENGTH_SHORT).show();
            }
        });



        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionsGranted()) {
                    if (coins > 5) {
                        coins = coins - 5;
                        database.getReference().child("profiles")
                                .child(currentUser.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                        intent.putExtra("profile", user.getProfile());
                        startActivity(intent);
                        //startActivity(new Intent(MainActivity.this, ConnectingActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    askPermissions();
                }
            }
        });

        binding.rewardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RewardActivity.class));
            }
        });




    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for(String permission : permissions ){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;

    }
}