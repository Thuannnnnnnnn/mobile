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
import com.example.midterm.view.CreateEvent;
import com.example.midterm.view.ViewEventDetailsOrganizer;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class OrganizerEventsFragment extends Fragment {
    private TabLayout tabLayout;
    private RecyclerView rvEvents;
    private FloatingActionButton fabCreateEvent;
    private TextView tvEmptyState;

    private EventViewModel eventViewModel;
    private EventAdapter eventAdapter;
    private int currentOrganizerId = -1;

    public static OrganizerEventsFragment newInstance(int organizerId) {
        OrganizerEventsFragment fragment = new OrganizerEventsFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_events, container, false);

        if (getArguments() != null) {
            currentOrganizerId = getArguments().getInt("user_id", -1);
        }

        initViews(view);
        initViewModel();
        setupClickListeners();
        setupTabLayout();
        loadEvents(0);

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        rvEvents = view.findViewById(R.id.rv_events);
        fabCreateEvent = view.findViewById(R.id.fab_create_event);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventAdapter = new EventAdapter();
        rvEvents.setAdapter(eventAdapter);
    }

    private void initViewModel() {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
    }

    private void setupClickListeners() {
        fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateEvent.class);
            intent.putExtra("user_id", currentOrganizerId);
            startActivity(intent);
        });

        eventAdapter.setOnItemClickListener(event -> {
            Intent intent = new Intent(requireContext(), ViewEventDetailsOrganizer.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Đang diễn ra"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã kết thúc"));
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadEvents(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void loadEvents(int tabPosition) {
        switch (tabPosition) {
            case 0:
                eventViewModel.getActiveEvents(currentOrganizerId).observe(getViewLifecycleOwner(), events -> {
                    if (events != null && !events.isEmpty()) {
                        eventAdapter.setEvents(events);
                        tvEmptyState.setVisibility(View.GONE);
                        rvEvents.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Không có sự kiện đang diễn ra");
                        rvEvents.setVisibility(View.GONE);
                    }
                });
                break;
            case 1:
                eventViewModel.getPastEvents(currentOrganizerId).observe(getViewLifecycleOwner(), events -> {
                    if (events != null && !events.isEmpty()) {
                        eventAdapter.setEvents(events);
                        tvEmptyState.setVisibility(View.GONE);
                        rvEvents.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Không có sự kiện đã kết thúc");
                        rvEvents.setVisibility(View.GONE);
                    }
                });
                break;
            case 2:
                eventViewModel.getEventsByOrganizerId(currentOrganizerId).observe(getViewLifecycleOwner(), events -> {
                    if (events != null && !events.isEmpty()) {
                        eventAdapter.setEvents(events);
                        tvEmptyState.setVisibility(View.GONE);
                        rvEvents.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Chưa có sự kiện nào");
                        rvEvents.setVisibility(View.GONE);
                    }
                });
                break;
        }
    }
}
