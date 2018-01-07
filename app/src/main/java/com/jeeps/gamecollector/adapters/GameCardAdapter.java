package com.jeeps.gamecollector.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.model.Game;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by jeeps on 12/25/2017.
 */

public class GameCardAdapter extends RecyclerView.Adapter<GameCardAdapter.MyViewHolder> {
    private Context mContext;
    private List<Game> mGames;
    private GameCardAdapterListener listener;

    public interface GameCardAdapterListener {
        void deleteSelectedGame(int position);
        void editGame(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView cover;
        public ImageView isDigital;
        public TextView title;
        public ImageView completed;
        public ProgressBar coverProgressBar;

        public MyViewHolder(View view) {
            super(view);
            cover = (ImageView) view.findViewById(R.id.card_game_cover);
            title = (TextView) view.findViewById(R.id.card_game_title);
            isDigital = (ImageView) view.findViewById(R.id.card_is_digital);
            completed = (ImageView) view.findViewById(R.id.card_game_completed);
            coverProgressBar = (ProgressBar) view.findViewById(R.id.cover_progressbar);
        }
    }

    public GameCardAdapter(Context context, List<Game> games, GameCardAdapterListener listener) {
        mContext = context;
        mGames = games;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_card_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Game game = mGames.get(position);

        //Display progress bar
        holder.coverProgressBar.setVisibility(View.VISIBLE);
        //load image cover
        Picasso.with(mContext).load(game.getImageUri()).into(holder.cover, new Callback() {
            @Override
            public void onSuccess() {
                //Hide progress bar when image is finished loading
                holder.coverProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                //Hide progress bar when image is finished loading
                holder.coverProgressBar.setVisibility(View.INVISIBLE);
                //Display temp error message
                Toast.makeText(mContext, "Welp, image couldn't load... whoops", Toast.LENGTH_SHORT).show();
            }
        });
        //Display logo if it's digital
        if (!game.isPhysical())
            holder.isDigital.setVisibility(View.VISIBLE);
        else {
            holder.isDigital.setVisibility(View.GONE);
        }

        //Name
        holder.title.setText(game.getName());

        //turn check green if game is completed
        if (game.getTimesCompleted() > 0)
            holder.completed.setColorFilter(Color.parseColor("#7FFF00"));

        holder.cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.deleteSelectedGame(position);
                return true;
            }
        });

        holder.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.editGame(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }
}
