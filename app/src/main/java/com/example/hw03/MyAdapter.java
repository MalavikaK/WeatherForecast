package com.example.hw03;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


    private final MainActivity ctx;
    private OnItemClickListener newOnItemClickListener;
    ArrayList<City> cities;

    public MyAdapter(ArrayList<City> cityList, MainActivity mainActivity) {
        this.cities = cityList;
        this.ctx = mainActivity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView rv_cityCountry;
        TextView rv_temp;
        TextView rv_updated;
        ImageView imageView;
        OnItemClickListener onItemClickListener;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            rv_cityCountry = itemView.findViewById(R.id.rv_cityCountry);
            rv_temp = itemView.findViewById(R.id.rv_temp);
            rv_updated = itemView.findViewById(R.id.rv_updated);
            imageView = itemView.findViewById(R.id.imageView);
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
        LinearLayout rv_layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.city_list, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(rv_layout, newOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        newOnItemClickListener = ctx;
        holder.rv_cityCountry.setText(cities.get(position).getCityName() + "," + cities.get(position).getCountry());
        holder.rv_temp.setText("Temperature: " + cities.get(position).getTemperature().toString()+" F");
        String pattern2 = "yyyy-MM-dd'T'HH:mm:ssZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern2);
        try {
            Date date = simpleDateFormat.parse(cities.get(position).getDate());
            PrettyTime p = new PrettyTime();
            holder.rv_updated.setText(String.format("Updated: " + p.format(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                newOnItemClickListener.onItemClick(position);
                return true;
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Drawable.ConstantState image = holder.imageView.getDrawable().getConstantState();
                Drawable.ConstantState btnOff  = ctx.getResources().getDrawable(android.R.drawable.btn_star_big_off).getConstantState();
                if(image == btnOff){
                    holder.imageView.setImageDrawable(ctx.getResources().getDrawable(android.R.drawable.btn_star_big_on));
                }else{
                    holder.imageView.setImageDrawable(ctx.getResources().getDrawable(android.R.drawable.btn_star_big_off));
                }
            }
        });
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    @Override
    public int getItemCount() {
        return cities.size();
    }

}
