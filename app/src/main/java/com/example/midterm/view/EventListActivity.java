package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.example.midterm.view.Adapter.EventAdapter;
import com.example.midterm.view.fragment.FilterBottomSheet;
import com.example.midterm.viewModel.EventViewModel;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity implements FilterBottomSheet.FilterListener{
    public static final String EXTRA_EVENT_TYPE = "EVENT_TYPE_KEY";
    public static final String TYPE_ACTIVE = "active";
    public static final String TYPE_PAST = "past";

    private FilterBottomSheet.SortType currentSortType;

    private Toolbar toolbar;
    private TextView tvListTitle, tvEmptyState;
    private EditText etSearch;
    private ImageButton btnFilter;
    private RecyclerView rvEventList;

    // Logic
    private EventViewModel eventViewModel;
    private EventAdapter eventAdapter;
    private int organizerId = -1;
    private String eventType;
    private List<Event> allEvents = new ArrayList<>(); // Danh sách gốc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_list);

        organizerId = getIntent().getIntExtra("user_id", -1);
        eventType = getIntent().getStringExtra(EXTRA_EVENT_TYPE);

        if (organizerId == -1 || eventType == null) {
            finish();
            return;
        }
        initViews();
        setupToolbar();
        initViewModelAndAdapter();
        setupSearchListener();
        loadDataBasedOnType(); // Tải dữ liệu

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvListTitle = findViewById(R.id.tv_list_title);
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);
        rvEventList = findViewById(R.id.rv_event_list);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViewModelAndAdapter() {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        eventAdapter = new EventAdapter();
        rvEventList.setLayoutManager(new LinearLayoutManager(this));
        rvEventList.setAdapter(eventAdapter);

        eventAdapter.setOnItemClickListener(event -> {
             Intent intent = new Intent(this, ViewEventDetailsOrganizer.class);
             intent.putExtra("event_id", event.getId());
             startActivity(intent);
        });
    }

    /** Dựa vào "cờ" nhận được, gọi hàm ViewModel tương ứng*/
    private void loadDataBasedOnType() {
        LiveData<List<Event>> eventsLiveData;

        if (eventType.equals(TYPE_ACTIVE)) {
            tvListTitle.setText("Sự kiện đang hoạt động");
            eventsLiveData = eventViewModel.getActiveEvents(organizerId);
            currentSortType = FilterBottomSheet.SortType.DATE_ASC; // Sắp diễn ra lên đầu
        } else { // TYPE_PAST
            tvListTitle.setText("Sự kiện đã kết thúc");
            eventsLiveData = eventViewModel.getPastEvents(organizerId);
            currentSortType = FilterBottomSheet.SortType.DATE_DESC; // Vừa kết thúc lên đầu
        }

        // Lắng nghe dữ liệu
        eventsLiveData.observe(this, events -> {
            if (events == null || events.isEmpty()) {
                tvEmptyState.setText("Không có sự kiện nào.");
                tvEmptyState.setVisibility(View.VISIBLE);
                rvEventList.setVisibility(View.GONE);
                allEvents.clear();
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvEventList.setVisibility(View.VISIBLE);
                allEvents = events; // Lưu danh sách gốc

                applyFilters();
            }
        });
    }

    //Cài đặt trình lắng nghe cho thanh tìm kiếm
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {applyFilters();}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Xử lý nút Lọc (Filter)
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheet bottomSheet = new FilterBottomSheet();
            // TODO: Gửi trạng thái sắp xếp hiện tại sang BottomSheet (nếu cần)
            bottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }

    /*** Hàm này được gọi TỪ BottomSheet khi nhấn "Áp dụng"*/
    @Override
    public void onFilterApplied(FilterBottomSheet.SortType sortType) {
        this.currentSortType = sortType;
        applyFilters(); // Áp dụng cả tìm kiếm và sắp xếp
    }

    /*** HÀM HỢP NHẤT: Áp dụng cả Lọc (search) và Sắp xếp (sort)*/
    private void applyFilters() {
        if (allEvents == null) return;

        String query = etSearch.getText().toString();
        List<Event> filteredList = new ArrayList<>();

        // Lọc (Filter) theo từ khóa tìm kiếm
        if (query.isEmpty()) {
            filteredList.addAll(allEvents);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Event event : allEvents) {
                if (event.getEventName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(event);
                }
            }
        }

        // Sắp xếp (Sort) theo tùy chọn
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if (currentSortType == FilterBottomSheet.SortType.NAME_AZ) {
                filteredList.sort((e1, e2) -> e1.getEventName().compareToIgnoreCase(e2.getEventName()));
            } else if (currentSortType == FilterBottomSheet.SortType.DATE_DESC) {
                // Sắp xếp giảm dần (DESC)
                filteredList.sort((e1, e2) -> e2.getEndDate().compareToIgnoreCase(e1.getEndDate()));
            } else { // DATE_ASC (Mặc định)
                // Sắp xếp tăng dần (ASC)
                filteredList.sort((e1, e2) -> e1.getEndDate().compareToIgnoreCase(e2.getEndDate()));
            }
        }
        eventAdapter.setEvents(filteredList);
        // Xử lý trạng thái rỗng
        if (filteredList.isEmpty()) {
            if (!query.isEmpty()) {
                tvEmptyState.setText("Không tìm thấy sự kiện nào khớp.");
            } else {
                tvEmptyState.setText("Không có sự kiện nào.");
            }
            tvEmptyState.setVisibility(View.VISIBLE);
            rvEventList.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvEventList.setVisibility(View.VISIBLE);
        }
    }
}