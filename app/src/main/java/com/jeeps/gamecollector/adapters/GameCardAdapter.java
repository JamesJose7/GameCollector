package com.jeeps.gamecollector.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.model.Game;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by jeeps on 12/25/2017.
 */

public class GameCardAdapter extends RecyclerView.Adapter<GameCardAdapter.MyViewHolder> {
    private Context mContext;
    private List<Game> mGames;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView cover;
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            cover = (ImageView) view.findViewById(R.id.card_game_cover);
            title = (TextView) view.findViewById(R.id.card_game_title);
        }
    }

    public GameCardAdapter(Context context, List<Game> games) {
        mContext = context;
        mGames = games;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_card_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Game game = mGames.get(position);

        //load image cover
        Picasso.with(mContext).load(game.getImageUri()).into(holder.cover);

        holder.title.setText(game.getName());

        /*holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }
}
