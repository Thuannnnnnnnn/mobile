package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.EventSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventSectionAdapter extends RecyclerView.Adapter<EventSectionAdapter.SectionViewHolder> {
    private List<EventSection> sections = new ArrayList<>();
    private final Context context;
    private final OnSectionActionListener listener;


    public interface OnSectionActionListener {
        void onDeleteClick(EventSection section);
        // Có thể thêm onEditClick(...)
    }

    public EventSectionAdapter(Context context, OnSectionActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setSections(List<EventSection> newSections) {
        this.sections = newSections;
        notifyDataSetChanged(); // Cập nhật danh sách
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_section_summary, parent, false);
        return new SectionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        EventSection currentSection = sections.get(position);

        holder.tvSectionName.setText(currentSection.name);

        // Hiển thị chi tiết dựa trên loại khu vực
        if ("seated".equals(currentSection.sectionType)) {
            // VD: Có ghế ngồi - Sức chứa: 200 (10x20)
            String details = String.format(Locale.getDefault(),
                    "Có ghế ngồi - Sức chứa: %d (%d x %d)",
                    currentSection.capacity,
                    currentSection.mapTotalRows != null ? currentSection.mapTotalRows : 0,
                    currentSection.mapTotalCols != null ? currentSection.mapTotalCols : 0
            );
            holder.tvSectionDetails.setText(details);
        } else {
            // VD: Khu đứng - Sức chứa: 1000
            String details = String.format(Locale.getDefault(),
                    "Khu đứng - Sức chứa: %d",
                    currentSection.capacity
            );
            holder.tvSectionDetails.setText(details);
        }

        // Bắt sự kiện click xóa
        holder.iconDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentSection);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }
    static class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSectionName;
        private final TextView tvSectionDetails;
        private final ImageView iconDelete;
        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionName = itemView.findViewById(R.id.tv_section_name);
            tvSectionDetails = itemView.findViewById(R.id.tv_section_details);
            iconDelete = itemView.findViewById(R.id.icon_delete_section);
        }
    }
}