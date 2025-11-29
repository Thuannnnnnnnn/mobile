package com.example.midterm.view.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketTypeManagementAdapter extends RecyclerView.Adapter<TicketTypeManagementAdapter.ViewHolder> {
    private List<TicketType> ticketTypes = new ArrayList<>();
    private OnTicketTypeClickListener listener;

    public interface OnTicketTypeClickListener {
        void onTicketTypeClick(TicketType ticketType);
    }

    public void setOnTicketTypeClickListener(OnTicketTypeClickListener listener) {
        this.listener = listener;
    }

    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes != null ? ticketTypes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_type_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketType ticketType = ticketTypes.get(position);
        holder.bind(ticketType);
    }

    @Override
    public int getItemCount() {
        return ticketTypes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTicketName;
        private TextView tvTicketPrice;
        private TextView tvQuantitySold;
        private TextView tvQuantityRemaining;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketName = itemView.findViewById(R.id.tv_ticket_name);
            tvTicketPrice = itemView.findViewById(R.id.tv_ticket_price);
            tvQuantitySold = itemView.findViewById(R.id.tv_quantity_sold);
            tvQuantityRemaining = itemView.findViewById(R.id.tv_quantity_remaining);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTicketTypeClick(ticketTypes.get(getAdapterPosition()));
                }
            });
        }

        public void bind(TicketType ticketType) {
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

            tvTicketName.setText(ticketType.getCode());
            tvTicketPrice.setText(currencyFormat.format(ticketType.getPrice()) + " VNĐ");
            tvQuantitySold.setText("Đã bán: " + ticketType.getSoldQuantity() + " vé");

            int remaining = ticketType.getQuantity() - ticketType.getSoldQuantity();
            tvQuantityRemaining.setText("Còn lại: " + remaining + " vé (Tổng: " + ticketType.getQuantity() + ")");
        }
    }
}
