package com.jeeps.gamecollector.adapters;

import static com.jeeps.gamecollector.utils.ColorsUtils.getColorByHoursRange;
import static com.jeeps.gamecollector.utils.FormatUtils.formatDecimal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.jeeps.gamecollector.R;
import com.jeeps.gamecollector.model.Game;
import com.jeeps.gamecollector.model.SortStat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeeps on 12/25/2017.
 */

public class GameCardAdapter extends RecyclerView.Adapter<GameCardAdapter.MyViewHolder> {
    private Context mContext;
    private List<Game> mGames;
    private GameCardAdapterListener listener;
    private SortStat sortStat = SortStat.NONE;

    public GameCardAdapter(Context context, List<Game> games, GameCardAdapterListener listener) {
        mContext = context;
        mGames = new ArrayList<>(games);
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

        //Set progressbar animation
        DoubleBounce doubleBounce = new DoubleBounce();
        holder.coverProgressBar.setIndeterminateDrawable(doubleBounce);

        //Display progressbar
        holder.coverProgressBar.setVisibility(View.VISIBLE);
        //load image cover
        if (!game.getImageUri().isEmpty()) {
            Picasso.get().load(game.getImageUri()).into(holder.cover, new Callback() {
                @Override
                public void onSuccess() {
                    //Hide progress bar when image is finished loading
                    holder.coverProgressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    //Hide progress bar when image is finished loading
                    holder.coverProgressBar.setVisibility(View.INVISIBLE);
                    //Display temp error message
                    //Toast.makeText(mContext, "Welp, image couldn't load... whoops", Toast.LENGTH_SHORT).show();
                }
            });
        } else
            holder.coverProgressBar.setVisibility(View.INVISIBLE);
        //Display logo if it's digital
        if (!game.isPhysical())
            holder.isDigital.setVisibility(View.VISIBLE);
        else {
            holder.isDigital.setVisibility(View.GONE);
        }

        //Name
        holder.title.setText(game.getName());
        if (game.getShortName() != null)
            if (!game.getShortName().isEmpty())
                holder.title.setText(game.getShortName());

        //turn check green if game is completed
        if (game.getTimesCompleted() > 0)
            holder.completed.setColorFilter(Color.parseColor("#7FFF00"));
        else
            holder.completed.setColorFilter(Color.parseColor("#cccccc"));

        holder.cover.setOnLongClickListener(view -> {
            listener.deleteSelectedGame(position);
            return true;
        });

        // Sort stats
        if (sortStat.equals(SortStat.HOURS_MAIN)) {
            holder.sortStatCard.setVisibility(View.VISIBLE);
            double gameplayMainExtra = game.getGameHoursStats() != null ?
                    game.getGameHoursStats().getGameplayMain() : 0;
            formatHourStat(holder.sortStatTv, gameplayMainExtra);
        } else if (sortStat.equals(SortStat.HOURS_MAIN_EXTRA)) {
            holder.sortStatCard.setVisibility(View.VISIBLE);
            double gameplayMainExtra = game.getGameHoursStats() != null ?
                    game.getGameHoursStats().getGameplayMainExtra() : 0;
            formatHourStat(holder.sortStatTv, gameplayMainExtra);
        } else if (sortStat.equals(SortStat.HOURS_COMPLETIONIST)) {
            holder.sortStatCard.setVisibility(View.VISIBLE);
            double gameplayMainExtra = game.getGameHoursStats() != null ?
                    game.getGameHoursStats().getGameplayCompletionist() : 0;
            formatHourStat(holder.sortStatTv, gameplayMainExtra);
        } if (sortStat.equals(SortStat.NONE)) {
            holder.sortStatCard.setVisibility(View.GONE);
        }

        holder.cover.setOnClickListener(view -> listener.editGame(position, holder.cover, holder.title));
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public void setGames(List<Game> games) {
        mGames.clear();
        mGames.addAll(games);
        notifyDataSetChanged();
    }

    public void setSortStat(SortStat sortStat)  {
        this.sortStat = sortStat;
    }

    private void formatHourStat(TextView sortStatTv, double gameplayMainExtra) {
        sortStatTv.setText(
                sortStatTv.getContext().getString(
                        R.string.hours_template,
                        formatDecimal(gameplayMainExtra)));
        sortStatTv.setTextColor(getColorByHoursRange(
                sortStatTv.getContext(), gameplayMainExtra));
    }

    public void removeGameAtPosition(int position) {
        mGames.remove(position);
        notifyItemRemoved(position);
    }

    public void addGameAtPosition(int position, Game selectedGame) {
        mGames.add(position, selectedGame);
        notifyItemInserted(position);
    }

    public interface GameCardAdapterListener {
        void deleteSelectedGame(int position);
        void editGame(int position, View imageView, TextView titleView);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView cover;
        public ImageView isDigital;
        public TextView title;
        public ImageView completed;
        public ProgressBar coverProgressBar;
        public CardView sortStatCard;
        public TextView sortStatTv;

        public MyViewHolder(View view) {
            super(view);
            cover = view.findViewById(R.id.card_game_cover);
            title = view.findViewById(R.id.card_game_title);
            isDigital = view.findViewById(R.id.card_is_digital);
            completed = view.findViewById(R.id.card_game_completed);
            coverProgressBar = view.findViewById(R.id.cover_progressbar);
            sortStatCard = view.findViewById(R.id.sort_stat_card);
            sortStatTv = view.findViewById(R.id.sort_stat_tv);
        }
    }
}
