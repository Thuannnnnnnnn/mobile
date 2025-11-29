package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<String> genreList;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(String genre, boolean isSelected);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void clearSelection() {
        int previousSelected = selectedPosition;
        selectedPosition = -1;
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
    }

    public CategoryAdapter(Context context, List<String> genreList) {
        this.context = context;
        this.genreList = genreList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_icon, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String genre = genreList.get(position);
        holder.categoryChip.setText(genre);

        boolean isSelected = (position == selectedPosition);
        holder.categoryChip.setChecked(isSelected);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int previousSelected = selectedPosition;

                if (selectedPosition == position) {
                    selectedPosition = -1;
                    notifyItemChanged(position);
                    listener.onItemClick(genre, false);
                } else {
                    selectedPosition = position;
                    if (previousSelected != -1) {
                        notifyItemChanged(previousSelected);
                    }
                    notifyItemChanged(position);
                    listener.onItemClick(genre, true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public void updateData(List<String> newGenres) {
        genreList.clear();
        genreList.addAll(newGenres);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        Chip categoryChip;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryChip = itemView.findViewById(R.id.category_chip);
        }
    }
}