package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jeeps.gamecollector.adapters.PlatformsListAdapter;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.Platform;

import java.util.List;
import java.util.Random;

public class PlatformLibraryActivity extends AppCompatActivity {

    public static final String CURRENT_PLATFORM = "CURRENT_PLATFORM";

    private Context mContext;
    private int mPlatformId;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mPlatformLibraryDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_library);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        //Get platform id from intent
        Intent intent = getIntent();
        mPlatformId = intent.getIntExtra(CURRENT_PLATFORM, 0);

        // Get libraries from the database
        mDatabase = FirebaseDatabase.getInstance();
        mPlatformLibraryDB = mDatabase.getReference("library/games/" + mPlatformId);

        // Read from the database
        /*librariesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //List<Platform> platforms = (List<Platform>) dataSnapshot.getValue();
                GenericTypeIndicator<List<Platform>> genericTypeIndicator = new GenericTypeIndicator<List<Platform>>() {};
                List<Platform> platforms = dataSnapshot.getValue(genericTypeIndicator);

                PlatformsListAdapter adapter = new PlatformsListAdapter(mContext, R.layout.platform_list_item, platforms);
                platformsList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(MAIN_LIBRARY_TAG, "Failed to read value.", error.toException());
            }
        });*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AddGameActivity.class);
                intent.putExtra(CURRENT_PLATFORM, mPlatformId);
                startActivity(intent);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
