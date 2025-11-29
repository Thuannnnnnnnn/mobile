package com.example.midterm.view.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketDetailAdapter extends RecyclerView.Adapter<TicketDetailAdapter.TicketViewHolder> {

    private Context context;
    private List<TicketType> ticketList = new ArrayList<>();
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public TicketDetailAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_list, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketType ticket = ticketList.get(position);

        holder.tvTicketCode.setText(ticket.getCode()); // Giả sử 'Vé VIP' là getTypeName()

        String formattedPrice = currencyFormatter.format(ticket.getPrice());
        holder.tvTicketPrice.setText(formattedPrice);

        if (ticket.getQuantity() > 0) {
            holder.chipTicketStatus.setText("Còn vé");
            holder.chipTicketStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#C8E6C9")));
            holder.chipTicketStatus.setTextColor(Color.parseColor("#1B5E20"));
        } else {
            holder.chipTicketStatus.setText("Hết vé");
            holder.chipTicketStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D9BEBE")));
            holder.chipTicketStatus.setTextColor(Color.parseColor("#E65100"));
        }
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public void updateData(List<TicketType> newTickets) {
        ticketList.clear();
        ticketList.addAll(newTickets);
        notifyDataSetChanged();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketCode, tvTicketPrice;
        Chip chipTicketStatus;
        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTicketCode = itemView.findViewById(R.id.tv_ticket_code);
            tvTicketPrice = itemView.findViewById(R.id.tv_ticket_price);
            chipTicketStatus = itemView.findViewById(R.id.chip_ticket_status);
        }
    }
}
