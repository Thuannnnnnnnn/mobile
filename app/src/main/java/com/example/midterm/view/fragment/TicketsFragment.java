package com.example.midterm.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.Ticket;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.viewModel.TicketViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Cần import cái này
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TicketsFragment extends Fragment {
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvTickets;
    private TextView tvEmptyState;

    private TicketViewModel ticketViewModel;
    private MyTicketAdapter ticketAdapter;
    private List<TicketWithDetails> ticketList = new ArrayList<>();

    private int userId = -1;

    public static TicketsFragment newInstance(int userId) {
        TicketsFragment fragment = new TicketsFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);

        if (getArguments() != null) {
            userId = getArguments().getInt("user_id", -1);
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.appBarLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        initViews(view);
        initViewModel();
        setupTabLayout();
        loadTickets(0);

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tabLayout = view.findViewById(R.id.tab_layout);
        rvTickets = view.findViewById(R.id.rv_tickets);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        rvTickets.setLayoutManager(new LinearLayoutManager(requireContext()));

        ticketAdapter = new MyTicketAdapter(ticketList, this::showQRDialog);
        rvTickets.setAdapter(ticketAdapter);

        if (getArguments() != null && getArguments().getBoolean("show_back")) {
            toolbar.setNavigationIcon(R.drawable.arrow__1_); // Set icon mũi tên
            toolbar.setNavigationOnClickListener(v -> {
                // Quay về tab Home khi nhấn nút back
                if (getActivity() != null) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_home);
                    }
                }
            });
        } else {
            toolbar.setNavigationIcon(null);
        }
    }

    private void initViewModel() {
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Sắp diễn ra"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã qua"));
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {loadTickets(tab.getPosition());}
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadTickets(int tabPosition) {
        if (userId == -1) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Vui lòng đăng nhập để xem vé");
            rvTickets.setVisibility(View.GONE);
            return;
        }
        if (ticketViewModel == null) {
            ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);
        }

        switch (tabPosition) {
            case 0:
                ticketViewModel.getUpcomingTicketsByUser(userId).observe(getViewLifecycleOwner(), tickets -> {
                    updateUI(tickets, "Không có vé sắp diễn ra");
                });
                break;
            case 1:
                ticketViewModel.getPastTicketsByUser(userId).observe(getViewLifecycleOwner(), tickets -> {
                    updateUI(tickets, "Không có vé đã qua");
                });
                break;
            case 2:
                ticketViewModel.getTicketsByUser(userId).observe(getViewLifecycleOwner(), tickets -> {
                    updateUI(tickets, "Chưa có vé nào");
                });
                break;
        }
    }

    private void updateUI(List<Ticket> tickets, String emptyMessage) {
        if (tickets != null && !tickets.isEmpty()) {
            tvEmptyState.setVisibility(View.GONE);
            rvTickets.setVisibility(View.VISIBLE);
            loadTicketDetails(tickets);
        } else {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(emptyMessage);
            rvTickets.setVisibility(View.GONE);
            ticketList.clear();
            ticketAdapter.notifyDataSetChanged();
        }
    }

    private void loadTicketDetails(List<Ticket> tickets) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (!isAdded()) return; // Kiểm tra an toàn

            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<TicketWithDetails> details = new ArrayList<>();

            for (Ticket ticket : tickets) {
                TicketType ticketType = db.ticketTypeDAO().getTicketTypeByIdSync(ticket.getTicketTypeID());
                if (ticketType != null) {
                    com.example.midterm.model.entity.Event event = db.eventDAO().getEventByIdSync(ticketType.getEventID());
                    if (event != null) {
                        TicketWithDetails detail = new TicketWithDetails();
                        detail.ticket = ticket;
                        detail.ticketType = ticketType;
                        detail.eventName = event.getEventName();
                        detail.eventDate = event.getStartDate();
                        detail.eventLocation = event.getLocation();
                        details.add(detail);
                    }
                }
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    ticketList.clear();
                    ticketList.addAll(details);
                    ticketAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void showQRDialog(TicketWithDetails ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qr_code, null);

        ImageView ivQR = dialogView.findViewById(R.id.iv_qr_code);
        TextView tvTicketCode = dialogView.findViewById(R.id.tv_ticket_code);

        Bitmap qrBitmap = generateQRCode(ticket.ticket.getQrCode());
        if (qrBitmap != null) {
            ivQR.setImageBitmap(qrBitmap);
        }
        tvTicketCode.setText(ticket.ticket.getQrCode().substring(0, 8).toUpperCase());

        builder.setView(dialogView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    public static class TicketWithDetails {
        public Ticket ticket;
        public TicketType ticketType;
        public String eventName;
        public String eventDate;
        public String eventLocation;
    }

    public static class MyTicketAdapter extends RecyclerView.Adapter<MyTicketAdapter.ViewHolder> {
        private final List<TicketWithDetails> tickets;
        private final OnTicketClickListener listener;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public interface OnTicketClickListener {
            void onTicketClick(TicketWithDetails ticket);
        }

        public MyTicketAdapter(List<TicketWithDetails> tickets, OnTicketClickListener listener) {
            this.tickets = tickets;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TicketWithDetails ticket = tickets.get(position);
            holder.tvEventName.setText(ticket.eventName);
            holder.tvTicketType.setText(ticket.ticketType.getCode());
            holder.tvPrice.setText(currencyFormatter.format(ticket.ticketType.getPrice()));
            holder.tvLocation.setText(ticket.eventLocation);

            try {
                Date date = dbFormat.parse(ticket.eventDate);
                holder.tvDate.setText(displayFormat.format(date));
            } catch (ParseException e) {
                holder.tvDate.setText(ticket.eventDate);
            }

            // Set status chip logic...
            String status = ticket.ticket.getStatus();
            if ("checked_in".equals(status)) {
                holder.chipStatus.setText("Đã check-in");
                holder.chipStatus.setChipBackgroundColorResource(R.color.green_light);
            } else if ("cancelled".equals(status)) {
                holder.chipStatus.setText("Đã hủy");
                holder.chipStatus.setChipBackgroundColorResource(R.color.red_light);
            } else {
                holder.chipStatus.setText("Chưa sử dụng");
                holder.chipStatus.setChipBackgroundColorResource(R.color.blue_light);
            }

            holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket));
        }

        @Override
        public int getItemCount() { return tickets.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEventName, tvTicketType, tvPrice, tvDate, tvLocation;
            Chip chipStatus;
            ViewHolder(View itemView) {
                super(itemView);
                tvEventName = itemView.findViewById(R.id.tv_event_name);
                tvTicketType = itemView.findViewById(R.id.tv_ticket_type);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvLocation = itemView.findViewById(R.id.tv_location);
                chipStatus = itemView.findViewById(R.id.chip_status);
            }
        }
    }
}