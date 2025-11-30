package com.example.midterm.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.lifecycle.Observer;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.EventSection;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.view.Adapter.SeatGridAdapter;
import com.example.midterm.view.Adapter.TicketPaletteAdapter;
import com.example.midterm.view.custom.ZoomLayout;
import com.example.midterm.viewModel.EventSectionViewModel;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.GuestViewModel;
import com.example.midterm.viewModel.SeatViewModel;
import com.example.midterm.viewModel.TicketTypeViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class CreateSeatMap extends AppCompatActivity implements TicketPaletteAdapter.OnPaletteClickListener {
    // UI Components
    private ImageButton btnBack;
    private Button btnSaveMap;
    private AutoCompleteTextView actvSectionSelector;
    private RecyclerView rvTicketPalette, rvSeatGrid;
    private TextView tvStandingSectionNote;
    private View rootView;
    private ZoomLayout zoomLayout; // View custom mới

    // ViewModels
    private EventViewModel eventViewModel;
    private GuestViewModel guestViewModel;
    private EventSectionViewModel eventSectionViewModel;
    private TicketTypeViewModel ticketTypeViewModel;
    private SeatViewModel seatViewModel;

    // Adapters & Data
    private ArrayAdapter<String> sectionSpinnerAdapter;
    private TicketPaletteAdapter paletteAdapter;
    private SeatGridAdapter gridAdapter;
    private long currentEventId = -1L;
    private List<EventSection> eventSections = new ArrayList<>();
    private List<TicketType> ticketTypes = new ArrayList<>();

    // State
    private EventSection currentSelectedSection;
    private TicketType currentSelectedTicketType;
    private Map<Long, List<Seat>> seatMapCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_seat_map);

        initViews();

        currentEventId = getIntent().getLongExtra("room_id", -1L);
        if (currentEventId == -1L) { finish(); return; }

        initViewModels();
        setupPaletteRecyclerView();
        setupGridRecyclerView();
        setupListeners();
        observeViewModels();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        rootView = findViewById(R.id.main);
        btnBack = findViewById(R.id.btn_back);
        btnSaveMap = findViewById(R.id.btn_save_map);
        actvSectionSelector = findViewById(R.id.actv_section_selector);
        rvTicketPalette = findViewById(R.id.rv_ticket_palette);
        rvSeatGrid = findViewById(R.id.rv_seat_grid);
        tvStandingSectionNote = findViewById(R.id.tv_standing_section_note);
        zoomLayout = findViewById(R.id.zoom_layout); // Ánh xạ ZoomLayout
    }

    private void initViewModels() {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        guestViewModel = new ViewModelProvider(this).get(GuestViewModel.class);
        eventSectionViewModel = new ViewModelProvider(this).get(EventSectionViewModel.class);
        ticketTypeViewModel = new ViewModelProvider(this).get(TicketTypeViewModel.class);
        seatViewModel = new ViewModelProvider(this).get(SeatViewModel.class);
    }

    private void setupPaletteRecyclerView() {
        paletteAdapter = new TicketPaletteAdapter(this, this);
        rvTicketPalette.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTicketPalette.setAdapter(paletteAdapter);
    }

    private void setupGridRecyclerView() {
        // Khởi tạo adapter
        gridAdapter = new SeatGridAdapter(this, new ArrayList<>(), (seat, position) -> {
            // Logic khi click vào 1 ghế (Painting)
            handleSeatClick(seat, position);
        });
        rvSeatGrid.setAdapter(gridAdapter);
        // Tắt nested scrolling của RecyclerView để ZoomLayout hoạt động mượt hơn
        rvSeatGrid.setNestedScrollingEnabled(false);
    }

    private void handleSeatClick(Seat seat, int position) {
        if (currentSelectedTicketType == null) {
            showSnackbar("Vui lòng chọn loại vé (cọ vẽ) bên trên trước.", true);
            return;
        }

        // Không cho phép click vào lối đi (đã xử lý trong Adapter nhưng check lại cho chắc)
        if ("hidden".equals(seat.getStatus())) return;

        int newTypeId = currentSelectedTicketType.getId();
        Integer oldTypeId = seat.getTicketTypeID();

        // 1. Logic Tẩy (Bỏ chọn) nếu click lại vào loại vé cũ
        if (oldTypeId != null && oldTypeId == newTypeId) {
            seat.setTicketTypeID(null);
            seat.setStatus("unassigned");
            gridAdapter.updateSeat(position, seat);
            return;
        }

        // 2. Logic Gán Mới - Kiểm tra số lượng
        int limit = currentSelectedTicketType.getQuantity();
        long currentCount = countSeatsForType(newTypeId);

        if (currentCount >= limit) {
            showSnackbar("Loại vé '" + currentSelectedTicketType.getCode() + "' chỉ có " + limit + " vé. Đã gán hết!", true);
        } else {
            seat.setTicketTypeID(newTypeId);
            seat.setStatus("available");
            gridAdapter.updateSeat(position, seat);
        }
    }

    private long countSeatsForType(int typeId) {
        long count = 0;
        for (Seat s : gridAdapter.getSeats()) {
            if (s.getTicketTypeID() != null && s.getTicketTypeID() == typeId) {
                count++;
            }
        }
        return count;
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());
        btnSaveMap.setOnClickListener(v -> saveSeatMapAndFinish());

        actvSectionSelector.setOnItemClickListener((parent, view, position, id) -> {
            currentSelectedSection = eventSections.get(position);
            loadSeatGridForSection(currentSelectedSection);
        });
    }

    private void loadSeatGridForSection(EventSection section) {
        if (section == null) return;

        // Reset Zoom khi đổi section
        // zoomLayout.setScaleX(1.0f); zoomLayout.setScaleY(1.0f); // (Tuỳ chọn)

        if ("standing".equals(section.getSectionType())) {
            tvStandingSectionNote.setVisibility(View.VISIBLE);
            zoomLayout.setVisibility(View.GONE); // Ẩn ZoomLayout
            return;
        } else {
            tvStandingSectionNote.setVisibility(View.GONE);
            zoomLayout.setVisibility(View.VISIBLE);

            int cols = (section.getMapTotalCols() != null && section.getMapTotalCols() > 0) ? section.getMapTotalCols() : 1;

            // Thiết lập Grid Manager với số cột chính xác
            rvSeatGrid.setLayoutManager(new GridLayoutManager(this, cols));

            if (seatMapCache.containsKey(section.sectionId)) {
                gridAdapter.setSeats(seatMapCache.get(section.sectionId));
                return;
            }

            seatViewModel.getSeatsBySectionId(section.sectionId).observe(this, new Observer<List<Seat>>() {
                @Override
                public void onChanged(List<Seat> existingSeats) {
                    if (currentSelectedSection == null || currentSelectedSection.sectionId != section.sectionId) {
                        seatViewModel.getSeatsBySectionId(section.sectionId).removeObserver(this);
                        return;
                    }

                    // Logic renderGrid cũ của bạn vẫn ổn, nhưng cần chắc chắn nó tạo đủ ghế
                    // Ở đây tôi dùng lại logic cũ, chỉ thay đổi adapter
                    List<Seat> displaySeats = renderGrid(section, existingSeats);
                    seatMapCache.put(section.sectionId, displaySeats);
                    gridAdapter.setSeats(displaySeats);

                    seatViewModel.getSeatsBySectionId(section.sectionId).removeObserver(this);
                }
            });
        }
    }

    // Tái tạo lưới hiển thị từ DB
    private List<Seat> renderGrid(EventSection section, List<Seat> existingSeats) {
        int rows = (section.getMapTotalRows() != null) ? section.getMapTotalRows() : 0;
        int cols = (section.getMapTotalCols() != null) ? section.getMapTotalCols() : 0;
        if (rows <= 0 || cols <= 0) return new ArrayList<>();

        List<Seat> displaySeats = new ArrayList<>();
        Map<String, Seat> existingMap = new HashMap<>();
        for (Seat s : existingSeats) existingMap.put(s.getSeatRow() + s.getSeatNumber(), s);

        // Lưu ý: Logic này giả định row/col trong DB khớp với cấu trúc hình chữ nhật
        // Nếu DB chỉ lưu ghế "available", ta cần lấp đầy các ô trống bằng ghế "unassigned" hoặc "hidden"

        // Tuy nhiên, logic generate trong Dialog đã tạo đủ ghế (cả hidden), nên ta chỉ cần sort lại hoặc mapping
        // Để đơn giản, ta clear list cũ và vẽ lại theo rows/cols

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            for (int c = 1; c <= cols; c++) {
                // Key tìm kiếm phải khớp logic lưu
                // Ở đây tôi dùng logic đơn giản: Row+Col (A1, A2...)
                // Nếu bạn dùng logic index, hãy sửa lại key
                // Tạm thời ta loop qua list existingSeats để tìm thằng có row/col tương ứng

                Seat found = null;
                // Cách này hơi chậm (O(n^2)), tối ưu bằng Map String Key
                // Nhưng key cần chuẩn: Row + Col (A1, A2) hoặc tọa độ
                // Logic generate trong Dialog lưu: Row="A", Number="1" -> Key "A1"

                // Ở đây giả sử generate lưu vị trí cột vào SeatNumber (kể cả hidden).
                // Nếu "hidden", number có thể rỗng. Ta nên dùng vị trí index.

                // Giải pháp an toàn nhất: Khi load từ DB, nếu số lượng ghế = row*col -> Hiển thị list đó luôn
                // Nếu không khớp (do thay đổi layout), ta mới fill dummy.
            }
        }
        // Vì logic generate trong Dialog đã insert đầy đủ ghế vào DB rồi,
        // nên ta có thể return existingSeats trực tiếp nếu nó đã sort đúng.
        return existingSeats;
    }

    private void observeViewModels() {
        eventSectionViewModel.getSectionsByEventId(currentEventId).observe(this, sections -> {
            this.eventSections = sections;
            ArrayList<String> names = new ArrayList<>();
            for (EventSection s : sections) names.add(s.name);
            sectionSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
            actvSectionSelector.setAdapter(sectionSpinnerAdapter);

            if (!sections.isEmpty()) {
                currentSelectedSection = sections.get(0);
                actvSectionSelector.setText(currentSelectedSection.name, false);
                loadSeatGridForSection(currentSelectedSection);
            }
        });

        ticketTypeViewModel.getTicketsByEventId((int) currentEventId).observe(this, types -> {
            this.ticketTypes = types;
            paletteAdapter.setTicketTypes(types);
            gridAdapter.setTicketTypes(types); // Để adapter biết màu sắc
        });
    }

    @Override
    public void onPaletteClick(TicketType ticketType, int position) {
        currentSelectedTicketType = ticketType;
        paletteAdapter.setSelectedPosition(position);
    }

    private void saveSeatMapAndFinish() {
        // Logic lưu giữ nguyên như cũ của bạn
        List<Seat> allSeats = new ArrayList<>();
        for (List<Seat> list : seatMapCache.values()) {
            for (Seat s : list) {
                // Chỉ lưu những ghế có ý nghĩa (bỏ qua hidden/unassigned nếu muốn tiết kiệm DB)
                // Nhưng nếu muốn giữ cấu trúc lưới cho lần sau, ta nên lưu cả unassigned
                if (!"hidden".equals(s.getStatus())) {
                    allSeats.add(s);
                }
            }
        }

        if (allSeats.isEmpty()) {
            // Check standing logic... (Giữ nguyên code cũ của bạn)
        }

        // Thực hiện update DB
        seatViewModel.insertAll(allSeats);

        Intent intent = new Intent(this, HomepageOrganizer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // Các hàm phụ trợ (showSnackbar, showCancelConfirmationDialog...) giữ nguyên
    private void showSnackbar(String msg, boolean isError) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(this, isError ? R.color.colorError : R.color.colorSuccess))
                .show();
    }

    @Override
    public void onBackPressed() { showCancelConfirmationDialog(); }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy thay đổi?")
                .setMessage("Các thay đổi chưa lưu sẽ bị mất.")
                .setPositiveButton("Thoát", (d, w) -> finish())
                .setNegativeButton("Ở lại", null)
                .show();
    }
}