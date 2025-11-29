package com.example.midterm.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.example.midterm.view.Adapter.CategoryAdapter;
import com.example.midterm.view.Adapter.EventCardAdapter;
import com.example.midterm.view.Adapter.HistoryAdapter;
import com.example.midterm.view.fragment.FilterBottomSheet;
import com.example.midterm.viewModel.EventViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Search extends AppCompatActivity implements
        HistoryAdapter.OnItemClickListener,
        CategoryAdapter.OnItemClickListener,
        FilterBottomSheet.FilterListener {

    private MaterialToolbar toolbar;
    private MaterialButton btnFilterDate, btnFilterMore;
    private RecyclerView rvSearchHistory, rvDiscoverGenre, rvDiscoverCity, rvSuggestions;
    private TextInputEditText etSearch;
    private TextView tvNoResults;

    private HistoryAdapter historyAdapter;
    private CategoryAdapter genreAdapter, cityAdapter;
    private EventCardAdapter eventAdapter;
    private EventViewModel eventViewModel;

    private List<String> historyList = new ArrayList<>();
    private List<String> genreList = new ArrayList<>();
    private List<String> cityList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();

    private SharedPreferences prefs;
    private static final String PREF_SEARCH_HISTORY = "search_history";
    private static final int MAX_HISTORY = 10;

    private String selectedDate = null;
    private String selectedGenre = null;
    private String selectedCity = null;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        prefs = getSharedPreferences("EventApp", MODE_PRIVATE);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        setupToolbar();
        initViews();
        setupRecyclerViews();
        loadSearchHistory();
        observeData();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnFilterDate = findViewById(R.id.btn_filter_date);
        btnFilterMore = findViewById(R.id.btn_filter_more);
        rvSearchHistory = findViewById(R.id.rv_search_history);
        rvDiscoverGenre = findViewById(R.id.rv_discover_genre);
        rvDiscoverCity = findViewById(R.id.rv_discover_city);
        rvSuggestions = findViewById(R.id.rv_suggestions);
        etSearch = findViewById(R.id.et_search);

        // Add TextView for no results (we'll create this dynamically if not in layout)
        tvNoResults = new TextView(this);
        tvNoResults.setText("Không tìm thấy kết quả");
        tvNoResults.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, historyList);
        historyAdapter.setOnItemClickListener(this);
        rvSearchHistory.setAdapter(historyAdapter);

        rvDiscoverGenre.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        genreAdapter = new CategoryAdapter(this, genreList);
        genreAdapter.setOnItemClickListener(this);
        rvDiscoverGenre.setAdapter(genreAdapter);

        rvDiscoverCity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        cityAdapter = new CategoryAdapter(this, cityList);
        cityAdapter.setOnItemClickListener((item, isSelected) -> {
            selectedCity = isSelected ? item : null;
            performSearch(etSearch.getText().toString().trim());
            Toast.makeText(this, isSelected ? "Lọc theo thành phố: " + item : "Đã bỏ lọc thành phố", Toast.LENGTH_SHORT).show();
        });
        rvDiscoverCity.setAdapter(cityAdapter);


        rvSuggestions.setLayoutManager(new GridLayoutManager(this, 2));
        eventAdapter = new EventCardAdapter(this, eventList);
        eventAdapter.setOnItemClickListener(event -> {
            Intent intent = new Intent(this, EventDetail.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });
        rvSuggestions.setAdapter(eventAdapter);
    }

    private void observeData() {
        // Load genres
        eventViewModel.getAllGenres().observe(this, genres -> {
            if (genres != null) {
                genreList.clear();
                genreList.addAll(genres);
                genreAdapter.notifyDataSetChanged();
            }
        });

        // Load cities
        eventViewModel.getAllCities().observe(this, cities -> {
            if (cities != null) {
                cityList.clear();
                // Extract city names from full addresses
                for (String location : cities) {
                    String city = extractCity(location);
                    if (!cityList.contains(city)) {
                        cityList.add(city);
                    }
                }
                cityAdapter.notifyDataSetChanged();
            }
        });

        // Load hot/suggested events
        eventViewModel.getHotEvents().observe(this, events -> {
            if (events != null) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
            }
        });
    }

    private String extractCity(String location) {
        if (location == null) return "";
        // Try to extract city from address (usually last part after comma)
        String[] parts = location.split(",");
        if (parts.length > 0) {
            return parts[parts.length - 1].trim();
        }
        return location;
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    rvSearchHistory.setVisibility(View.GONE);
                } else {
                    rvSearchHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Search action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    saveSearchHistory(query);
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        btnFilterDate.setOnClickListener(v -> showDatePicker());
        btnFilterMore.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedDate = dateFormat.format(selected.getTime());
                    btnFilterDate.setText(displayFormat.format(selected.getTime()));
                    performSearch(etSearch.getText().toString().trim());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showFilterBottomSheet() {
        FilterBottomSheet bottomSheet = new FilterBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
    }

    @Override
    public void onFilterApplied(FilterBottomSheet.SortType sortType) {
        // Handle sort type from FilterBottomSheet
        performSearch(etSearch.getText().toString().trim());
    }

    private void performSearch(String query) {
        String searchQuery = query.trim().isEmpty() ? null : query.trim();
        if (searchQuery == null && selectedGenre == null && selectedCity == null && selectedDate == null) {
            // Show hot events when no filters
            eventViewModel.getHotEvents().observe(this, events -> {
                updateEventList(events);
            });
            return;
        }

        eventViewModel.searchEventsWithFilters(searchQuery, selectedGenre, selectedCity).observe(this, events -> {
            updateEventList(events);
        });
    }

    private void updateEventList(List<Event> events) {
        eventList.clear();
        if (events != null && !events.isEmpty()) {
            eventList.addAll(events);
            rvSuggestions.setVisibility(View.VISIBLE);
        } else {
            rvSuggestions.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Không tìm thấy sự kiện", Toast.LENGTH_SHORT).show();
        }
        eventAdapter.notifyDataSetChanged();
    }

    private void loadSearchHistory() {
        Set<String> history = prefs.getStringSet(PREF_SEARCH_HISTORY, new HashSet<>());
        historyList.clear();
        historyList.addAll(history);
        historyAdapter.notifyDataSetChanged();
    }

    private void saveSearchHistory(String query) {
        Set<String> history = new HashSet<>(prefs.getStringSet(PREF_SEARCH_HISTORY, new HashSet<>()));
        history.add(query);

        if (history.size() > MAX_HISTORY) {
            List<String> list = new ArrayList<>(history);
            list.remove(0);
            history = new HashSet<>(list);
        }

        prefs.edit().putStringSet(PREF_SEARCH_HISTORY, history).apply();
        loadSearchHistory();
    }

    @Override
    public void onRemoveClick(String query, int position) {
        Set<String> history = new HashSet<>(prefs.getStringSet(PREF_SEARCH_HISTORY, new HashSet<>()));
        history.remove(query);
        prefs.edit().putStringSet(PREF_SEARCH_HISTORY, history).apply();
        historyAdapter.removeItem(position);
    }

    @Override
    public void onItemClick(String query) {
        etSearch.setText(query);
        saveSearchHistory(query);
        performSearch(query);
    }

    @Override
    public void onItemClick(String item, boolean isSelected) {
        selectedGenre = isSelected ? item : null;
        if (isSelected) {
            etSearch.setText("");
        }
        performSearch(etSearch.getText().toString().trim());
        Toast.makeText(this, isSelected ? "Lọc theo thể loại: " + item : "Đã bỏ lọc thể loại", Toast.LENGTH_SHORT).show();
    }
}
