package com.jeeps.gamecollector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jeeps.gamecollector.adapters.GameCardAdapter;
import com.jeeps.gamecollector.comparators.GameByNameComparator;
import com.jeeps.gamecollector.comparators.GameByPhysicalComparator;
import com.jeeps.gamecollector.comparators.GameByTimesPlayedComparator;
import com.jeeps.gamecollector.model.Game;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PlatformLibraryActivity extends AppCompatActivity implements GameCardAdapter.GameCardAdapterListener {

    public static final String CURRENT_PLATFORM = "CURRENT_PLATFORM";
    public static final String CURRENT_PLATFORM_NAME = "CURRENT_PLATFORM_NAME";
    public static final String SELECTED_GAME_KEY = "SELECTED_GAME_KEY";

    @BindView(R.id.games_recycler_view)
    RecyclerView mGamesRecyclerView;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.games_progressbar)
    ProgressBar mProgressBar;

    private Context mContext;
    private int mPlatformId;
    private String mPlatformName;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mPlatformLibraryDB;
    private GameCardAdapter mAdapter;
    private List<Game> mGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_library);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mContext = this;
        mGames = new ArrayList<>();
        initCollapsingToolbar();

        //Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //Get platform id from intent
        Intent intent = getIntent();
        mPlatformId = intent.getIntExtra(CURRENT_PLATFORM, 0);
        mPlatformName = intent.getStringExtra(CURRENT_PLATFORM_NAME);


        //Display cover
        Picasso.with(mContext).load(getPlatformCover()).into(backdrop);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(mContext, 2);
        mGamesRecyclerView.setLayoutManager(mLayoutManager);
        mGamesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mGamesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Get games from the database
        getGamesFromDB();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Reset list
                Intent intent = new Intent(mContext, AddGameActivity.class);
                intent.putExtra(CURRENT_PLATFORM, mPlatformId);
                startActivity(intent);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

 private void getGamesFromDB() {
        mDatabase = FirebaseDatabase.getInstance();
        mPlatformLibraryDB = mDatabase.getReference("library/games/" + mPlatformId);

        mPlatformLibraryDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String, HashMap<String, Object>> gamesMap = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();
                List<Game> games = new ArrayList<>();
                List<String> keys = new ArrayList<>();
                if (gamesMap != null) {
                    for (Map.Entry<String, HashMap<String, Object>> entry : gamesMap.entrySet()) {
                        Game game = Game.mapToGame(entry.getValue());
                        games.add(game);
                        keys.add(entry.getKey());
                    }
                }
                mGames = games;

                //Sort A-z
                Collections.sort(mGames, new GameByNameComparator());

                //Create adapter
                mAdapter = new GameCardAdapter(mContext, mGames, PlatformLibraryActivity.this);

                mGamesRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                //Hide progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(CURRENT_PLATFORM, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(mPlatformName);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    public int getPlatformCover() {
        switch (mPlatformId) {
            case 0:
                return R.drawable.switch_cover;
            case 1:
                return R.drawable.wiiu_cover;
            case 2:
                return R.drawable.n3ds_cover;
            case 3:
                return R.drawable.wii_cover;
            case 4:
                return R.drawable.ds_cover;
        }
        return R.drawable.switch_cover;
    }

    @Override
    public void deleteSelectedGame(final int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        //Get game
                        Game game = mGames.get(position);
                        //Get game key for DB
                        String key = game.getKey();

                        //Get stored cover file name
                        String firstCut[] = game.getImageUri().split("gameCovers%2F");
                        String secondCut[] = firstCut[1].split(".png");
                        String fileId = secondCut[0];
                        //Delete uploaded image
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                        StorageReference gameCoverRef = storageReference.child("gameCovers/" + fileId + ".png");
                        gameCoverRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Toast.makeText(mContext, "Deleted image", Toast.LENGTH_SHORT).show();
                                // File deleted successfully
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Toast.makeText(mContext, "There was an error trying to delete cover", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //Delete game
                        DatabaseReference gameReference = mDatabase.getReference("library/games/" + mPlatformId + "/" + key);
                        gameReference.removeValue();

                        //Notify user
                        //Toast.makeText(mContext, "Deleted: " + game.getName(), Toast.LENGTH_SHORT).show();
                        Snackbar.make(mGamesRecyclerView, "Deleted: " + game.getName(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Delete game?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void editGame(int position) {
        String key = mGames.get(position).getKey();
        //Start add game activity to edit selected
        Intent intent = new Intent(mContext, AddGameActivity.class);
        intent.putExtra(CURRENT_PLATFORM, mPlatformId);
        intent.putExtra(SELECTED_GAME_KEY, key);
        startActivity(intent);
    }


    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_platform_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case R.id.action_filter_alph:
                //Sort A-z
                Collections.sort(mGames, new GameByNameComparator());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_alph_desc:
                //Sort A-z desc
                Collections.sort(mGames, new GameByNameComparator(true));
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_physical:
                //Sort physical
                Collections.sort(mGames, new GameByPhysicalComparator(true));
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_alph_physical_desc:
                Collections.sort(mGames, new GameByPhysicalComparator());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_timesc:
                //Sort physical
                Collections.sort(mGames, new GameByTimesPlayedComparator());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_alph_timesc_desc:
                Collections.sort(mGames, new GameByTimesPlayedComparator(true));
                mAdapter.notifyDataSetChanged();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
