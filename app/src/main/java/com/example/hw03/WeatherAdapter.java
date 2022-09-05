package com.example.hw03;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> {


    private final CityWeatherActivity ctx;
    private OnWeatherItemClickListener newOnWeatherItemClickListener;
    ArrayList<ForecastDet> det;

    public WeatherAdapter(ArrayList<ForecastDet> details, CityWeatherActivity cityWeatherActivity) {
        this.det = details;
        this.ctx = cityWeatherActivity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnWeatherItemClickListener onItemClickListener;

        ImageView iv_icon;
        TextView tv_date;

        public MyViewHolder(@NonNull View itemView, OnWeatherItemClickListener onItemClickListener) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_date = itemView.findViewById(R.id.tv_date);

            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout rv_layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(rv_layout, newOnWeatherItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        newOnWeatherItemClickListener = ctx;

        String imageURL = "https://developer.accuweather.com/sites/default/files/"+det.get(position).getDayIcon()+"-s.png";
        Picasso.get().load(imageURL).into(holder.iv_icon);
        holder.tv_date.setText(det.get(position).getDate());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newOnWeatherItemClickListener.onItemClick(position);
            }
        });

    }

    public interface OnWeatherItemClickListener{
        void onItemClick(int position);
    }
    @Override
    public int getItemCount() {
        return det.size();
    }

}
