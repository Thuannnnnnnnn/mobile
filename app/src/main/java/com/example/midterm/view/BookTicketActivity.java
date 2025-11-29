package com.example.midterm.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class BookTicketActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivEventBanner;
    private TextView tvEventName, tvEventDate, tvEventLocation;
    private RecyclerView rvTicketTypes;
    private EditText etDiscountCode;
    private Button btnApplyDiscount, btnProceedPayment;
    private TextView tvSubtotal, tvDiscount, tvTotal;
    private LinearLayout llDiscountApplied;
    private TextView tvDiscountInfo;

    private EventViewModel eventViewModel;
    private TicketSelectionAdapter ticketAdapter;

    private int eventId;
    private Event currentEvent;
    private List<TicketType> ticketTypes = new ArrayList<>();
    private Map<Integer, Integer> selectedQuantities = new HashMap<>(); // ticketTypeId -> quantity
    private Discount appliedDiscount = null;

    private double subtotal = 0;
    private double discountAmount = 0;
    private double total = 0;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy • HH:mm", new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ticket);

        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        if (eventId == -1) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        loadEventData();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivEventBanner = findViewById(R.id.iv_event_banner);
        tvEventName = findViewById(R.id.tv_event_name);
        tvEventDate = findViewById(R.id.tv_event_date);
        tvEventLocation = findViewById(R.id.tv_event_location);
        rvTicketTypes = findViewById(R.id.rv_ticket_types);
        etDiscountCode = findViewById(R.id.et_discount_code);
        btnApplyDiscount = findViewById(R.id.btn_apply_discount);
        btnProceedPayment = findViewById(R.id.btn_proceed_payment);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvTotal = findViewById(R.id.tv_total);
        llDiscountApplied = findViewById(R.id.ll_discount_applied);
        tvDiscountInfo = findViewById(R.id.tv_discount_info);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đặt vé");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ticketAdapter = new TicketSelectionAdapter(this, ticketTypes, (ticketType, quantity) -> {
            selectedQuantities.put(ticketType.getId(), quantity);
            calculateTotal();
        });
        rvTicketTypes.setLayoutManager(new LinearLayoutManager(this));
        rvTicketTypes.setAdapter(ticketAdapter);
        rvTicketTypes.setNestedScrollingEnabled(false);
    }

    private void loadEventData() {
        eventViewModel.getEventWithTickets(eventId).observe(this, eventWithTickets -> {
            if (eventWithTickets == null) return;

            currentEvent = eventWithTickets.getEvent();
            ticketTypes.clear();
            ticketTypes.addAll(eventWithTickets.getTickets());

            populateEventInfo();
            ticketAdapter.notifyDataSetChanged();
        });
    }

    private void populateEventInfo() {
        tvEventName.setText(currentEvent.getEventName());
        tvEventLocation.setText(currentEvent.getLocation());
        tvEventDate.setText(formatDate(currentEvent.getStartDate()));

        Glide.with(this)
                .load(currentEvent.getBannerUrl())
                .centerCrop()
                .placeholder(R.drawable.loading)
                .into(ivEventBanner);
    }

    private void setupListeners() {
        btnApplyDiscount.setOnClickListener(v -> applyDiscount());
        btnProceedPayment.setOnClickListener(v -> proceedToPayment());
    }

    private void applyDiscount() {
        String code = etDiscountCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Discount discount = db.discountDAO().validateDiscount(code, eventId);

            runOnUiThread(() -> {
                if (discount != null) {
                    appliedDiscount = discount;
                    llDiscountApplied.setVisibility(View.VISIBLE);

                    String discountText;
                    if ("percentage".equals(discount.getDiscountType())) {
                        discountText = "Giảm " + (int) discount.getDiscountValue() + "%";
                    } else {
                        discountText = "Giảm " + currencyFormatter.format(discount.getDiscountValue());
                    }
                    tvDiscountInfo.setText(discountText + " - " + code);

                    calculateTotal();
                    Toast.makeText(this, "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void calculateTotal() {
        subtotal = 0;

        for (TicketType ticketType : ticketTypes) {
            int quantity = selectedQuantities.getOrDefault(ticketType.getId(), 0);
            subtotal += ticketType.getPrice() * quantity;
        }

        // Calculate discount
        if (appliedDiscount != null && subtotal >= appliedDiscount.getMinPurchase()) {
            discountAmount = appliedDiscount.calculateDiscount(subtotal);
        } else {
            discountAmount = 0;
        }

        total = subtotal - discountAmount;

        // Update UI
        tvSubtotal.setText(currencyFormatter.format(subtotal));
        tvDiscount.setText("-" + currencyFormatter.format(discountAmount));
        tvTotal.setText(currencyFormatter.format(total));

        // Enable/disable payment button
        boolean hasSelection = selectedQuantities.values().stream().anyMatch(q -> q > 0);
        btnProceedPayment.setEnabled(hasSelection);
        btnProceedPayment.setAlpha(hasSelection ? 1.0f : 0.5f);
    }

    private void proceedToPayment() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt vé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if any tickets selected
        boolean hasSelection = selectedQuantities.values().stream().anyMatch(q -> q > 0);
        if (!hasSelection) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 vé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Count how many ticket types selected
        int selectedTypes = 0;
        TicketType selectedType = null;
        int selectedQty = 0;

        for (TicketType ticketType : ticketTypes) {
            int quantity = selectedQuantities.getOrDefault(ticketType.getId(), 0);
            if (quantity > 0) {
                selectedTypes++;
                selectedType = ticketType;
                selectedQty = quantity;
            }
        }

        // Only allow one ticket type for seat selection
        if (selectedTypes > 1) {
            Toast.makeText(this, "Vui lòng chỉ chọn 1 loại vé để chọn chỗ ngồi", Toast.LENGTH_LONG).show();
            return;
        }

        // Go to seat selection
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("TICKET_TYPE_ID", selectedType.getId());
        intent.putExtra("MAX_SEATS", selectedQty);
        intent.putExtra("PRICE", selectedType.getPrice());
        intent.putExtra("TICKET_TYPE_NAME", selectedType.getCode());
        intent.putExtra("EVENT_NAME", currentEvent.getEventName());
        intent.putExtra("SUBTOTAL", subtotal);
        intent.putExtra("DISCOUNT_AMOUNT", discountAmount);
        intent.putExtra("USER_ID", userId);

        if (appliedDiscount != null) {
            intent.putExtra("DISCOUNT_ID", appliedDiscount.getId());
        }

        startActivity(intent);
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            Date date = dbDateFormat.parse(dateString);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    // Inner adapter class for ticket selection
    public static class TicketSelectionAdapter extends RecyclerView.Adapter<TicketSelectionAdapter.ViewHolder> {

        public interface OnQuantityChangeListener {
            void onQuantityChanged(TicketType ticketType, int quantity);
        }

        private final List<TicketType> ticketTypes;
        private final OnQuantityChangeListener listener;
        private final Map<Integer, Integer> quantities = new HashMap<>();
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private final android.content.Context context;

        public TicketSelectionAdapter(android.content.Context context, List<TicketType> ticketTypes, OnQuantityChangeListener listener) {
            this.context = context;
            this.ticketTypes = ticketTypes;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ticket_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TicketType ticketType = ticketTypes.get(position);
            int available = ticketType.getQuantity() - ticketType.getSoldQuantity();
            int currentQty = quantities.getOrDefault(ticketType.getId(), 0);

            holder.tvTicketName.setText(ticketType.getCode());
            holder.tvTicketPrice.setText(currencyFormatter.format(ticketType.getPrice()));
            holder.tvAvailable.setText("Còn " + available + " vé");
            holder.tvQuantity.setText(String.valueOf(currentQty));

            // Disable if sold out
            if (available <= 0) {
                holder.itemView.setAlpha(0.5f);
                holder.btnMinus.setEnabled(false);
                holder.btnPlus.setEnabled(false);
                holder.tvAvailable.setText("Hết vé");
            } else {
                holder.itemView.setAlpha(1.0f);
                holder.btnMinus.setEnabled(currentQty > 0);
                holder.btnPlus.setEnabled(currentQty < available && currentQty < 10);
            }

            holder.btnMinus.setOnClickListener(v -> {
                int qty = quantities.getOrDefault(ticketType.getId(), 0);
                if (qty > 0) {
                    qty--;
                    quantities.put(ticketType.getId(), qty);
                    holder.tvQuantity.setText(String.valueOf(qty));
                    holder.btnMinus.setEnabled(qty > 0);
                    holder.btnPlus.setEnabled(true);
                    listener.onQuantityChanged(ticketType, qty);
                }
            });

            holder.btnPlus.setOnClickListener(v -> {
                int qty = quantities.getOrDefault(ticketType.getId(), 0);
                if (qty < available && qty < 10) {
                    qty++;
                    quantities.put(ticketType.getId(), qty);
                    holder.tvQuantity.setText(String.valueOf(qty));
                    holder.btnMinus.setEnabled(true);
                    holder.btnPlus.setEnabled(qty < available && qty < 10);
                    listener.onQuantityChanged(ticketType, qty);
                }
            });
        }

        @Override
        public int getItemCount() {
            return ticketTypes.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTicketName, tvTicketPrice, tvAvailable, tvQuantity;
            ImageView btnMinus, btnPlus;

            ViewHolder(View itemView) {
                super(itemView);
                tvTicketName = itemView.findViewById(R.id.tv_ticket_name);
                tvTicketPrice = itemView.findViewById(R.id.tv_ticket_price);
                tvAvailable = itemView.findViewById(R.id.tv_available);
                tvQuantity = itemView.findViewById(R.id.tv_quantity);
                btnMinus = itemView.findViewById(R.id.btn_minus);
                btnPlus = itemView.findViewById(R.id.btn_plus);
            }
        }
    }
}
