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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.view.Adapter.EventAdapter;
import com.example.midterm.view.SalesReportActivity;
import com.example.midterm.viewModel.EventViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class OrganizerAnalyticsFragment extends Fragment {
    private TextView tvTotalRevenue, tvTotalTicketsSold, tvTotalEvents, tvActiveEvents, tvPastEvents;
    private RecyclerView rvPopularEvents;

    private EventViewModel eventViewModel;
    private EventAdapter popularEventsAdapter;
    private int currentOrganizerId = -1;

    public static OrganizerAnalyticsFragment newInstance(int organizerId) {
        OrganizerAnalyticsFragment fragment = new OrganizerAnalyticsFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_analytics, container, false);

        if (getArguments() != null) {
            currentOrganizerId = getArguments().getInt("user_id", -1);
        }

        initViews(view);
        initViewModel();
        observeData();

        return view;
    }

    private void initViews(View view) {
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvTotalTicketsSold = view.findViewById(R.id.tv_total_tickets_sold);
        tvTotalEvents = view.findViewById(R.id.tv_total_events);
        tvActiveEvents = view.findViewById(R.id.tv_active_events);
        tvPastEvents = view.findViewById(R.id.tv_past_events);
        rvPopularEvents = view.findViewById(R.id.rv_popular_events);

        rvPopularEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        popularEventsAdapter = new EventAdapter();
        rvPopularEvents.setAdapter(popularEventsAdapter);

        // Click 1 sư kiện xem report
        popularEventsAdapter.setOnItemClickListener(event -> {
            Intent intent = new Intent(requireContext(), SalesReportActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
    }

    private void initViewModel() {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
    }

    private void observeData() {
        eventViewModel.getTotalRevenueByOrganizer(currentOrganizerId).observe(getViewLifecycleOwner(), revenue -> {
            if (revenue != null) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvTotalRevenue.setText(formatter.format(revenue));
            } else {
                tvTotalRevenue.setText("0 ₫");
            }
        });

        eventViewModel.getTotalTicketsSoldByOrganizer(currentOrganizerId).observe(getViewLifecycleOwner(), tickets -> {
            tvTotalTicketsSold.setText(tickets != null ? String.valueOf(tickets) : "0");
        });

        eventViewModel.getTotalEventCount(currentOrganizerId).observe(getViewLifecycleOwner(), count -> {
            tvTotalEvents.setText(count != null ? String.valueOf(count) : "0");
        });

        eventViewModel.getActiveEventCount(currentOrganizerId).observe(getViewLifecycleOwner(), count -> {
            tvActiveEvents.setText(count != null ? String.valueOf(count) : "0");
        });

        eventViewModel.getPastEventCount(currentOrganizerId).observe(getViewLifecycleOwner(), count -> {
            tvPastEvents.setText(count != null ? String.valueOf(count) : "0");
        });

        eventViewModel.getEventsSortedByPopularity(currentOrganizerId).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                popularEventsAdapter.setEvents(events);
            }
        });
    }
}
