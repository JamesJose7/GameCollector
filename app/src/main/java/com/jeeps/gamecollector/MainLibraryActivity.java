package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jeeps.gamecollector.adapters.PlatformsListAdapter;
import com.jeeps.gamecollector.model.CurrentUser;
import com.jeeps.gamecollector.model.Platform;
import com.jeeps.gamecollector.model.User;
import com.jeeps.gamecollector.model.UserDetails;
import com.jeeps.gamecollector.services.api.ApiClient;
import com.jeeps.gamecollector.services.api.PlatformService;
import com.jeeps.gamecollector.services.api.UserService;
import com.jeeps.gamecollector.utils.UserUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainLibraryActivity extends AppCompatActivity {

    private static final String TAG = MainLibraryActivity.class.getSimpleName();
    protected static final int RC_SIGN_IN = 420;
    protected static final int ADD_PLATFORM_RESULT = 13;
    protected static final int EDIT_PLATFORM_RESULT = 97;

    @BindView(R.id.platforms_list) ListView platformsListView;
    @BindView(R.id.platforms_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.fab) FloatingActionButton fab;

    private Context context;
    private SharedPreferences sharedPreferences;

    private FirebaseUser user;
    private PlatformsListAdapter platformsAdapter;
    private List<Platform> platforms;
    private CurrentUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_library);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Platforms");
        context = this;
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);

        //Show progressbar
        mProgressBar.setVisibility(View.VISIBLE);

        checkUserLogin();

        fab.setOnClickListener(view -> {
            // Create platform activity
            Intent intent = new Intent(this, AddPlatformActivity.class);
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
                        }
                    });
        } else
            promptUserLogin();
    }

    private void promptUserLogin() {
        // Sign in auth providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());
        // Launch sign in activity
        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.switch_cover)
                    .build(),
                RC_SIGN_IN);
    }

    private void signOut() {
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
        } else if (id == R.id.action_logout) {
            signOut();
        }

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
                        String newUsername = email.split("@")[0];
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
                    }

                });
            } else {
                // Error signing in
                Toast.makeText(context, "There was an error signing you in, Please try again", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ADD_PLATFORM_RESULT) {
            if (resultCode == RESULT_OK) {
                // Add platform
                Platform platform = (Platform) data.getSerializableExtra(AddPlatformActivity.PLATFORM);
                platforms.add(platform);
                platforms.sort((p1, p2) -> p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase()));
                platformsAdapter.notifyDataSetChanged();
                Snackbar.make(fab, "Successfully added platform", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == EDIT_PLATFORM_RESULT) {
            if (resultCode == RESULT_OK) {
                Platform platform = (Platform) data.getSerializableExtra(AddPlatformActivity.PLATFORM);
                int platformPosition = data.getIntExtra(AddPlatformActivity.EDITED_PLATFORM_POSITION, -1);
                // Change old platform with the new one
                if (platformPosition >= 0) {
                    platforms.remove(platformPosition);
                    platforms.add(platformPosition, platform);
                    platformsAdapter.notifyDataSetChanged();
                }
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

    private void populatePlatforms() {
        // Load current user
        currentUser = UserUtils.getCurrentUser(context, sharedPreferences);
        // Load user platforms
        PlatformService platformService = ApiClient.createService(PlatformService.class);
        Call<List<Platform>> getPlatformsByUser = platformService.getPlatformsByUser("Bearer " + currentUser.getToken());
        getPlatformsByUser.enqueue(new Callback<List<Platform>>() {
            @Override
            public void onResponse(Call<List<Platform>> call, Response<List<Platform>> response) {
                if (response.isSuccessful()) {
                    platforms = response.body();
                    platformsAdapter = new PlatformsListAdapter(context, R.layout.platform_list_item, platforms);
                    platformsListView.setAdapter(platformsAdapter);
                    // Click listener to open a platform activity
                    platformsListView.setOnItemClickListener((adapterView, view, i, l) -> {
                        //Get selected platform
                        Platform platform = (Platform) adapterView.getItemAtPosition(i);
                        //start games activity with platform id
                        Intent intent = new Intent(context, PlatformLibraryActivity.class);
                        intent.putExtra(PlatformLibraryActivity.CURRENT_PLATFORM, platform.getId());
                        intent.putExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME, platform.getName());
                        startActivity(intent);
                    });
                    platformsListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
                        //Get selected platform
                        Platform platform = (Platform) adapterView.getItemAtPosition(i);
                        // Start add platform activity to edit the selected platform
                        Intent intent = new Intent(context, AddPlatformActivity.class);
                        intent.putExtra(AddPlatformActivity.EDITED_PLATFORM, platform);
                        intent.putExtra(AddPlatformActivity.EDITED_PLATFORM_POSITION, i);
                        startActivityForResult(intent, EDIT_PLATFORM_RESULT);
                        return true;
                    });
                }

                //Hide progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<List<Platform>> call, Throwable t) {
                //Hide progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
                Log.e(TAG, "There was an error retrieving user platforms");
                Toast.makeText(context, "An error has occurred, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
