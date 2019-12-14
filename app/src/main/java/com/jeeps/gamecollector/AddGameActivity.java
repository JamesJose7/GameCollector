package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.Platform;
import com.jeeps.gamecollector.model.Publisher;
import com.jeeps.gamecollector.utils.FileUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;

public class AddGameActivity extends AppCompatActivity {

    private static final String TAG = "ADD_GAME_ACTIVITY";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 2929;

    @BindView(R.id.game_cover)
    ImageView mGameCover;
    @BindView(R.id.game_name_edit)
    EditText mNameEdit;
    @BindView(R.id.platform_game_edit)
    EditText mPlatformEdit;
    @BindView(R.id.game_publisher_spinner)
    Spinner mPublishersSpinner;
    @BindView(R.id.add_publisher_button)
    ImageView mAddPublisher;
    @BindView(R.id.radio_group)
    RadioGroup mRadioGroup;
    @BindView(R.id.radio_digital)
    RadioButton mRadioDigital;
    @BindView(R.id.radio_physical)
    RadioButton mRadioPhysical;
    @BindView(R.id.card_game_completed_selector)
    NumberPicker mNumberPicker;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private int mPlatformID;
    private Platform mCurrentPlatform;
    private Uri currImageURI;
    private Uri gameImageURI;
    private String previousUri;

    private StorageReference mStorageRef;
    private Context mContext;
    private FirebaseDatabase mDatabase;
    private Format mDateFormatter;
    private String mInputPublisher;
    private int mTimesCompleted;
    private String mSelectedGameKey;
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
        mContext = this;

        //Change title
        getSupportActionBar().setTitle("Add New Game");

        //Get intent contents
        Intent intent = getIntent();
        mPlatformID = intent.getIntExtra(PlatformLibraryActivity.CURRENT_PLATFORM, 0);
        mSelectedGameKey = intent.getStringExtra(PlatformLibraryActivity.SELECTED_GAME_KEY);

        //Formatter for file names
        mDateFormatter = new SimpleDateFormat("dd:HH:mm:ss");
        //Select physical radio button by default
        mRadioGroup.check(mRadioPhysical.getId());

        //Number picker
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(10);
        mNumberPicker.setOnValueChangedListener((numberPicker, i, i1) -> mTimesCompleted = i1);

        //Database
        mDatabase = FirebaseDatabase.getInstance();

        //Get current platform
        getCurrentPlatform();

        //Select image for cover
        mGameCover.setOnClickListener(view -> {
            // To open up a gallery browser
            Intent intent1 = new Intent();
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent1, "Select Picture"),1);
        });


        //Get publishers for spinner
        populateSpinner();
        //Add publishers
        mAddPublisher.setOnClickListener(view -> promptForPublisher());

        // Edit mode
        if (mSelectedGameKey != null) {
            //Selected game is being edited
            getSupportActionBar().setTitle("Edit Game");
            fab.setImageResource(R.drawable.edit);
        }

        fab.setOnClickListener(view -> {
            if (mSelectedGameKey != null)
                deleteGame();
            saveGame();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void deleteGame() {
        //Delete game
        DatabaseReference gameReference = mDatabase.getReference("library/games/" + mPlatformID + "/" + mSelectedGameKey);
        gameReference.removeValue();

        if (currImageURI != null) {
            //Delete uploaded image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference gameCoverRef = storageReference.child("gameCovers/" + mSelectedGameKey + ".png");
            gameCoverRef.delete().addOnSuccessListener(aVoid -> {
                // File deleted successfully
                Log.i(AddGameActivity.class.getSimpleName(), "Deleted image successfully");
            }).addOnFailureListener(exception -> {
                // Uh-oh, an error occurred!
                Log.e(AddGameActivity.class.getSimpleName(), "There was an error deleting the image");
            });
        }
    }

    private void mapSelectedGameToFields() {
        //Get selected game
        DatabaseReference gameReference = mDatabase.getReference("library/games/" + mPlatformID + "/" + mSelectedGameKey);
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
                    Picasso.with(mContext).load(game.getImageUri()).into(mGameCover);
                    mGameCover.setAlpha(1f);
                    mGameCover.setBackgroundColor(Color.parseColor("#99cccccc"));
                    previousUri = game.getImageUri();
                    //Set name
                    mNameEdit.setText(game.getName());
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
                mSpinnerAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_item, publisherNames);
                mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mPublishersSpinner.setAdapter(mSpinnerAdapter);

                // In case a game is being edited, map the values after this finishes
                if (mSelectedGameKey != null)
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
        //Games DB Reference
        mGamesDB = mDatabase.getReference("library/games/");
        //Get push key
        final String key = mGamesDB.child(mCurrentPlatform.getId() + "").push().getKey();

        //Get values from text fields
        String name = mNameEdit.getText().toString();
        String publisher = mPublishersSpinner.getSelectedItem().toString();

        //Get value from radio button
        boolean isPhysical = mRadioPhysical.isChecked();

        //Create game
        final Game game = new Game(key, name, publisher, "", mCurrentPlatform.getName(), isPhysical);
        final String fileName = mDateFormatter.format(Long.parseLong(game.getDateAdded()));
        game.setTimesCompleted(mTimesCompleted);

        //Start uploading cover to firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //Create name with current date
        StorageReference gameCovers = mStorageRef.child("gameCovers/" + key + ".png");

        if (currImageURI != null) {
            saveGameOnImageUpload(game, key, gameCovers);
            //Display success message
            Toast.makeText(AddGameActivity.this, "Game added!", Toast.LENGTH_SHORT).show();
        } else {
            if (previousUri != null) {
                game.setImageUri(previousUri);

                //Add game to database

                Map<String, Object> gameValues = game.toMap();
                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put(mCurrentPlatform.getId() + "/" + key, gameValues);
                mGamesDB.updateChildren(childUpdates);
            } else
                Toast.makeText(mContext, "Image can't be null", Toast.LENGTH_SHORT).show();
        }
        //Close activity
        finish();
    }

    private void saveGameOnImageUpload(final Game game, final String key, StorageReference gameCovers) {
        //Get file from URI
        final File localCover = new File(mContext.getFilesDir(), mDateFormatter.format(new Date()));
        try {
            InputStream inputStream = getContentResolver()
                    .openInputStream(currImageURI);
            FileOutputStream fileOutputStream = new FileOutputStream(
                    localCover);
            FileUtils.copyStream(inputStream, fileOutputStream);
            fileOutputStream.close();
            inputStream.close();
            //Compress image
            final File compressedImageFile = new Compressor(this)
                    .setMaxWidth(300)
                    .setQuality(75)
                    .compressToFile(localCover);
            //Get URI from file
            currImageURI = Uri.fromFile(compressedImageFile);

            gameCovers.putFile(currImageURI)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get a URL to the uploaded content
//                        gameImageURI = taskSnapshot.getDownloadUrl();

                        //Delete temp files
                        deleteTempFiles(localCover, compressedImageFile);
                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        // ...
                        //Delete temp files
                        deleteTempFiles(localCover, compressedImageFile);
                    }).continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return gameCovers.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            gameImageURI = task.getResult();
                            String imageUri = "";
                            if (gameImageURI.toString() != null)
                                imageUri = gameImageURI.toString();
                            game.setImageUri(imageUri);

                            //Add game to database

                            Map<String, Object> gameValues = game.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put(mCurrentPlatform.getId() + "/" + key, gameValues);
                            mGamesDB.updateChildren(childUpdates);
                        } else {
                            // Handle failures
                            // ...
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTempFiles(File localCover, File compressedImageFile) {
        if (localCover.exists())
            localCover.delete();
        if (compressedImageFile.exists())
            compressedImageFile.delete();
    }

    private void getCurrentPlatform() {
        DatabaseReference platformsDB = mDatabase.getReference("library/platforms/" + mPlatformID);
        platformsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                mCurrentPlatform = dataSnapshot.getValue(Platform.class);
                mPlatformEdit.setText(mCurrentPlatform.getName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // To handle when an image is selected from the browser, add the following to your Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {

                // currImageURI is the global variable I'm using to hold the content:// URI of the image
                currImageURI = data.getData();

                //Load local image for displaying purposes
                Picasso.with(mContext).load(currImageURI).into(mGameCover);
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

}
