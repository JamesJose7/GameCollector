package com.jeeps.gamecollector.deprecated;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.deprecated.adapters.GameCardAdapter;
import com.jeeps.gamecollector.deprecated.model.CurrentUser;
import com.jeeps.gamecollector.deprecated.services.api.ApiClient;
import com.jeeps.gamecollector.deprecated.services.api.GameService;
import com.jeeps.gamecollector.deprecated.utils.PlatformCovers;
import com.jeeps.gamecollector.deprecated.utils.UserUtils;
import com.jeeps.gamecollector.deprecated.views.GridSpacingItemDecoration;
import com.jeeps.gamecollector.remaster.data.model.data.games.Game;
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat;
import com.jeeps.gamecollector.remaster.utils.comparators.GameByHoursCompletionistComparator;
import com.jeeps.gamecollector.remaster.utils.comparators.GameByHoursMainExtraComparator;
import com.jeeps.gamecollector.remaster.utils.comparators.GameByHoursStoryComparator;
import com.jeeps.gamecollector.remaster.utils.comparators.GameByNameComparator;
import com.jeeps.gamecollector.remaster.utils.comparators.GameByPhysicalComparator;
import com.jeeps.gamecollector.remaster.utils.comparators.GameByTimesPlayedComparator;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PlatformLibraryActivity extends AppCompatActivity {

    public static final String CURRENT_PLATFORM = "CURRENT_PLATFORM";
    public static final String CURRENT_PLATFORM_NAME = "CURRENT_PLATFORM_NAME";
    public static final String SELECTED_GAME = "SELECTED_GAME";
    public static final String SELECTED_GAME_POSITION = "SELECTED_GAME_POSITION";
    public static final int ADD_GAME_RESULT = 123;
    public static final int EDIT_GAME_RESULT = 321;
    public static final String NEW_GAME = "NEW_GAME";
    private static final String TAG = PlatformLibraryActivity.class.getSimpleName();

    RecyclerView gamesRecyclerView;
    ImageView backdrop;
    ProgressBar progressBar;
    FloatingActionButton fab;

    private Context context;
    private String platformId;
    private String platformName;
    private GameCardAdapter gamesAdapter;
    private List<Game> gamesOriginalList;
    private List<Game> games;
    private CurrentUser currentUser;
    private SharedPreferences sharedPreferences;
    private String gameToBeDeleted;
    private FirebaseFirestore db;

    private Comparator<Game> currentComparator = new GameByNameComparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup exit transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setExitTransition(new Explode());

        super.onCreate(savedInstanceState);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        games = new ArrayList<>();
        initCollapsingToolbar();

        // Get Firestore instance
        db = FirebaseFirestore.getInstance();

        //Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);

        //Get platform id from intent
        Intent intent = getIntent();
        platformId = intent.getStringExtra(CURRENT_PLATFORM);
        platformName = intent.getStringExtra(CURRENT_PLATFORM_NAME);

        //Display cover
        Picasso.get().load(PlatformCovers.getPlatformCover(platformName)).into(backdrop);

        initializeGamesAdapter();

        // Add game
        fab.setOnClickListener(view -> {
            Intent startAddGameActivityIntent = new Intent(context, AddGameActivityOld.class);
            startAddGameActivityIntent.putExtra(CURRENT_PLATFORM, platformId);
            startAddGameActivityIntent.putExtra(CURRENT_PLATFORM_NAME, platformName);
            startActivityForResult(startAddGameActivityIntent, ADD_GAME_RESULT);
        });

        // Get games from the database
        populateGames();
    }

    private void initializeGamesAdapter() {
        // Configure recycler view
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        gamesRecyclerView.setLayoutManager(mLayoutManager);
        gamesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        gamesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // Create adapter
        gamesAdapter = new GameCardAdapter(games);
        gamesRecyclerView.setAdapter(gamesAdapter);
        gamesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_GAME_RESULT) {
            if (resultCode == RESULT_OK) {
                Game game = (Game) data.getSerializableExtra(NEW_GAME);
                Snackbar.make(fab, "Game added successfully", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == EDIT_GAME_RESULT) {
            if (resultCode == RESULT_OK) {
                Game game = (Game) data.getSerializableExtra(NEW_GAME);
                int position = data.getIntExtra(SELECTED_GAME_POSITION, -1);
                Snackbar.make(fab, "Game edited successfully", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void populateGames() {
        // Load current user
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);
        // Load games
        db.collection("games")
                .whereEqualTo("user", currentUser.getUsername())
                .whereEqualTo("platformId", platformId)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    // Hide progressbar
                    progressBar.setVisibility(View.INVISIBLE);
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Log.e(TAG, "Something went wrong when retrieving games");
                        Toast.makeText(context, "An error has occurred, please try again", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    games.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Game game = doc.toObject(Game.class);
                        game.setId(doc.getId());
                        games.add(game);
                    }
                    // Sort A-z
                    sortGamesWithCurrentComparator();
                    gamesOriginalList = new ArrayList<>(games);
                    // Update adapter
                    updateGamesAdapter();
                });
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        /*final CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
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
        });*/
    }

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

    public void editGame(int position, @NotNull View imageView, @NotNull TextView gameTitle) {
        Game game = games.get(position);
        //Start add game activity to edit selected
        Intent intent = new Intent(context, GameDetailsActivityOld.class);
        intent.putExtra(CURRENT_PLATFORM, platformId);
        intent.putExtra(CURRENT_PLATFORM_NAME, platformName);
        intent.putExtra(SELECTED_GAME, game);
        intent.putExtra(SELECTED_GAME_POSITION, position);

        ActivityOptions activityOptions = ActivityOptions
                .makeSceneTransitionAnimation(this,
                        Pair.create(imageView, "cover"),
                        Pair.create(gameTitle, "gameTitle"));
        startActivityForResult(intent, EDIT_GAME_RESULT,
                activityOptions.toBundle());
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_platform_library, menu);

        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handleSearch(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        gamesAdapter.setSortStat(SortStat.NONE);
        switch (id) {
            case R.id.action_filter_alph:
                //Sort A-z
                currentComparator = new GameByNameComparator();
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_alph_desc:
                //Sort A-z desc
                currentComparator = new GameByNameComparator(true);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_physical:
                //Sort physical
                currentComparator = new GameByPhysicalComparator(true);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_alph_physical_desc:
                currentComparator = new GameByPhysicalComparator();
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_timesc:
                //Sort physical
                currentComparator = new GameByTimesPlayedComparator();
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_alph_timesc_desc:
                currentComparator = new GameByTimesPlayedComparator(true);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_hoursmain:
                currentComparator = new GameByHoursStoryComparator();
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_hoursmain_desc:
                currentComparator = new GameByHoursStoryComparator(true);
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_hoursme:
                currentComparator = new GameByHoursMainExtraComparator();
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN_EXTRA);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_hoursme_desc:
                currentComparator = new GameByHoursMainExtraComparator(true);
                gamesAdapter.setSortStat(SortStat.HOURS_MAIN_EXTRA);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_hourscompletionist:
                currentComparator = new GameByHoursCompletionistComparator();
                gamesAdapter.setSortStat(SortStat.HOURS_COMPLETIONIST);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
                return true;
            case R.id.action_filter_hourscompletionist_desc:
                currentComparator = new GameByHoursCompletionistComparator(true);
                gamesAdapter.setSortStat(SortStat.HOURS_COMPLETIONIST);
                sortGamesWithCurrentComparator();
                updateGamesAdapter();
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

    private void handleSearch(String query) {
        games = gamesOriginalList.stream()
                .sorted(currentComparator)
                .filter(game -> isGameNameSimilar(game, query))
                .collect(Collectors.toList());
        updateGamesAdapter();
    }

    private boolean isGameNameSimilar(Game game, String query) {
        String name = game.getName().toLowerCase();
        String shortName = game.getShortName().toLowerCase();
        String queryNormalized = query.toLowerCase();
        return name.contains(queryNormalized) || shortName.contains(queryNormalized);
    }

    private void updateGamesAdapter() {
        gamesAdapter.setGames(games);
        gamesAdapter.notifyDataSetChanged();
    }

    private void sortGamesWithCurrentComparator() {
        games.sort(currentComparator);
    }
}
