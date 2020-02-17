package com.jeeps.gamecollector;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jeeps.gamecollector.model.Platform;
import com.jeeps.gamecollector.utils.Colors;
import com.jeeps.gamecollector.utils.FileUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPlatformActivity extends AppCompatActivity {

    public static final String PLATFORM = "PLATFORM";
    public static final String COVER_FILE = "COVER_FILE";

    @BindView(R.id.add_platform_layout) ConstraintLayout rootLayout;
    @BindView(R.id.platform_cover) ImageView platformCover;
    @BindView(R.id.platform_name_edit) EditText platformNameInput;

    private Platform platform;
    private Uri currImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_platform);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        platform = new Platform();
        platform.setColor(Colors.NORMIE_WHITE.getColor());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createAndReturnPlatform());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                currImageURI = data.getData();
                // Load local image for displaying purposes
                Picasso.with(this).load(currImageURI).into(platformCover);
                platformCover.setAlpha(1f);
                platformCover.setBackgroundColor(Color.parseColor("#99cccccc"));
            }
        }
    }

    private void createAndReturnPlatform() {
        // Get platform details
        String name = platformNameInput.getText().toString();
        if (name.isEmpty()) {
            dataCheckMessage("Please enter a platform name");
            return;
        }
        platform.setName(name);
        platform.setImageUri(currImageURI.toString());
        try {
            // Return platform details
            Intent data = new Intent();
            data.putExtra(PLATFORM, platform);
            data.putExtra(COVER_FILE, FileUtils.compressImage(this, "temp.png", currImageURI));
            setResult(MainLibraryActivity.ADD_PLATFORM_RESULT, data);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onColorPickerClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.color_switch_normiewhite:
                if (checked) platform.setColor(Colors.NORMIE_WHITE.getColor());
                break;
            case R.id.color_switchred:
                if (checked) platform.setColor(Colors.SWITCH_RED.getColor());
                break;
            case R.id.color_xboxgreen:
                if (checked) platform.setColor(Colors.XBOX_GREEN.getColor());
                break;
            case R.id.color_playstationblue:
                if (checked) platform.setColor(Colors.PLAYSTATION_BLUE.getColor());
                break;
        }
    }

    private void dataCheckMessage(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.platform_cover)
    protected void choosePlatformCover(View v) {
        // To open up a gallery browser
        Intent intent1 = new Intent();
        intent1.setType("image/*");
        intent1.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent1, "Select Picture"),1);
    }
}
