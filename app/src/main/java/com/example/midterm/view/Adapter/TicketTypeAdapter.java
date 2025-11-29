package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketTypeAdapter extends RecyclerView.Adapter<TicketTypeAdapter.TicketTypeViewHolder> {
    private List<TicketType> ticketTypes = new ArrayList<>();
    private final Context context;

    public interface OnTicketTypeActionListener {
        void onDeleteClick(TicketType ticketType);
        // Có thể thêm void onEditClick(TicketType ticketType);
    }
    private OnTicketTypeActionListener listener;

    public TicketTypeAdapter(Context context, OnTicketTypeActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTicketTypes(List<TicketType> ticketTypes) {
        List<TicketType> reversedList = new ArrayList<>(ticketTypes);
        java.util.Collections.reverse(reversedList);
        this.ticketTypes = reversedList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_type_preview, parent, false);
        return new TicketTypeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketTypeViewHolder holder, int position) {
        TicketType currentTicket = ticketTypes.get(position);

        holder.tvTicketName.setText(currentTicket.getCode());

        String priceFormatted = String.format(Locale.getDefault(), "%,.0f VND", currentTicket.getPrice());
        holder.tvTicketPrice.setText(priceFormatted);

        // Hiển thị số lượng
        String quantityText = String.format(Locale.getDefault(), "Số lượng: %d", currentTicket.getQuantity());
        holder.tvTicketQuantity.setText(quantityText);

        // Logic xử lý khi click nút Sửa/Xóa (cần được triển khai)
        holder.btnEdit.setOnClickListener(v -> {
            // TODO: Mở Dialog/Activity chỉnh sửa loại vé này
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentTicket); // Truyền đối tượng TicketType
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketTypes.size();
    }

    static class TicketTypeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTicketName, tvTicketPrice, tvTicketQuantity;
        private final ImageButton btnEdit, btnDelete;

        public TicketTypeViewHolder(View itemView) {
            super(itemView);
            tvTicketName = itemView.findViewById(R.id.tv_ticket_name);
            tvTicketPrice = itemView.findViewById(R.id.tv_ticket_price);
            tvTicketQuantity = itemView.findViewById(R.id.tv_ticket_quantity);
            btnEdit = itemView.findViewById(R.id.btn_edit_ticket);
            btnDelete = itemView.findViewById(R.id.btn_delete_ticket);
        }
    }
}
