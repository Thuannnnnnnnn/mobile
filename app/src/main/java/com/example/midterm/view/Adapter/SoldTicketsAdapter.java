package com.example.midterm.view.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.dto.TicketWithDetails;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SoldTicketsAdapter extends RecyclerView.Adapter<SoldTicketsAdapter.ViewHolder> {
    private List<TicketWithDetails> tickets = new ArrayList<>();
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(TicketWithDetails ticket);
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.listener = listener;
    }

    public void setTickets(List<TicketWithDetails> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_sold, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketWithDetails ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBuyerName;
        private TextView tvTicketTypeName;
        private TextView tvPurchaseDate;
        private Chip chipTicketStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBuyerName = itemView.findViewById(R.id.tv_buyer_name);
            tvTicketTypeName = itemView.findViewById(R.id.tv_ticket_type_name);
            tvPurchaseDate = itemView.findViewById(R.id.tv_purchase_date);
            chipTicketStatus = itemView.findViewById(R.id.chip_ticket_status);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTicketClick(tickets.get(getAdapterPosition()));
                }
            });
        }

        public void bind(TicketWithDetails ticket) {
            tvBuyerName.setText(ticket.getBuyerName() != null ? ticket.getBuyerName() : "Khách hàng");
            tvTicketTypeName.setText("Loại vé: " + ticket.getTicketTypeCode());

            String purchaseDate = formatDate(ticket.getPurchaseDate());
            tvPurchaseDate.setText("Mua: " + purchaseDate);

            updateStatusChip(ticket.getStatus());
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "N/A";
            }
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return date != null ? outputFormat.format(date) : dateString;
            } catch (Exception e) {
                return dateString;
            }
        }

        private void updateStatusChip(String status) {
            if (status == null) {
                status = "booked";
            }

            switch (status) {
                case "checked_in":
                    chipTicketStatus.setText("Đã check-in");
                    chipTicketStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                    break;
                case "cancelled":
                    chipTicketStatus.setText("Đã hủy");
                    chipTicketStatus.setChipBackgroundColorResource(android.R.color.holo_red_dark);
                    break;
                case "booked":
                default:
                    chipTicketStatus.setText("Đã đặt");
                    chipTicketStatus.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                    break;
            }
        }
    }
}
