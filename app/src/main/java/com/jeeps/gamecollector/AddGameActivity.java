package com.jeeps.gamecollector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.Platform;
import com.jeeps.gamecollector.model.Publisher;
import com.squareup.picasso.Picasso;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddGameActivity extends AppCompatActivity {

    private static final String TAG = "ADD_GAME_ACTIVITY";

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

    private int mPlatformID;
    private Platform mCurrentPlatform;
    private Uri currImageURI;
    private Uri gameImageURI;

    private StorageReference mStorageRef;
    private Context mContext;
    private FirebaseDatabase mDatabase;
    private Format mDateFormatter;
    private String mInputPublisher;
    private List<Publisher> mPublishers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mContext = this;

        //Formatter for file names
        mDateFormatter = new SimpleDateFormat("dd:HH:mm:ss");
        //Database
        mDatabase = FirebaseDatabase.getInstance();

        //Get current platform
        getCurrentPlatform();

        //Select image for cover
        mGameCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // To open up a gallery browser
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);
            }
        });


        //Get publishers for spinner
        populateSpinner();
        //Add publishers
        mAddPublisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptForPublisher();
            }
        });

        //Save game
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGame();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void populateSpinner() {
        final DatabaseReference publishers = mDatabase.getReference("library/publishers/");
        //Publisher publisher = new Publisher("Nintendo");
        //publishers.child("0").setValue(publisher);

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
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
                        android.R.layout.simple_spinner_item, publisherNames);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mPublishersSpinner.setAdapter(dataAdapter);
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
        String name = mNameEdit.getText().toString();
        String publisher = mPublishersSpinner.getSelectedItem().toString();

        //Create game
        final Game game = new Game(name, publisher, "", mCurrentPlatform.getName());
        final String fileName = mDateFormatter.format(game.getDateAdded());

        //Start uploading cover to firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //Create name with current date
        StorageReference riversRef = mStorageRef.child("gameCovers/" + fileName + ".png");

        if (currImageURI != null) {
            riversRef.putFile(currImageURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            gameImageURI = taskSnapshot.getDownloadUrl();
                            String imageUri = "";
                            if (gameImageURI.toString() != null)
                                imageUri = gameImageURI.toString();
                            game.setImageUri(imageUri);

                            //Add game to database
                            DatabaseReference gamesDB = mDatabase.getReference("library/games/");
                            //gamesDB.child(mCurrentPlatform.getId() + ""/* + "_" + fileName*/).setValue(game);

                            Map<String, Object> gameValues = game.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();

                            String key = gamesDB.child(mCurrentPlatform.getId() + "").push().getKey();

                            childUpdates.put(mCurrentPlatform.getId() + "/" + key, gameValues);
                            gamesDB.updateChildren(childUpdates);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });

            //Display success message
            Toast.makeText(AddGameActivity.this, "Game added!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Image can't be null", Toast.LENGTH_SHORT).show();
        }



        //Close activity
        finish();
    }

    private void getCurrentPlatform() {
        Intent intent = getIntent();
        mPlatformID = intent.getIntExtra(PlatformLibraryActivity.CURRENT_PLATFORM, 0);

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
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        mInputPublisher = value.toString();

                        //Add publisher to database
                        final DatabaseReference publishers = mDatabase.getReference("library/publishers/");
                        Publisher publisher = new Publisher(mInputPublisher);
                        publishers.child(mPublishers.size() + "").setValue(publisher);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

}
