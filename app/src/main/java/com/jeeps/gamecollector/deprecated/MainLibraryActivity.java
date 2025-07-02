package com.jeeps.gamecollector.deprecated;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.deprecated.model.CurrentUser;
import com.jeeps.gamecollector.deprecated.services.api.ApiClient;
import com.jeeps.gamecollector.deprecated.services.api.UserService;
import com.jeeps.gamecollector.deprecated.services.igdb.IgdbApiClient;
import com.jeeps.gamecollector.deprecated.utils.UserUtils;
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform;
import com.jeeps.gamecollector.remaster.data.model.data.user.User;
import com.jeeps.gamecollector.remaster.data.model.data.user.UserDetails;
import com.jeeps.gamecollector.remaster.ui.adapters.PlatformsListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainLibraryActivity extends AppCompatActivity {

    private static final String TAG = MainLibraryActivity.class.getSimpleName();
    protected static final int RC_SIGN_IN = 420;
    public static final int ADD_PLATFORM_RESULT = 13;
    public static final int EDIT_PLATFORM_RESULT = 97;

    RecyclerView platformsRecyclerView;
    ProgressBar mProgressBar;
    FloatingActionButton fab;

    private Context context;
    private SharedPreferences sharedPreferences;

    private FirebaseUser user;
    private PlatformsListAdapter platformsAdapter;
    private List<Platform> platforms;
    private CurrentUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_library);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Platforms");

        // Get Firestore instance
        db = FirebaseFirestore.getInstance();

        context = this;
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);

        //Show progressbar
        mProgressBar.setVisibility(View.VISIBLE);

        initializePlatformsAdapter();
        checkUserLogin();

        // Authenticate with twitch API for IGDB
        IgdbApiClient.setToken();

        fab.setOnClickListener(view -> {
            // Create platform activity
            Intent intent = new Intent(this, AddPlatformActivityOld.class);
            startActivityForResult(intent, ADD_PLATFORM_RESULT);
        });
    }

    private void checkUserLogin() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Refresh token
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            UserUtils.updateToken(context, sharedPreferences, task.getResult().getToken());
                            populatePlatforms();
                        } else
                            promptUserLogin();
                    });
        } else
            promptUserLogin();
    }

    private void promptUserLogin() {
        // Sign in auth providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());
        // Launch sign in activity
        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.login_text_logo)
                    .build(),
                RC_SIGN_IN);
    }

    private void signOut() {
        if (platforms != null && platformsAdapter != null) {
            platforms.clear();
            platformsAdapter.notifyDataSetChanged();
        }
        mProgressBar.setVisibility(View.VISIBLE);
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show();
                    promptUserLogin();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_stats) {
//            Intent intent = new Intent(this, StatsActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (id == R.id.action_logout) {
//            signOut();
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Receive logged user
        if (requestCode == RC_SIGN_IN) {
            IdpResponse authResponse = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.getIdToken(true).addOnCompleteListener(task -> {
                    // Save current user data
                    String token = task.getResult().getToken();
                    String uid = user.getUid();

                    // Save user details if it's a new user
                    if (authResponse.isNewUser()) {
                        // Create new user details in DB
                        String email = authResponse.getEmail();
                        String newUsername = email.split("@")[0].replaceAll("\\.", "");
                        UserService userService = ApiClient.createService(UserService.class);
                        Call<ResponseBody> signupUserDetails = userService.signupUserdetails(new User(uid, newUsername, email));
                        signupUserDetails.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful())
                                    storeCurrentUser(token, uid);
                                else
                                    Log.e(TAG, "There was an error registering user details");
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e(TAG, "Failed to request signupUserdetails");
                            }
                        });
                    } else
                        storeCurrentUser(token, uid);
                });
            } else {
                // Error signing in
                Toast.makeText(context, "There was an error signing you in, Please try again", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ADD_PLATFORM_RESULT) {
            if (resultCode == RESULT_OK) {
                // Add platform
                Platform platform = (Platform) data.getSerializableExtra(AddPlatformActivityOld.PLATFORM);
                Snackbar.make(fab, "Successfully added platform", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == EDIT_PLATFORM_RESULT) {
            if (resultCode == RESULT_OK) {
                Platform platform = (Platform) data.getSerializableExtra(AddPlatformActivityOld.PLATFORM);
                int platformPosition = data.getIntExtra(AddPlatformActivityOld.EDITED_PLATFORM_POSITION, -1);
            }
        }
    }

    private void storeCurrentUser(String token, String uid) {
        // Get username from service
        UserService userService = ApiClient.createService(UserService.class);
        Call<UserDetails> call = userService.getUser("Bearer " + token);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        User currentUser = response.body().getCredentials();
                        UserUtils.saveCurrentUserData(
                                context, sharedPreferences,
                                currentUser.getUsername(), uid, token);
                        populatePlatforms();
                    }
                } else {
                    Log.e(TAG, "Authentication error on login");
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e(TAG, "Failed retrieving user details");
            }
        });
    }

    private void initializePlatformsAdapter() {
        platforms = new ArrayList<>();
        // Configure recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        platformsRecyclerView.setLayoutManager(layoutManager);
        platformsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // Create adapter
        platformsAdapter = new PlatformsListAdapter(this, platforms);
        platformsRecyclerView.setAdapter(platformsAdapter);
        platformsAdapter.notifyDataSetChanged();
    }

    private void populatePlatforms() {
        // Load current user
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);
        // Load user platforms
        db.collection("platforms")
                .whereEqualTo("user", currentUser.getUsername())
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    //Hide progressbar
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Log.e(TAG, "There was an error retrieving user platforms");
                        Toast.makeText(context, "An error has occurred, please try again", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    platforms.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Platform platform = doc.toObject(Platform.class);
                        platform.setId(doc.getId());
                        platforms.add(platform);
                    }
                    // Sort by name
                    platforms.sort(Comparator.comparing(p -> p.getName().toLowerCase()));
                    // Update adapter
                    platformsAdapter.notifyDataSetChanged();
                });
    }
}
