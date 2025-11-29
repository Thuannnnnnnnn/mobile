package com.example.midterm.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.view.Adapter.EventAdapter;
import com.example.midterm.view.CreateEvent;
import com.example.midterm.view.EventDashboardActivity;
import com.example.midterm.view.EventListActivity;
import com.example.midterm.view.ManageVouchers;
import com.example.midterm.view.QRScannerActivity;
import com.example.midterm.view.ViewEventDetailsOrganizer;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.button.MaterialButton;

public class OrganizerDashboardFragment extends Fragment {
    private MaterialButton btnCreateEvent, btnCheckIn, btnManageVouchers, btnManageOrders;
    private TextView tvViewAllEvents, tvViewAllPastEvents;
    private RecyclerView rvActiveEvents, rvPastEvents;

    private TextView tvSoldTicketsValue, tvRevenueValue, tvEventsValue;

    private EventViewModel eventViewModel;
    private EventAdapter activeEventsAdapter;
    private EventAdapter pastEventsAdapter;
    private int currentOrganizerId = -1;

    public static OrganizerDashboardFragment newInstance(int organizerId) {
        OrganizerDashboardFragment fragment = new OrganizerDashboardFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);

        if (getArguments() != null) {
            currentOrganizerId = getArguments().getInt("user_id", -1);
        }

        initViews(view);
        initViewModelAndAdapters();
        setupClickListeners();
        observeEvents();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        btnCreateEvent = view.findViewById(R.id.btn_create_event);
        btnCheckIn = view.findViewById(R.id.btn_check_in);
        btnManageVouchers = view.findViewById(R.id.btn_manage_vouchers);
        btnManageOrders = view.findViewById(R.id.btn_history);
        rvActiveEvents = view.findViewById(R.id.rv_active_events);
        rvPastEvents = view.findViewById(R.id.rv_past_events);
        tvViewAllEvents = view.findViewById(R.id.tv_view_all_events);
        tvViewAllPastEvents = view.findViewById(R.id.tv_view_all_past_events);

        tvSoldTicketsValue = view.findViewById(R.id.tv_sold_tickets_value);
        tvRevenueValue = view.findViewById(R.id.tv_revenue_value);
        tvEventsValue = view.findViewById(R.id.tv_events_value);
    }

    private void initViewModelAndAdapters() {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        activeEventsAdapter = new EventAdapter();
        pastEventsAdapter = new EventAdapter();
        rvActiveEvents.setAdapter(activeEventsAdapter);
        rvPastEvents.setAdapter(pastEventsAdapter);
    }

    private void setupClickListeners() {
        // Tạo event
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateEvent.class);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        // Manage voucher
        btnManageVouchers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ManageVouchers.class);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        // Manage orders - view all sold tickets
        btnManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventDashboardActivity.class);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        // Check-in - scan QR code
        btnCheckIn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), QRScannerActivity.class);
            startActivity(intent);
        });

        // Xem tất cả sự kiện đang hoạt động
        tvViewAllEvents.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventListActivity.class);
            intent.putExtra(EventListActivity.EXTRA_EVENT_TYPE, EventListActivity.TYPE_ACTIVE);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        // Click vào 1 event đang hoạt động
        activeEventsAdapter.setOnItemClickListener(event -> {
            Intent intent = new Intent(requireContext(), ViewEventDetailsOrganizer.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });

        // Xem tất cả sự kiện đã diễn ra
        tvViewAllPastEvents.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventListActivity.class);
            intent.putExtra(EventListActivity.EXTRA_EVENT_TYPE, EventListActivity.TYPE_PAST);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        // Click vào 1 event đã diễn ra
        pastEventsAdapter.setOnItemClickListener(event -> {
            Intent intent = new Intent(requireContext(), ViewEventDetailsOrganizer.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
    }

    private void observeEvents() {
        // Lấy sự kiện còn hoạt động
        eventViewModel.getActiveEvents(currentOrganizerId).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                activeEventsAdapter.setEvents(events);
            }
        });
        // Lấy sự kiện đã diễn ra
        eventViewModel.getPastEvents(currentOrganizerId).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                pastEventsAdapter.setEvents(events);
            }
        });
    }

    private void loadStatistics() {
        // Thống kê vé đã bán
        eventViewModel.getTotalTicketsSoldByOrganizer(currentOrganizerId).observe(getViewLifecycleOwner(), totalTickets -> {
            if (totalTickets != null) {
                tvSoldTicketsValue.setText(String.valueOf(totalTickets));
            } else {
                tvSoldTicketsValue.setText("0");
            }
        });

        // Thống kê doanh thu
        eventViewModel.getTotalRevenueByOrganizer(currentOrganizerId).observe(getViewLifecycleOwner(), revenue -> {
            if (revenue != null && revenue > 0) {
                tvRevenueValue.setText(formatCurrency(revenue));
            } else {
                tvRevenueValue.setText("0đ");
            }
        });

        // Thống kê số event còn hoạt động
        eventViewModel.getActiveEventCount(currentOrganizerId).observe(getViewLifecycleOwner(), eventCount -> {
            if (eventCount != null) {
                tvEventsValue.setText(String.valueOf(eventCount));
            } else {
                tvEventsValue.setText("0");
            }
        });
    }

    private String formatCurrency(double amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        if (amount >= 1_000_000_000) {
            return formatter.format(amount / 1_000_000_000) + "B";
        } else if (amount >= 1_000_000) {
            return formatter.format(amount / 1_000_000) + "M";
        } else if (amount >= 1_000) {
            return formatter.format(amount / 1_000) + "K";
        }
        return formatter.format(amount) + "đ";
    }
}
