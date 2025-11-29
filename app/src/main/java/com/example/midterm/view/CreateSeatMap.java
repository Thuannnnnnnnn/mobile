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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
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

public class CreateSeatMap extends AppCompatActivity implements TicketPaletteAdapter.OnPaletteClickListener {
    private ImageButton btnBack;
    private Button btnSaveMap;
    private AutoCompleteTextView actvSectionSelector;
    private RecyclerView rvTicketPalette, rvSeatGrid;
    private TextView tvStandingSectionNote;
    private View rootView;

    // ViewModels
    private EventViewModel eventViewModel;
    private GuestViewModel guestViewModel;
    private EventSectionViewModel eventSectionViewModel;
    private TicketTypeViewModel ticketTypeViewModel;
    private SeatViewModel seatViewModel;

    // Adapters
    private ArrayAdapter<String> sectionSpinnerAdapter;
    private TicketPaletteAdapter paletteAdapter;
    private SeatGridAdapter gridAdapter;

    // Data
    private long currentEventId = -1L;
    private List<EventSection> eventSections = new ArrayList<>();
    private List<TicketType> ticketTypes = new ArrayList<>();

    // State (Trạng thái)
    private EventSection currentSelectedSection;
    private TicketType currentSelectedTicketType; // "Cọ vẽ" đang được chọn
    private Map<Long, List<Seat>> seatMapCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_seat_map);

        initViews();

        currentEventId = getIntent().getLongExtra("room_id", -1L);
        if (currentEventId == -1L) {
            finish();
            return;
        }

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
        // Khởi tạo adapter nhưng chưa có dữ liệu
        gridAdapter = new SeatGridAdapter(this, new ArrayList<>(), (seat, position) -> {
            // Đây là sự kiện "Vẽ" (Painting)
            if (currentSelectedTicketType == null) {
                showSnackbar("Vui lòng chọn một loại vé (cọ vẽ) trước.", true);
                return;
            }
            int newTicketTypeId = currentSelectedTicketType.getId();
            Integer currentSeatTicketTypeId = seat.getTicketTypeID();

            // Logic "Tẩy" (Un-assign / Toggle off)
            if (currentSeatTicketTypeId != null && currentSeatTicketTypeId == newTicketTypeId) {
                seat.setTicketTypeID(null);
                seat.setStatus("unassigned");
                gridAdapter.updateSeat(position, seat);
                return;
            }

            // <-- LOGIC QUAN TRỌNG: Kiểm tra giới hạn (Count) -->
            int limit = currentSelectedTicketType.getQuantity();
            long currentCount = 0;
            for (Seat s : gridAdapter.getSeats()) { // Lấy danh sách ghế hiện tại từ adapter
                if (s.getTicketTypeID() != null && s.getTicketTypeID() == newTicketTypeId) {
                    currentCount++;
                }
            }

            // Ra quyết định
            if (currentCount >= limit) {
                // Đã đạt giới hạn -> Thông báo lỗi
                showSnackbar("Đã đạt giới hạn (" + limit + ") cho loại vé '" + currentSelectedTicketType.getCode() + "'.", true);
            } else {
                // Vẫn còn chỗ -> "Vẽ" (Gán)
                seat.setTicketTypeID(newTicketTypeId);
                seat.setStatus("available");
                gridAdapter.updateSeat(position, seat);
            }
        });
        rvSeatGrid.setAdapter(gridAdapter);
    }

    private void setupListeners() {
        // Nút Hủy (Back)
        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());

        // Nút Lưu (Hoàn tất)
        btnSaveMap.setOnClickListener(v -> saveSeatMapAndFinish());

        // Dropdown chọn Khu vực
        actvSectionSelector.setOnItemClickListener((parent, view, position, id) -> {
            currentSelectedSection = eventSections.get(position);
            loadSeatGridForSection(currentSelectedSection);
        });
    }

    //Tải và vẽ lưới (Grid) khi một Khu vực (Section) được chọn
    private void loadSeatGridForSection(EventSection section) {
        if (section == null) return;

        // Xử lý Khu Đứng (Standing)
        if ("standing".equals(section.getSectionType())) {
            tvStandingSectionNote.setVisibility(View.VISIBLE);
            rvSeatGrid.setVisibility(View.GONE);
            gridAdapter.setSeats(new ArrayList<>()); // Xóa lưới cũ
            return;
        }
        // Xử lý Khu Ngồi (Seated)
        else {
            tvStandingSectionNote.setVisibility(View.GONE);
            rvSeatGrid.setVisibility(View.VISIBLE);

            // Set LayoutManager
            int numColumns = (section.getMapTotalCols() != null && section.getMapTotalCols() > 0) ? section.getMapTotalCols() : 1;
            rvSeatGrid.setLayoutManager(new GridLayoutManager(this, numColumns));

            // Nếu đã có trong cache (đã "vẽ" hoặc đã load), dùng cache
            if (seatMapCache.containsKey(section.sectionId)) {
                gridAdapter.setSeats(seatMapCache.get(section.sectionId));
                return;
            }

            seatViewModel.getSeatsBySectionId(section.sectionId).observe(this, new Observer<List<Seat>>() {
                @Override
                public void onChanged(List<Seat> existingSeats) {
                    // Kiểm tra xem người dùng có đổi khu vực trong khi đang load không
                    if (currentSelectedSection == null || currentSelectedSection.sectionId != section.sectionId) {
                        // Nếu đã đổi, gỡ observer và bỏ qua
                        seatViewModel.getSeatsBySectionId(section.sectionId).removeObserver(this);
                        return;
                    }
                    List<Seat> displaySeats = renderGrid(section, existingSeats); // Tạo danh sách ghế cho lần đầu tiên
                    seatMapCache.put(section.sectionId, displaySeats); // ĐƯA VÀO CACHE
                    gridAdapter.setSeats(displaySeats); // Cập nhật Adapter
                    // Gỡ observer để nó không chạy lại và ghi đè các thay đổi của người dùng
                    seatViewModel.getSeatsBySectionId(section.sectionId).removeObserver(this);
                }
            });
        }
    }
    //Tái tạo lưới (Grid) với các ghế đã có và các ghế trống
    private List<Seat> renderGrid(EventSection section, List<Seat> existingSeats) {
        int rows = (section.getMapTotalRows() != null) ? section.getMapTotalRows() : 0;
        int cols = (section.getMapTotalCols() != null) ? section.getMapTotalCols() : 0;
        List<Seat> displaySeats = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            String rowName = String.valueOf((char) ('A' + r));
            for (int c = 0; c < cols; c++) {
                String colName = String.valueOf(c + 1);

                Seat foundSeat = null;
                for (Seat existing : existingSeats) {
                    if (rowName.equals(existing.getSeatRow()) && colName.equals(existing.getSeatNumber())) {
                        foundSeat = existing;
                        break;
                    }
                }
                if (foundSeat != null) {
                    displaySeats.add(foundSeat);
                } else {
                    displaySeats.add(new Seat(section.sectionId, null, rowName, colName, "unassigned", null, null));
                }
            }
        }
        return displaySeats; // <- Trả về danh sách
    }
    //Quan sát (Observe) tất cả LiveData
    private void observeViewModels() {
        // Tải danh sách Khu vực (Sections)
        eventSectionViewModel.getSectionsByEventId(currentEventId).observe(this, sections -> {
            this.eventSections = sections;
            ArrayList<String> sectionNames = new ArrayList<>();
            for (EventSection s : sections) {
                sectionNames.add(s.name);
            }
            // Đổ vào Dropdown
            sectionSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sectionNames);
            actvSectionSelector.setAdapter(sectionSpinnerAdapter);
        });
        //Tải danh sách Loại vé (Ticket Types)
        ticketTypeViewModel.getTicketsByEventId((int) currentEventId).observe(this, ticketTypes -> {
            this.ticketTypes = ticketTypes;
            paletteAdapter.setTicketTypes(ticketTypes);
            gridAdapter.setTicketTypes(this.ticketTypes);
        });
    }
    //Sự kiện khi click vào 1 item trong Bảng màu vé (Palette)
    @Override
    public void onPaletteClick(TicketType ticketType, int position) {
        currentSelectedTicketType = ticketType;
        paletteAdapter.setSelectedPosition(position); // Báo cho adapter đổi màu item
    }

    //Lưu sơ đồ đã vẽ và Hoàn tất
    private void saveSeatMapAndFinish() {
        // TẠO DANH SÁCH LƯU TỔNG TỪ CACHE ---
        List<Seat> allSeatsToSave = new ArrayList<>();
        // Lặp qua tất cả các giá trị (List<Seat>) trong cache
        for (List<Seat> sectionSeats : seatMapCache.values()) {
            // Lặp qua từng ghế trong danh sách của khu vực đó
            for (Seat seat : sectionSeats) {
                // Chỉ lưu những ghế ĐÃ ĐƯỢC GÁN
                if (seat.getTicketTypeID() != null) {
                    allSeatsToSave.add(seat);
                }
            }
        }
        // VALIDATION (TRÊN DANH SÁCH TỔNG) ---
        if (allSeatsToSave.isEmpty()) {
            // Kiểm tra xem có phải tất cả các khu đều là 'standing' không
            boolean allStanding = true;
            for(EventSection s : eventSections) {
                if("seated".equals(s.getSectionType())) {
                    allStanding = false;
                    break;
                }
            }
            if(!allStanding) {
                showSnackbar("Bạn chưa gán loại vé cho bất kỳ ghế nào.", true);
                return;
            }
        }

        // Kiểm tra giới hạn (Count) khi LƯU
        Map<Integer, Integer> seatCountPerType = new HashMap<>();
        for (Seat s : allSeatsToSave) { // Dùng allSeatsToSave
            Integer typeId = s.getTicketTypeID();
            if (typeId != null) {
                seatCountPerType.put(typeId, seatCountPerType.getOrDefault(typeId, 0) + 1);
            }
        }

        for (TicketType type : this.ticketTypes) {
            int typeId = type.getId();
            int limit = type.getQuantity();
            int assignedCount = seatCountPerType.getOrDefault(typeId, 0);

            if (assignedCount > limit) {
                showSnackbar("Lỗi: Loại vé '" + type.getCode() + "' có " + assignedCount + " ghế được gán, nhưng giới hạn là " + limit + ".", true);
                return; // Dừng lại, không lưu
            }
        }

        // CẬP NHẬT TIMESTAMP (TRÊN DANH SÁCH TỔNG) ---
        String now = getCurrentTimestamp();
        for (Seat seat : allSeatsToSave) { // Dùng allSeatsToSave
            if (seat.getCreatedAt() == null) {
                seat.setCreatedAt(now);
            }
            seat.setUpdatedAt(now);
        }
        //LƯU VÀO DATABASE ---
        seatViewModel.insertAll(allSeatsToSave); // Lưu danh sách tổng
        showSnackbar("Sự kiện đã được tạo thành công!", false);

        // Quay về Homepage
        Intent intent = new Intent(this, HomepageOrganizer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        showCancelConfirmationDialog();
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy tạo sự kiện?")
                .setMessage("Đây là bước cuối cùng. Nếu bạn hủy, toàn bộ sự kiện (Guest, Section, Vé...) sẽ bị xóa.")
                .setPositiveButton("Xóa & Hủy", (dialog, which) -> {
                    deleteEventAndFinish();
                })
                .setNegativeButton("Tiếp tục", null)
                .setIcon(R.drawable.warning)
                .show();
    }
    //Logic Hủy (Xóa tất cả)
    private void deleteEventAndFinish() {
        guestViewModel.deleteGuestsAndCrossRefsByEventId(currentEventId);
        ticketTypeViewModel.deleteTicketsByEventId(currentEventId);
        eventViewModel.deleteEventById(currentEventId);

        showSnackbar("Đã hủy tạo sự kiện.", true);

        // Quay về Homepage
        Intent intent = new Intent(this, HomepageOrganizer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        int color = isError ? ContextCompat.getColor(this, R.color.colorError) : ContextCompat.getColor(this, R.color.colorSuccess);
        snackbarView.setBackgroundTintList(ColorStateList.valueOf(color));
        snackbar.show();
    }
    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }
}