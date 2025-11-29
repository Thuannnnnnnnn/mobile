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
public class TicketPriceSummaryAdapter extends RecyclerView.Adapter<TicketPriceSummaryAdapter.TicketViewHolder> {

    private List<TicketType> ticketTypes = new ArrayList<>();

    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_type_management, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketType ticket = ticketTypes.get(position);

        holder.tvTicketName.setText(ticket.getCode());
        holder.tvTicketPrice.setText(formatPrice(ticket.getPrice()));
        String soldText = String.format(Locale.getDefault(),
                "Đã bán: %d vé",
                ticket.getSoldQuantity()
        );
        holder.tvQuantitySold.setText(soldText);

        // Tính toán và gán số lượng còn lại
        int totalQuantity = ticket.getQuantity();
        int soldQuantity = ticket.getSoldQuantity();
        int remainingQuantity = totalQuantity - soldQuantity;

        String remainingText = String.format(Locale.getDefault(),
                "Còn lại: %d vé (Tổng: %d)",
                remainingQuantity,
                totalQuantity
        );
        holder.tvQuantityRemaining.setText(remainingText);
    }

    @Override
    public int getItemCount() {
        return ticketTypes.size();
    }

    // Hàm format giá tiền (giữ nguyên)
    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketName;
        TextView tvTicketPrice;
        TextView tvQuantitySold;
        TextView tvQuantityRemaining;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTicketName = itemView.findViewById(R.id.tv_ticket_name);
            tvTicketPrice = itemView.findViewById(R.id.tv_ticket_price);
            tvQuantitySold = itemView.findViewById(R.id.tv_quantity_sold);
            tvQuantityRemaining = itemView.findViewById(R.id.tv_quantity_remaining);
        }
    }
}