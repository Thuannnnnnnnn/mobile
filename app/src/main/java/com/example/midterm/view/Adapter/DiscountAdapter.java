package com.example.midterm.view.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Discount;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.ViewHolder>{

    private List<Discount> discountList;
    private final OnDiscountLongClickListener longClickListener;

    public interface OnDiscountLongClickListener {
        void onDiscountLongClick(Discount discount);
    }

    public DiscountAdapter(List<Discount> discountList, OnDiscountLongClickListener longClickListener) {
        this.discountList = discountList;
        this.longClickListener = longClickListener;
    }

    public void setDiscounts(List<Discount> discounts) {
        this.discountList = discounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discount discount = discountList.get(position);

        holder.tvCode.setText(discount.getCode());

        // Format giá trị giảm
        if ("percentage".equals(discount.getDiscountType())) {
            holder.tvValue.setText("Giảm " + (int)discount.getDiscountValue() + "%");
        } else {
            // Format tiền tệ VND
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            holder.tvValue.setText("Giảm " + currency.format(discount.getDiscountValue()));
        }

        // Usage limit
        String limit = discount.getUsageLimit() == 0 ? "∞" : String.valueOf(discount.getUsageLimit());
        holder.tvUsage.setText("Đã dùng: " + discount.getUsedCount() + "/" + limit);

        // Date
        holder.tvDate.setText("Hết hạn: " + discount.getEndDate());

        // Status
        if (discount.isActive()) {
            holder.tvStatus.setText("Hoạt động");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvStatus.setText("Đã đóng");
            holder.tvStatus.setTextColor(Color.RED);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onDiscountLongClick(discount);
            }
            return true; // Trả về true để báo là sự kiện đã được xử lý (không kích hoạt onClick thường nữa)
        });
    }

    @Override
    public int getItemCount() {
        return discountList == null ? 0 : discountList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvValue, tvUsage, tvStatus, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_item_code);
            tvValue = itemView.findViewById(R.id.tv_item_value);
            tvUsage = itemView.findViewById(R.id.tv_item_usage);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            tvDate = itemView.findViewById(R.id.tv_item_date);
        }
    }
}
