package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.jeeps.gamecollector.model.CurrentUser;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.Platform;
import com.jeeps.gamecollector.model.Publisher;
import com.jeeps.gamecollector.model.igdb.GameCoverIG;
import com.jeeps.gamecollector.model.igdb.GameIG;
import com.jeeps.gamecollector.services.api.ApiClient;
import com.jeeps.gamecollector.services.api.GameService;
import com.jeeps.gamecollector.services.igdb.IgdbApiClient;
import com.jeeps.gamecollector.services.igdb.IgdbService;
import com.jeeps.gamecollector.utils.FileUtils;
import com.jeeps.gamecollector.utils.IgdbUtils;
import com.jeeps.gamecollector.utils.UserUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddGameActivity extends AppCompatActivity {

    private static final String TAG = "ADD_GAME_ACTIVITY";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 2929;

    @BindView(R.id.game_cover) ImageView mGameCover;
    @BindView(R.id.game_name_edit) EditText nameEdit;
    @BindView(R.id.game_shortname_edit) EditText shortNameEdit;
    @BindView(R.id.platform_game_edit) EditText platformEdit;
    @BindView(R.id.game_publisher_spinner) Spinner mPublishersSpinner;
    @BindView(R.id.add_publisher_button) ImageView mAddPublisher;
    @BindView(R.id.radio_group) RadioGroup mRadioGroup;
    @BindView(R.id.radio_digital) RadioButton mRadioDigital;
    @BindView(R.id.radio_physical) RadioButton radioPhysical;
    @BindView(R.id.card_game_completed_selector) NumberPicker mNumberPicker;
    @BindView(R.id.fab) FloatingActionButton fab;

    private SharedPreferences sharedPreferences;
    private CurrentUser currentUser;

    private String platformId;
    private String platformName;
    private Platform mCurrentPlatform;
    private Uri currImageURI;
    private Uri gameImageURI;
    private String previousUri;

    private StorageReference mStorageRef;
    private Context context;
    private FirebaseDatabase mDatabase;
    private Format mDateFormatter;
    private String mInputPublisher;
    private int timesCompleted;
    private String selectedGameId;
    private List<Publisher> mPublishers;
    private DatabaseReference mGamesDB;
    private ArrayAdapter<String> mSpinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        context = this;

        //Change title
        getSupportActionBar().setTitle("Add New Game");

        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);

        //Get intent contents
        Intent intent = getIntent();
        platformId = intent.getStringExtra(PlatformLibraryActivity.CURRENT_PLATFORM);
        platformName = intent.getStringExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME);
        selectedGameId = intent.getStringExtra(PlatformLibraryActivity.SELECTED_GAME_KEY);

        //Formatter for file names
        mDateFormatter = new SimpleDateFormat("dd:HH:mm:ss");
        //Select physical radio button by default
        mRadioGroup.check(radioPhysical.getId());

        //Number picker
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(10);
        mNumberPicker.setOnValueChangedListener((numberPicker, i, i1) -> timesCompleted = i1);

        //Database
        mDatabase = FirebaseDatabase.getInstance();

        //Get current platform
        platformEdit.setText(platformName);

        //Select image for cover
        mGameCover.setOnClickListener(view -> {
            // To open up a gallery browser
            Intent intent1 = new Intent();
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent1, "Select Picture"),1);
        });

        //Get publishers for spinner
//        populateSpinner();
        //Add publishers
        mAddPublisher.setOnClickListener(view -> promptForPublisher());

        // Edit mode
        if (selectedGameId != null) {
            //Selected game is being edited
            getSupportActionBar().setTitle("Edit Game");
            fab.setImageResource(R.drawable.edit);
        }

        fab.setOnClickListener(view -> {
//            if (selectedGameId != null)
//                deleteGame();
            saveGame();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void mapSelectedGameToFields() {
        //Get selected game
        DatabaseReference gameReference = mDatabase.getReference("library/games/" + platformId + "/" + selectedGameId);
        gameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                HashMap<String, Object> map = dataSnapshot.getValue(genericTypeIndicator);
                if (map != null) {
                    Game game = Game.mapToGame(map);
                    System.out.println(game);

                    //Set image
                    Picasso.with(context).load(game.getImageUri()).into(mGameCover);
                    mGameCover.setAlpha(1f);
                    mGameCover.setBackgroundColor(Color.parseColor("#99cccccc"));
                    previousUri = game.getImageUri();
                    //Set name
                    nameEdit.setText(game.getName());
                    //Set physical or digital
                    if (!game.isPhysical())
                        mRadioGroup.check(mRadioDigital.getId());
                    //Set times commpleted
                    mNumberPicker.setValue(game.getTimesCompleted());
                    //Set publisher in spinner
                    if (mSpinnerAdapter != null) {
                        int spinnerPosition = mSpinnerAdapter.getPosition(game.getPublisher());
                        mPublishersSpinner.setSelection(spinnerPosition);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void populateSpinner() {
        final DatabaseReference publishers = mDatabase.getReference("library/publishers/");

        // Read from the database
        publishers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<List<Publisher>> genericTypeIndicator = new GenericTypeIndicator<List<Publisher>>() {};
                mPublishers = dataSnapshot.getValue(genericTypeIndicator);

                //Get publishers names
                List<String> publisherNames = new ArrayList<>();
                for (Publisher publisher : mPublishers)
                    publisherNames.add(publisher.getName());
                //Add publishers to spinner
                mSpinnerAdapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, publisherNames);
                mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mPublishersSpinner.setAdapter(mSpinnerAdapter);

                // In case a game is being edited, map the values after this finishes
                if (selectedGameId != null)
                    mapSelectedGameToFields();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void saveGame() {
        //Get values from text fields
        String name = nameEdit.getText().toString();
        String shortName = shortNameEdit.getText().toString();
//        String publisher = mPublishersSpinner.getSelectedItem().toString();
        String publisher = "TEMP";
        String publisherId = "TEMP";

        //Get value from radio button
        boolean isPhysical = radioPhysical.isChecked();

        //Create game
        Game game = new Game("",
                isPhysical, name, shortName, platformId, platformName, publisherId, publisher);
        game.setTimesCompleted(timesCompleted);
        // Set selected image URI if selected
        if (currImageURI != null) {
            game.setImageUri(currImageURI.toString());
            // Post game
            postGame(game);
        } else {
            // Get cover from IGDB
            IgdbService igdbService = IgdbApiClient.createService(IgdbService.class);
            Call<List<GameIG>> searchGames = igdbService.searchGames(IgdbUtils.getSearchGamesQuery(game.getName()));
            searchGames.enqueue(new Callback<List<GameIG>>() {
                @Override
                public void onResponse(Call<List<GameIG>> call, Response<List<GameIG>> response) {
                    if (response.isSuccessful()) {
                        // Get games
                        List<GameIG> gamesIG = response.body();
                        if (gamesIG != null)
                            if (!gamesIG.isEmpty()) {
                                // Exclude DLC
                                Optional<GameIG> selectedGame = gamesIG.stream()
                                        .filter(gameIG -> gameIG.getCategory() != 1)
                                        .findFirst();
                                if (selectedGame.isPresent()) {
                                    // Get cover for game
                                    Call<List<GameCoverIG>> getGameCover =
                                            igdbService.getImageCoverById(IgdbUtils.getCoverImageQuery(selectedGame.get().getCover()));

                                    getGameCover.enqueue(new Callback<List<GameCoverIG>>() {
                                        @Override
                                        public void onResponse(Call<List<GameCoverIG>> call, Response<List<GameCoverIG>> response) {
                                            if (response.isSuccessful()) {
                                                List<GameCoverIG> gameCoverIGS = response.body();
                                                if (gameCoverIGS != null)
                                                    if (!gameCoverIGS.isEmpty())
                                                        game.setImageUri(gameCoverIGS.get(0).getUrl());
                                            }
                                            postGame(game);
                                        }

                                        @Override
                                        public void onFailure(Call<List<GameCoverIG>> call, Throwable t) {
                                            Log.e(TAG, "There was an error in the request to IGDB");
                                            postGame(game);
                                        }
                                    });
                                } else postGame(game);
                            } else postGame(game);
                        else postGame(game);
                    } else {
                        Log.e(TAG, "There was an error in the request to IGDB");
                        postGame(game);
                    }
                }

                @Override
                public void onFailure(Call<List<GameIG>> call, Throwable t) {
                    Log.e(TAG, "There was an error finding a cover from IGDB");
                    postGame(game);
                }
            });
        }
    }

    private void postGame(Game game) {
        GameService gameService = ApiClient.createService(GameService.class);
        Call<Game> postGame = gameService.postGame("Bearer " + currentUser.getToken(), game);
        postGame.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(Call<Game> call, Response<Game> response) {
                if (response.isSuccessful()) {
                    Game game = response.body();
                    Intent result = new Intent();
                    result.putExtra(PlatformLibraryActivity.NEW_GAME, game);
                    setResult(PlatformLibraryActivity.ADD_GAME_RESULT, result);
                    if (currImageURI != null) {
                        try {
                            uploadImageCover(FileUtils.compressImage(context, "temp.png", currImageURI),
                                            game.getId());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "There was an error uploading the image");
                            finish();
                        }
                    } else
                        finish();
                } else {
                    Log.e(TAG, "Authentication error");
                    Toast.makeText(context, "There was an error when adding the game, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Game> call, Throwable t) {
                Log.e(TAG, "Post game request failed");
                Toast.makeText(context, "There was an error when adding the game, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // To handle when an image is selected from the browser, add the following to your Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                currImageURI = data.getData();
                //Load local image for displaying purposes
                Picasso.with(context).load(currImageURI).into(mGameCover);
                mGameCover.setAlpha(1f);
                mGameCover.setBackgroundColor(Color.parseColor("#99cccccc"));
            }
        }
    }

    private void promptForPublisher() {
        //Prompt for publisher name
        final EditText input = new EditText(this);
        new AlertDialog.Builder(AddGameActivity.this)
                .setTitle("Add publisher")
                .setMessage("Name")
                .setView(input)
                .setPositiveButton("Ok", (dialog, whichButton) -> {
                    Editable value = input.getText();
                    mInputPublisher = value.toString();

                    //Add publisher to database
                    final DatabaseReference publishers = mDatabase.getReference("library/publishers/");
                    Publisher publisher = new Publisher(mInputPublisher);
                    publishers.child(mPublishers.size() + "").setValue(publisher);
                }).setNegativeButton("Cancel", (dialog, whichButton) -> {
                    // Do nothing.
                }).show();
    }

    private void uploadImageCover(File imageFile, String gameId) {
        GameService platformService = ApiClient.createService(GameService.class);
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/png"),
                imageFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        Call<ResponseBody> uploadCall = platformService.uploadGameCover(
                "Bearer " + currentUser.getToken(), gameId, body);
        uploadCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful())
                    Log.i(TAG, "Image cover uploaded correctly");
                else
                    Snackbar.make(fab, "There was an error uploading the image", Snackbar.LENGTH_SHORT);
                if (imageFile.exists())
                    imageFile.delete();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(fab, "There was an error uploading the image", Snackbar.LENGTH_SHORT);
                if (imageFile.exists())
                    imageFile.delete();
                finish();
            }
        });
    }
}
