package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jeeps.gamecollector.adapters.GameCardAdapter;
import com.jeeps.gamecollector.adapters.PlatformsListAdapter;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.Platform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PlatformLibraryActivity extends AppCompatActivity {

    public static final String CURRENT_PLATFORM = "CURRENT_PLATFORM";

    @BindView(R.id.games_recycler_view)
    RecyclerView mGamesRecyclerView;
    @BindView(R.id.backdrop)
    ImageView backdrop;

    private Context mContext;
    private int mPlatformId;
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

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(mContext, 2);
        mGamesRecyclerView.setLayoutManager(mLayoutManager);
        mGamesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mGamesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //Get platform id from intent
        Intent intent = getIntent();
        mPlatformId = intent.getIntExtra(CURRENT_PLATFORM, 0);

        // Get games from the database
        getGamesFromDB();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Reset list
                //mGames = new ArrayList<>();
                Intent intent = new Intent(mContext, AddGameActivity.class);
                intent.putExtra(CURRENT_PLATFORM, mPlatformId);
                startActivity(intent);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Display cover
        Picasso.with(mContext).load(R.drawable.switch_cover).into(backdrop);
    }

 private void getGamesFromDB() {
        mDatabase = FirebaseDatabase.getInstance();
        mPlatformLibraryDB = mDatabase.getReference("library/games/" + mPlatformId);

        /*mPlatformLibraryDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Game game = dataSnapshot.getValue(Game.class);
                mGames.add(game);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);

                for (Iterator<Game> iterator = mGames.iterator(); iterator.hasNext(); ) {
                    Game value = iterator.next();
                    if (value.getImageUri().equals(game.getImageUri())) {
                        iterator.remove();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        mPlatformLibraryDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String, HashMap<String, Object>> gamesMap = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();
                List<Game> games = new ArrayList<>();
                if (gamesMap != null) {
                    for (Map.Entry<String, HashMap<String, Object>> entry : gamesMap.entrySet()) {
                        Game game = Game.mapToGame(entry.getValue());
                        games.add(game);
                    }
                }
                mGames = games;

                //Create adapter
                mAdapter = new GameCardAdapter(mContext, mGames);

                mGamesRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
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
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
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

}
