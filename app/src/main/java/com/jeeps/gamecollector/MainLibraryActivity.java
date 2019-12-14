package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jeeps.gamecollector.adapters.PlatformsListAdapter;
import com.jeeps.gamecollector.model.Platform;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainLibraryActivity extends AppCompatActivity {

    private static final String MAIN_LIBRARY_TAG = "MAIN_LIBRARY_TAG";

    @BindView(R.id.platforms_list)
    ListView platformsList;
    @BindView(R.id.platforms_progress_bar)
    ProgressBar mProgressBar;

    private Context mContext;
    private DatabaseReference librariesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_library);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Platforms");

        mContext = this;

        //Show progressbar
        mProgressBar.setVisibility(View.VISIBLE);

        // Get libraries from the database
        librariesDB = FirebaseDatabase.getInstance().getReference("library/platforms");
        readGamePlatforms();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void readGamePlatforms() {
        // Read from the database
        librariesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<List<Platform>> genericTypeIndicator = new GenericTypeIndicator<List<Platform>>() {};
                List<Platform> platforms = dataSnapshot.getValue(genericTypeIndicator);

                PlatformsListAdapter adapter = new PlatformsListAdapter(mContext, R.layout.platform_list_item, platforms);
                platformsList.setAdapter(adapter);
                platformsList.setOnItemClickListener((adapterView, view, i, l) -> {
                    //Get selected platform
                    Platform platform = (Platform) adapterView.getItemAtPosition(i);
                    //start games activity with platform id
                    Intent intent = new Intent(mContext, PlatformLibraryActivity.class);
                    intent.putExtra(PlatformLibraryActivity.CURRENT_PLATFORM, platform.getId());
                    intent.putExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME, platform.getName());
                    startActivity(intent);
                });

                //Hide progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(MAIN_LIBRARY_TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_stats) {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
