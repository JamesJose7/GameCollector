package com.jeeps.gamecollector.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.model.Platform;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by jeeps on 12/23/2017.
 */

public class PlatformsListAdapter extends ArrayAdapter<Platform> {

    private Context mContext;

    public PlatformsListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
    }

    public PlatformsListAdapter(Context context, int resource, List<Platform> items) {
        super(context, resource, items);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.platform_list_item, null);
        }

        Platform platform = getItem(position);

        if (platform != null) {
            TextView background = v.findViewById(R.id.platform_background_color);
            ImageView logo = v.findViewById(R.id.platform_image);
            TextView name = v.findViewById(R.id.platform_name);

            if (background != null)
                background.setBackgroundColor(Color.parseColor(platform.getColor()));

            if (logo != null)
                Picasso.with(mContext).load(platform.getImageUri()).into(logo);

            if (name != null)
                name.setText(platform.getName());
        }

        return v;
    }

}
