package com.awaissaikhu.cloudconnectedaccelometer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    PlacesViewHolder placesViewHolder=null;
    Context context;
    ArrayList<leaderboardModal> InfoArrayList;
    public LeaderboardAdapter(Context context, ArrayList<leaderboardModal> object1s) {
        this.context = context;
        InfoArrayList = object1s;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view;

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboarditem, parent, false);

        PlacesViewHolder placesViewHolder = new PlacesViewHolder(view);


        return placesViewHolder;



    }

    @Override
    public int getItemViewType(int position) {


        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        placesViewHolder = (PlacesViewHolder) holder;
        placesViewHolder.name.setText( InfoArrayList.get(position).name);
        if(InfoArrayList.get(position).score==0.0){

            placesViewHolder.score.setText("N/A");
        }else
        placesViewHolder.score.setText( InfoArrayList.get(position).score+"");

        placesViewHolder.rank.setText(position+1+"");
        Log.e("item set",InfoArrayList.get(position).name);
        placesViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaderboardModal leaderboardModal=InfoArrayList.get(position);
                leaderboardModal.rank=position+1;
                context.startActivity(new Intent(context,FullscreenActivity.class).putExtra("data",leaderboardModal));
            }
        });
    }

    @Override
    public int getItemCount() {
        return InfoArrayList.size();
    }

    public static class PlacesViewHolder extends RecyclerView.ViewHolder  {

        View view;
        TextView name;
        TextView score;
        TextView rank;

        public PlacesViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            name = (TextView) view.findViewById(R.id.name);
            score = (TextView) view.findViewById(R.id.score);
            rank = (TextView) view.findViewById(R.id.rank);


        }

    }



}

