package com.example.midterm.view.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Ticket> tickets = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Ticket ticket);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTicketCode;
        private final TextView tvTicketType;
        private final TextView tvPrice;
        private final TextView tvStatus;
        private final TextView tvPurchaseDate;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketCode = itemView.findViewById(R.id.tv_ticket_code);
            tvTicketType = itemView.findViewById(R.id.tv_ticket_type);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPurchaseDate = itemView.findViewById(R.id.tv_purchase_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(tickets.get(position));
                }
            });
        }

        public void bind(Ticket ticket) {
            tvTicketCode.setText("Mã: " + ticket.getQrCode());
            tvTicketType.setText("Loại vé ID: " + ticket.getTicketTypeID());

            // Price is stored in TicketType, not in Ticket
            tvPrice.setVisibility(View.GONE);

            // Status display
            String status = ticket.getStatus();
            String statusText;
            int statusColor;
            switch (status) {
                case "checked_in":
                    statusText = "Đã check-in";
                    statusColor = 0xFF4CAF50; // Green
                    break;
                case "cancelled":
                    statusText = "Đã hủy";
                    statusColor = 0xFFF44336; // Red
                    break;
                default:
                    statusText = "Chưa sử dụng";
                    statusColor = 0xFF2196F3; // Blue
                    break;
            }
            tvStatus.setText(statusText);
            tvStatus.setTextColor(statusColor);

            tvPurchaseDate.setText("Ngày mua: " + ticket.getPurchaseDate());
        }
    }
}
