package com.jeeps.gamecollector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jeeps.gamecollector.model.CurrentUser;
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform;
import com.jeeps.gamecollector.services.api.ApiClient;
import com.jeeps.gamecollector.services.api.PlatformService;
import com.jeeps.gamecollector.utils.PlatformColors;
import com.jeeps.gamecollector.utils.FileUtils;
import com.jeeps.gamecollector.utils.UserUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPlatformActivityOld extends AppCompatActivity {

    public static final String PLATFORM = "PLATFORM";
    public static final String EDITED_PLATFORM = "EDITED PLATFORM";
    public static final String EDITED_PLATFORM_POSITION = "EDITED PLATFORM POSITION";
    private static final String TAG = AddPlatformActivityOld.class.getSimpleName();

    @BindView(R.id.add_platform_layout) ConstraintLayout rootLayout;
    @BindView(R.id.platform_cover) ImageView platformCover;
    @BindView(R.id.platform_name_edit) EditText platformNameInput;
    @BindView(R.id.platform_color_radio_group) RadioGroup colorRadioGroup;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.add_platform_progressbar) ProgressBar progressBar;

    private Context context;

    private Platform platform;
    private Uri currImageURI;
    private SharedPreferences sharedPreferences;
    private CurrentUser currentUser;
    private boolean isEdit = false;
    private boolean isImageEdited = false;
    private int editedPlatformPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_platform);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        context = this;

        platform = new Platform();
        platform.setColor(PlatformColors.NORMIE_WHITE.getColor());

        fab.setOnClickListener(view -> {
            toggleProgressbar(true);
            validatePlatformDetails();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(
                getString(R.string.shared_preferences_global), Context.MODE_PRIVATE);
        currentUser = UserUtils.getCurrentUser(this, sharedPreferences);

        // Get platform if being edited
        Intent intent = getIntent();
        Platform editedPlatform = (Platform) intent.getSerializableExtra(EDITED_PLATFORM);
        editedPlatformPosition = intent.getIntExtra(EDITED_PLATFORM_POSITION, -1);
        if (editedPlatform != null) {
            mapExistingPlatformDetails(editedPlatform);
            isEdit = true;
            // Invalidate picasso cache
            if (platform.getImageUri() != null)
                Picasso.get().invalidate(platform.getImageUri());
        }
    }

    private void mapExistingPlatformDetails(Platform editedPlatform) {
        platform = editedPlatform;
        // Cover image
        if (platform.getImageUri() != null)
            if (!platform.getImageUri().isEmpty())
                Picasso.get().load(editedPlatform.getImageUri()).into(platformCover);
        platformCover.setAlpha(1f);
        platformCover.setBackgroundColor(Color.parseColor("#99cccccc"));
        // Platform name
        platformNameInput.setText(editedPlatform.getName());
        // Get appropriate color
        Arrays.stream(PlatformColors.values())
                .filter(color -> color.getColor().equals(editedPlatform.getColor()))
                .findFirst()
                .ifPresent(color -> colorRadioGroup.check(color.getColorId()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                currImageURI = data.getData();
                // Load local image for displaying purposes
                Picasso.get().load(currImageURI).into(platformCover);
                platformCover.setAlpha(1f);
                platformCover.setBackgroundColor(Color.parseColor("#99cccccc"));
                if (isEdit)
                    isImageEdited = true;
            }
        }
    }

    private void validatePlatformDetails() {
        // Get platform details
        String name = platformNameInput.getText().toString();
        if (name.isEmpty()) {
            dataCheckMessage("Please enter a platform name");
            toggleProgressbar(false);
            return;
        }
        if (currImageURI == null) {
            if (!isEdit) {
                dataCheckMessage("Please select a platform image cover");
                toggleProgressbar(false);
                return;
            }
        }
        platform.setName(name);
        if (!isEdit)
            platform.setImageUri(currImageURI.toString());
        else {
            if (isImageEdited)
                platform.setImageUri(currImageURI.toString());
        }
        postPlatform(isEdit);
    }

    public void onColorPickerClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.color_switch_normiewhite:
                if (checked) platform.setColor(PlatformColors.NORMIE_WHITE.getColor());
                break;
            case R.id.color_switchred:
                if (checked) platform.setColor(PlatformColors.SWITCH_RED.getColor());
                break;
            case R.id.color_xboxgreen:
                if (checked) platform.setColor(PlatformColors.XBOX_GREEN.getColor());
                break;
            case R.id.color_playstationblue:
                if (checked) platform.setColor(PlatformColors.PLAYSTATION_BLUE.getColor());
                break;
        }
    }

    private void dataCheckMessage(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void postPlatform(boolean isEdit) {
        PlatformService platformService = ApiClient.createService(PlatformService.class);
        if (isEdit) {
            Call<Platform> editPlatform = platformService.editPlatform("Bearer " + currentUser.getToken(),
                    platform.getId(), platform);
            editPlatform.enqueue(new Callback<Platform>() {
                @Override
                public void onResponse(Call<Platform> call, Response<Platform> response) {
                    if (response.isSuccessful()) {
                        if (isImageEdited) {
                            // Upload image cover
                            try {
                                uploadImageCover(FileUtils.compressImage(context, "temp.png", currImageURI),
                                        platform.getId());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else
                            returnAddedPlatform();
                    } else {
                        Log.e(TAG, "Authentication error when editing platform");
                        Toast.makeText(context, "Authentication error when editing the platform", Toast.LENGTH_SHORT).show();
                        toggleProgressbar(false);
                    }
                }

                @Override
                public void onFailure(Call<Platform> call, Throwable t) {
                    Log.e(TAG, "Error editing the platform");
                    Toast.makeText(context, "An error occurred when editing the platform", Toast.LENGTH_SHORT).show();
                    toggleProgressbar(false);
                }
            });
        } else {
            Call<Platform> postPlatform = platformService.postPlatform("Bearer " + currentUser.getToken(), platform);
            postPlatform.enqueue(new Callback<Platform>() {
                @Override
                public void onResponse(Call<Platform> call, Response<Platform> response) {
                    if (response.isSuccessful()) {
                        platform = response.body();
                        // Upload image cover
                        try {
                            uploadImageCover(FileUtils.compressImage(context, "temp.png", currImageURI),
                                    platform.getId());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Authentication error when posting platform");
                        Toast.makeText(context, "Authentication error when creating the platform", Toast.LENGTH_SHORT).show();
                        toggleProgressbar(false);
                    }
                }

                @Override
                public void onFailure(Call<Platform> call, Throwable t) {
                    Log.e(TAG, "Error posting platform");
                    Toast.makeText(context, "An error occurred when adding the platform", Toast.LENGTH_SHORT).show();
                    toggleProgressbar(false);
                }
            });
        }
    }

    private void uploadImageCover(File imageFile, String platformId) {
        PlatformService platformService = ApiClient.createService(PlatformService.class);
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/png"),
                imageFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        Call<ResponseBody> uploadCall = platformService.uploadPlatformCover(
                "Bearer " + currentUser.getToken(), platformId, body);
        uploadCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful())
                    Log.i(TAG, "Image cover uploaded correctly");
                else
                    Toast.makeText(context, "There was an error uploading the image", Toast.LENGTH_SHORT).show();
                if (imageFile.exists())
                    imageFile.delete();
                returnAddedPlatform();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "There was an error uploading the image", Toast.LENGTH_SHORT).show();
                if (imageFile.exists())
                    imageFile.delete();
                returnAddedPlatform();
            }
        });
    }

    public void returnAddedPlatform() {
        // Return platform details
        Intent data = new Intent();
        data.putExtra(PLATFORM, platform);
        data.putExtra(EDITED_PLATFORM_POSITION, editedPlatformPosition);
        setResult(RESULT_OK, data);
        finish();
    }

    @OnClick(R.id.platform_cover)
    protected void choosePlatformCover(View v) {
        // To open up a gallery browser
        Intent intent1 = new Intent();
        intent1.setType("image/*");
        intent1.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent1, "Select Picture"),1);
    }

    private void toggleProgressbar(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);
        }
    }
}
