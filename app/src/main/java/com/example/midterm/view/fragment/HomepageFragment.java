package com.example.midterm.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.view.Adapter.BannerAdapter;
import com.example.midterm.view.Adapter.CategoryAdapter;
import com.example.midterm.view.Adapter.EventCardAdapter;
import com.example.midterm.model.entity.Event;
import com.example.midterm.view.EventDetail;

import com.example.midterm.view.NotificationsActivity;
import com.example.midterm.view.Search;
import com.example.midterm.viewModel.EventViewModel;

import java.util.ArrayList;
import java.util.List;


public class HomepageFragment extends Fragment implements BannerAdapter.OnItemClickListener, CategoryAdapter.OnItemClickListener, EventCardAdapter.OnItemClickListener {

    private EventViewModel eventViewModel;
    private RecyclerView rvMainBanner, rvCategories, rvFeaturedEvents;
    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private EventCardAdapter eventCardAdapter;

    private ImageView btnNotification;

    private LinearLayout linearSearch;

    private List<Event> bannerEventList = new ArrayList<>();
    private List<String> genreList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homepage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        setupViews(view);
        setupRecyclerViews();
        observeData();

        linearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Search.class);
                startActivity(intent);
            }
        });
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupViews(View view) {
        rvMainBanner = view.findViewById(R.id.rv_main_banner);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvFeaturedEvents = view.findViewById(R.id.rv_featured_events);
        linearSearch = view.findViewById(R.id.linear_search);
        btnNotification = view.findViewById(R.id.btn_notification);
    }

    private void setupRecyclerViews() {
        bannerAdapter = new BannerAdapter(getContext(), bannerEventList);
        bannerAdapter.setOnItemClickListener(this);
        rvMainBanner.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMainBanner.setAdapter(bannerAdapter);

        categoryAdapter = new CategoryAdapter(getContext(), genreList);
        categoryAdapter.setOnItemClickListener(this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        eventCardAdapter = new EventCardAdapter(getContext(), eventList);
        eventCardAdapter.setOnItemClickListener(this);
        rvFeaturedEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeaturedEvents.setAdapter(eventCardAdapter);
    }

    private void observeData() {

        eventViewModel.getAllGenres().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                categoryAdapter.updateData(genres);
            }
        });
        eventViewModel.getUpcomingEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                eventCardAdapter.updateData(events);
                bannerAdapter.updateData(events);
            }
        });
    }

    @Override
    public void onItemClick(Event event) {
        Intent intent = new Intent(getActivity(), EventDetail.class);
        intent.putExtra("EVENT_ID", event.getId());
        startActivity(intent);
    }

    @Override
    public void onItemClick(String genre, boolean isSelected) {
        if (isSelected) {
            eventViewModel.getEventsByGenreForUser(genre).observe(getViewLifecycleOwner(), events -> {
                if (events != null && !events.isEmpty()) {
                    eventCardAdapter.updateData(events);
                    Toast.makeText(getContext(), "Hiển thị " + events.size() + " sự kiện thể loại: " + genre, Toast.LENGTH_SHORT).show();
                } else {
                    eventCardAdapter.updateData(new ArrayList<>());
                    Toast.makeText(getContext(), "Không có sự kiện nào cho thể loại: " + genre, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            eventViewModel.getUpcomingEvents().observe(getViewLifecycleOwner(), events -> {
                if (events != null) {
                    eventCardAdapter.updateData(events);
                    Toast.makeText(getContext(), "Hiển thị tất cả sự kiện sắp tới", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}