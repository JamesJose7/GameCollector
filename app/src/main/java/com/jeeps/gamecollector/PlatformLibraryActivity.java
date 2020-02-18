package com.jeeps.gamecollector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jeeps.gamecollector.adapters.GameCardAdapter;
import com.jeeps.gamecollector.comparators.GameByNameComparator;
import com.jeeps.gamecollector.comparators.GameByPhysicalComparator;
import com.jeeps.gamecollector.comparators.GameByTimesPlayedComparator;
import com.jeeps.gamecollector.model.CurrentUser;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.services.api.ApiClient;
import com.jeeps.gamecollector.services.api.GameService;
import com.jeeps.gamecollector.utils.UserUtils;
import com.jeeps.gamecollector.views.GridSpacingItemDecoration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PlatformLibraryActivity extends AppCompatActivity implements GameCardAdapter.GameCardAdapterListener {

    public static final String CURRENT_PLATFORM = "CURRENT_PLATFORM";
    public static final String CURRENT_PLATFORM_NAME = "CURRENT_PLATFORM_NAME";
    public static final String SELECTED_GAME = "SELECTED_GAME";
    public static final String SELECTED_GAME_POSITION = "SELECTED_GAME_POSITION";
    public static final int ADD_GAME_RESULT = 123;
    public static final int EDIT_GAME_RESULT = 321;
    public static final String NEW_GAME = "NEW_GAME";
    private static final String TAG = PlatformLibraryActivity.class.getSimpleName();

    @BindView(R.id.games_recycler_view) RecyclerView gamesRecyclerView;
    @BindView(R.id.backdrop) ImageView backdrop;
    @BindView(R.id.games_progressbar) ProgressBar progressBar;
    @BindView(R.id.fab) FloatingActionButton fab;

    private Context context;
    private String platformId;
    private String platformName;
    private GameCardAdapter gamesAdapter;
    private List<Game> games;
    private CurrentUser currentUser;
    private SharedPreferences sharedPreferences;
    private String gameToBeDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_library);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        games = new ArrayList<>();
        initCollapsingToolbar();

        //Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);

        //Get platform id from intent
        Intent intent = getIntent();
        platformId = intent.getStringExtra(CURRENT_PLATFORM);
        platformName = intent.getStringExtra(CURRENT_PLATFORM_NAME);

        //Display cover
        Picasso.with(context).load(getPlatformCover()).into(backdrop);

        // Configure recycler view
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        gamesRecyclerView.setLayoutManager(mLayoutManager);
        gamesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        gamesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add game
        fab.setOnClickListener(view -> {
            Intent startAddGameActivityIntent = new Intent(context, AddGameActivity.class);
            startAddGameActivityIntent.putExtra(CURRENT_PLATFORM, platformId);
            startAddGameActivityIntent.putExtra(CURRENT_PLATFORM_NAME, platformName);
            startActivityForResult(startAddGameActivityIntent, ADD_GAME_RESULT);
        });

        // Get games from the database
        populateGames();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_GAME_RESULT) {
            Game game = (Game) data.getSerializableExtra(NEW_GAME);
            games.add(game);
            Collections.sort(games, new GameByNameComparator());
            gamesAdapter.notifyDataSetChanged();
        } else if (requestCode == EDIT_GAME_RESULT) {
            Game game = (Game) data.getSerializableExtra(NEW_GAME);
            int position = data.getIntExtra(SELECTED_GAME_POSITION, -1);
            if (position >= 0) {
                games.remove(position);
                games.add(game);
                Collections.sort(games, new GameByNameComparator());
                gamesAdapter.notifyDataSetChanged();
            }
        }
    }

    private void populateGames() {
        // Load current user
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);
        // Load games
        GameService gameService = ApiClient.createService(GameService.class);
        Call<List<Game>> getGamesByUserAndPlatform = gameService.getGamesByUserAndPlatform(
                "Bearer " + currentUser.getToken(), currentUser.getUsername(), platformId);
        getGamesByUserAndPlatform.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful()) {
                    games = response.body();
                    // Sort A-z
                    Collections.sort(games, new GameByNameComparator());
                    // Create adapter
                    gamesAdapter = new GameCardAdapter(context, games, PlatformLibraryActivity.this);
                    gamesRecyclerView.setAdapter(gamesAdapter);
                    gamesAdapter.notifyDataSetChanged();
                    // Hide progressbar
                    progressBar.setVisibility(View.INVISIBLE);
                } else
                    Log.e(TAG, "Something went wrong when retrieving games");
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(TAG, "Something went wrong when retrieving games");
            }
        });
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
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
                    collapsingToolbar.setTitle(platformName);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    public int getPlatformCover() {
        switch (platformId) {
            case "0":
                return R.drawable.switch_cover;
            case "1":
                return R.drawable.wiiu_cover;
            case "2":
                return R.drawable.n3ds_cover;
            case "3":
                return R.drawable.wii_cover;
            case "4":
                return R.drawable.ds_cover;
        }
        return R.drawable.switch_cover;
    }

    @Override
    public void deleteSelectedGame(int position) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    //Get game
                    Game game = games.get(position);
                    //Get game key for DB
                    gameToBeDeleted = game.getId();

                    // Remove game from adapter
                    games.remove(position);
                    gamesAdapter.notifyItemRemoved(position);

                    //Notify user
                    BaseTransientBottomBar.BaseCallback<Snackbar> undoCallback = new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            // Delete game from the database
                            deleteGame();
                        }
                    };
                    Snackbar undo = Snackbar.make(gamesRecyclerView, "Deleted: " + game.getName(), Snackbar.LENGTH_LONG)
                            .addCallback(undoCallback);
                    undo.setAction("UNDO", view -> {
                        // Remove deletion callback
                        gameToBeDeleted = null;
                        undo.removeCallback(undoCallback);
                        // Put the game back in the adapter
                        games.add(position, game);
                        gamesAdapter.notifyItemInserted(position);
                    });
                    undo.show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Delete game?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteGame() {
        String id = gameToBeDeleted;
        gameToBeDeleted = null;
        if (id != null) {
            GameService gameService = ApiClient.createService(GameService.class);
            Call<ResponseBody> deleteGame = gameService.deleteGame("Bearer " + currentUser.getToken(), id);
            deleteGame.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful())
                        Log.i(TAG, "Game deleted successfully");
                    else
                        Toast.makeText(context, "There was a problem deleting the game, please try again", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, "There was a problem deleting the game, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void editGame(int position) {
        Game game = games.get(position);
        //Start add game activity to edit selected
        Intent intent = new Intent(context, AddGameActivity.class);
        intent.putExtra(CURRENT_PLATFORM, platformId);
        intent.putExtra(CURRENT_PLATFORM_NAME, platformName);
        intent.putExtra(SELECTED_GAME, game);
        intent.putExtra(SELECTED_GAME_POSITION, position);
        startActivityForResult(intent, EDIT_GAME_RESULT);
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
                Collections.sort(games, new GameByNameComparator());
                gamesAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_alph_desc:
                //Sort A-z desc
                Collections.sort(games, new GameByNameComparator(true));
                gamesAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_physical:
                //Sort physical
                Collections.sort(games, new GameByPhysicalComparator(true));
                gamesAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_alph_physical_desc:
                Collections.sort(games, new GameByPhysicalComparator());
                gamesAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_timesc:
                //Sort physical
                Collections.sort(games, new GameByTimesPlayedComparator());
                gamesAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_filter_alph_timesc_desc:
                Collections.sort(games, new GameByTimesPlayedComparator(true));
                gamesAdapter.notifyDataSetChanged();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Delete a game if it was pending deletion
        if (gameToBeDeleted != null)
            deleteGame();
    }
}
