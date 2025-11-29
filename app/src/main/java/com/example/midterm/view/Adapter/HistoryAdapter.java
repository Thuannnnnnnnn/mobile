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

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<String> historyQueryList; // Danh sách các từ khóa (String)
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String query); // Khi nhấn vào text
        void onRemoveClick(String query, int position); // Khi nhấn vào nút xóa
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HistoryAdapter(Context context, List<String> historyQueryList) {
        this.context = context;
        this.historyQueryList = historyQueryList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String query = historyQueryList.get(position);
        holder.tvHistoryQuery.setText(query);

        //Xử lý click vào toàn bộ item (để tìm kiếm lại)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(query);
            }
        });

        // Xử lý click vào nút xóa (btn_remove_history)
        holder.btnRemoveHistory.setOnClickListener(v -> {
            if (listener != null) {
                // Lấy vị trí an toàn
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onRemoveClick(query, currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (historyQueryList != null) ? historyQueryList.size() : 0;
    }

    public void updateData(List<String> newQueries) {
        historyQueryList.clear();
        historyQueryList.addAll(newQueries);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        historyQueryList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, historyQueryList.size());
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryQuery;
        ImageView btnRemoveHistory;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHistoryQuery = itemView.findViewById(R.id.tv_history_query);
            btnRemoveHistory = itemView.findViewById(R.id.btn_remove_history);
        }
    }
}
