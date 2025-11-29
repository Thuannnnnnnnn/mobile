package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.entity.TicketType;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SeatSelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvSeats;
    private TextView tvSelectedCount, tvTotalPrice, tvTicketTypeName;
    private MaterialButton btnConfirm;

    private SeatAdapter seatAdapter;
    private List<Seat> seatList = new ArrayList<>();
    private List<Long> selectedSeatIds = new ArrayList<>();

    private int eventId, ticketTypeId, maxSeats, userId, discountId = -1;
    private double pricePerSeat, subtotal, discountAmount;
    private String ticketTypeName, eventName;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        getIntentData();
        initViews();
        setupToolbar();
        loadSeats();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        eventId = intent.getIntExtra("EVENT_ID", -1);
        ticketTypeId = intent.getIntExtra("TICKET_TYPE_ID", -1);
        maxSeats = intent.getIntExtra("MAX_SEATS", 10);
        pricePerSeat = intent.getDoubleExtra("PRICE", 0);
        ticketTypeName = intent.getStringExtra("TICKET_TYPE_NAME");
        eventName = intent.getStringExtra("EVENT_NAME");
        subtotal = intent.getDoubleExtra("SUBTOTAL", 0);
        discountAmount = intent.getDoubleExtra("DISCOUNT_AMOUNT", 0);
        userId = intent.getIntExtra("USER_ID", -1);
        discountId = intent.getIntExtra("DISCOUNT_ID", -1);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSeats = findViewById(R.id.rv_seats);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvTicketTypeName = findViewById(R.id.tv_ticket_type_name);
        btnConfirm = findViewById(R.id.btn_confirm);

        tvTicketTypeName.setText(ticketTypeName != null ? ticketTypeName : "Chọn ghế");
        updateSelection();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chọn vị trí ngồi");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSeats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            List<Seat> seats = db.seatDAO().getSeatsBySectionIdSync(ticketTypeId);

            if (seats == null || seats.isEmpty()) {
                var sections = db.eventSectionDAO().getEventSectionsByEventIdSync(eventId);
                seats = new ArrayList<>();
                if (sections != null) {
                    for (var section : sections) {
                        List<Seat> sectionSeats = db.seatDAO().getSeatsBySectionIdSync(section.getSectionId());
                        if (sectionSeats != null) {
                            for (Seat seat : sectionSeats) {
                                if (ticketTypeId == -1 || (seat.getTicketTypeID() != null && seat.getTicketTypeID() == ticketTypeId)) {
                                    seats.add(seat);
                                }
                            }
                        }
                    }
                }
            }

            final List<Seat> finalSeats = seats;
            runOnUiThread(() -> {
                seatList.clear();
                if (finalSeats != null) {
                    seatList.addAll(finalSeats);
                }
                setupRecyclerView();
            });
        });
    }

    private void setupRecyclerView() {
        int columns = 10;
        if (!seatList.isEmpty()) {
            int maxNum = 0;
            for (Seat seat : seatList) {
                try {
                    int num = Integer.parseInt(seat.getSeatNumber());
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
            if (maxNum > 0) columns = Math.min(maxNum, 12);
        }

        seatAdapter = new SeatAdapter(seatList, seat -> {
            if ("booked".equals(seat.getStatus())) {
                Toast.makeText(this, "Ghế này đã được đặt", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSeatIds.contains(seat.getId())) {
                selectedSeatIds.remove(seat.getId());
            } else {
                if (selectedSeatIds.size() >= maxSeats) {
                    Toast.makeText(this, "Bạn chỉ có thể chọn tối đa " + maxSeats + " ghế", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedSeatIds.add(seat.getId());
            }
            seatAdapter.notifyDataSetChanged();
            updateSelection();
        }, selectedSeatIds);

        rvSeats.setLayoutManager(new GridLayoutManager(this, columns));
        rvSeats.setAdapter(seatAdapter);
    }

    private void updateSelection() {
        int count = selectedSeatIds.size();
        tvSelectedCount.setText("Đã chọn: " + count + " ghế");
        tvTotalPrice.setText(currencyFormatter.format(count * pricePerSeat));
        btnConfirm.setEnabled(count > 0);
        btnConfirm.setAlpha(count > 0 ? 1.0f : 0.5f);
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> {
            if (selectedSeatIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            intent.putExtra("EVENT_NAME", eventName);
            intent.putExtra("SUBTOTAL", subtotal);
            intent.putExtra("DISCOUNT", discountAmount);
            intent.putExtra("TOTAL", subtotal - discountAmount);
            intent.putExtra("USER_ID", userId);

            if (discountId > 0) {
                intent.putExtra("DISCOUNT_ID", discountId);
            }

            ArrayList<Integer> ticketTypeIds = new ArrayList<>();
            ArrayList<Integer> quantities = new ArrayList<>();
            double[] prices = new double[1];
            ArrayList<String> ticketNames = new ArrayList<>();
            ArrayList<Long> seatIds = new ArrayList<>(selectedSeatIds);

            ticketTypeIds.add(ticketTypeId);
            quantities.add(selectedSeatIds.size());
            prices[0] = pricePerSeat;
            ticketNames.add(ticketTypeName);

            intent.putIntegerArrayListExtra("TICKET_TYPE_IDS", ticketTypeIds);
            intent.putIntegerArrayListExtra("QUANTITIES", quantities);
            intent.putExtra("PRICES", prices);
            intent.putStringArrayListExtra("TICKET_NAMES", ticketNames);
            intent.putExtra("SEAT_IDS", seatIds);

            startActivity(intent);
            finish();
        });
    }

    public static class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.ViewHolder> {
        private final List<Seat> seats;
        private final OnSeatClickListener listener;
        private final List<Long> selectedIds;

        public interface OnSeatClickListener {
            void onSeatClick(Seat seat);
        }

        public SeatAdapter(List<Seat> seats, OnSeatClickListener listener, List<Long> selectedIds) {
            this.seats = seats;
            this.listener = listener;
            this.selectedIds = selectedIds;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_seat_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Seat seat = seats.get(position);
            holder.tvSeatNumber.setText(seat.getSeatRow() + seat.getSeatNumber());

            // Set background based on status
            if ("booked".equals(seat.getStatus())) {
                holder.itemView.setBackgroundResource(R.drawable.bg_seat_booked);
                holder.tvSeatNumber.setTextColor(0xFFFFFFFF);
            } else if (selectedIds.contains(seat.getId())) {
                holder.itemView.setBackgroundResource(R.drawable.bg_seat_selected);
                holder.tvSeatNumber.setTextColor(0xFFFFFFFF);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.bg_seat_available);
                holder.tvSeatNumber.setTextColor(0xFF424242);
            }

            holder.itemView.setOnClickListener(v -> listener.onSeatClick(seat));
        }

        @Override
        public int getItemCount() {
            return seats.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSeatNumber;

            ViewHolder(View itemView) {
                super(itemView);
                tvSeatNumber = itemView.findViewById(R.id.tv_seat_number);
            }
        }
    }
}
