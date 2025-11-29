package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;

import java.util.List;
import java.util.Locale;

public class SalesReportAdapter extends RecyclerView.Adapter<SalesReportAdapter.ViewHolder> {

    private Context context;
    private List<TicketType> ticketTypes;

    public SalesReportAdapter(Context context, List<TicketType> ticketTypes) {
        this.context = context;
        this.ticketTypes = ticketTypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sales_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketType ticketType = ticketTypes.get(position);

        // Ticket type name
        holder.tvTicketType.setText(ticketType.getCode());

        // Price
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", ticketType.getPrice()));

        // Sold / Total
        holder.tvSoldCount.setText(String.format(Locale.getDefault(), "%d / %d",
                ticketType.getSoldQuantity(), ticketType.getQuantity()));

        // Revenue
        double revenue = ticketType.getPrice() * ticketType.getSoldQuantity();
        holder.tvRevenue.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", revenue));

        // Progress bar
        int progress = 0;
        if (ticketType.getQuantity() > 0) {
            progress = (ticketType.getSoldQuantity() * 100) / ticketType.getQuantity();
        }
        holder.progressBar.setProgress(progress);

        // Sell rate
        holder.tvSellRate.setText(String.format(Locale.getDefault(), "%d%%", progress));

        // Set progress bar color based on sell rate
        if (progress >= 80) {
            holder.progressBar.setProgressTintList(context.getColorStateList(R.color.colorSuccess));
        } else if (progress >= 50) {
            holder.progressBar.setProgressTintList(context.getColorStateList(R.color.colorWarning));
        } else {
            holder.progressBar.setProgressTintList(context.getColorStateList(R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return ticketTypes != null ? ticketTypes.size() : 0;
    }

    public void updateData(List<TicketType> newData) {
        this.ticketTypes = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketType, tvPrice, tvSoldCount, tvRevenue, tvSellRate;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketType = itemView.findViewById(R.id.tv_ticket_type);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvSoldCount = itemView.findViewById(R.id.tv_sold_count);
            tvRevenue = itemView.findViewById(R.id.tv_revenue);
            tvSellRate = itemView.findViewById(R.id.tv_sell_rate);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
