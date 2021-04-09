package com.jeeps.gamecollector.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jeeps.gamecollector.AddPlatformActivity;
import com.jeeps.gamecollector.MainLibraryActivity;
import com.jeeps.gamecollector.PlatformLibraryActivity;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.model.Platform;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jeeps on 12/23/2017.
 */

public class PlatformsListAdapter extends RecyclerView.Adapter<PlatformsListAdapter.PlatformsViewHolder> {

    private Activity parentActivity;
    private Context mContext;
    private List<Platform> platforms;

    public PlatformsListAdapter(Activity activity, Context context, List<Platform> platforms) {
        parentActivity = activity;
        mContext = context;
        this.platforms = platforms;
    }

    public class PlatformsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.platform_card) CardView platformCard;
        @BindView(R.id.platform_card_image) ImageView platformImage;
        @BindView(R.id.platform_card_border) View platformBorder;
        @BindView(R.id.platform_card_name) TextView platformName;

        public PlatformsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public PlatformsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.platform_card_layout, parent, false);
        return new PlatformsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatformsViewHolder holder, int position) {
        Platform platform = platforms.get(position);

        if (platform != null) {
            holder.platformBorder.setBackgroundColor(Color.parseColor(platform.getColor()));
//            if (!platform.getColor().equals(Colors.NORMIE_WHITE.getColor()))
//                name.setTextColor(Color.parseColor(Colors.NORMIE_WHITE.getColor()));
//            else
//                name.setTextColor(Color.parseColor("#000000"));

            if (platform.getImageUri() != null)
                if (!platform.getImageUri().isEmpty())
                    Picasso.get().load(platform.getImageUri()).into(holder.platformImage);
                else
                    Picasso.get().load(R.drawable.game_controller).into(holder.platformImage);
            else
                Picasso.get().load(R.drawable.game_controller).into(holder.platformImage);

            holder.platformName.setText(platform.getName());

            // Click listener to open a platform activity
            holder.platformCard.setOnClickListener(view -> {
                //start games activity with platform id
                Intent intent = new Intent(mContext, PlatformLibraryActivity.class);
                intent.putExtra(PlatformLibraryActivity.CURRENT_PLATFORM, platform.getId());
                intent.putExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME, platform.getName());
                mContext.startActivity(intent);
            });
            holder.platformCard.setOnLongClickListener(view -> {
                // Start add platform activity to edit the selected platform
                Intent intent = new Intent(mContext, AddPlatformActivity.class);
                intent.putExtra(AddPlatformActivity.EDITED_PLATFORM, platform);
                intent.putExtra(AddPlatformActivity.EDITED_PLATFORM_POSITION, position);
                parentActivity.startActivityForResult(intent, MainLibraryActivity.EDIT_PLATFORM_RESULT);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return platforms.size();
    }
}
