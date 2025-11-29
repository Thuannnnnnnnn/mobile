package com.example.midterm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.view.dialog.EditSeatLayoutDialog;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.TicketTypeViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageSeatFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";
    private int eventId;

    private TextView tvTotalSeats;
    private TextView tvBookedSeats;
    private TextView tvAvailableSeats;
    private AutoCompleteTextView actvSeatZone;
    private Button btnEditLayout;
    private GridLayout seatMapGrid;
    private LinearLayout legendLayout;

    private TicketTypeViewModel ticketTypeViewModel;
    private EventViewModel eventViewModel;

    private List<TicketType> ticketTypes = new ArrayList<>();
    private TicketType selectedTicketType;
    private int totalCapacity = 0;
    private int totalSold = 0;

    public static ManageSeatFragment newInstance(int eventId) {
        ManageSeatFragment fragment = new ManageSeatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_seat, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        loadData();
        setupListeners();
    }

    private void initViews(View view) {
        tvTotalSeats = view.findViewById(R.id.tv_total_seats);
        tvBookedSeats = view.findViewById(R.id.tv_booked_seats);
        tvAvailableSeats = view.findViewById(R.id.tv_available_seats);
        actvSeatZone = view.findViewById(R.id.actv_seat_zone);
        btnEditLayout = view.findViewById(R.id.btn_edit_layout);
        seatMapGrid = view.findViewById(R.id.seat_map_grid);
        legendLayout = view.findViewById(R.id.legend_layout);
    }

    private void initViewModels() {
        ticketTypeViewModel = new ViewModelProvider(this).get(TicketTypeViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
    }

    private void loadData() {
        ticketTypeViewModel.getTicketsByEventId(eventId).observe(getViewLifecycleOwner(), types -> {
            if (types != null && !types.isEmpty()) {
                ticketTypes = types;
                calculateStatistics();
                updateUI();
                setupZoneSelector();
            } else {
                ticketTypes = new ArrayList<>();
                updateUI();
            }
        });
    }

    private void calculateStatistics() {
        totalCapacity = 0;
        totalSold = 0;

        for (TicketType type : ticketTypes) {
            totalCapacity += type.getQuantity();
            totalSold += type.getSoldQuantity();
        }
    }

    private void updateUI() {
        tvTotalSeats.setText(String.valueOf(totalCapacity));
        tvBookedSeats.setText(String.valueOf(totalSold));

        int available = totalCapacity - totalSold;
        tvAvailableSeats.setText(String.valueOf(available));
    }

    private void setupZoneSelector() {
        List<String> zoneNames = new ArrayList<>();
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        for (TicketType type : ticketTypes) {
            String zoneName = type.getCode() + " (" + currencyFormat.format(type.getPrice()) + " VNĐ)";
            zoneNames.add(zoneName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                zoneNames
        );

        actvSeatZone.setAdapter(adapter);

        if (!zoneNames.isEmpty()) {
            actvSeatZone.setText(zoneNames.get(0), false);
        }

        actvSeatZone.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < ticketTypes.size()) {
                selectedTicketType = ticketTypes.get(position);
                generateSeatMap(selectedTicketType);
                updateLegend(selectedTicketType);
            }
        });

        // Select first ticket type by default
        if (!ticketTypes.isEmpty()) {
            selectedTicketType = ticketTypes.get(0);
            generateSeatMap(selectedTicketType);
            updateLegend(selectedTicketType);
        }
    }

    private void showZoneStatistics(TicketType ticketType) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        int available = ticketType.getQuantity() - ticketType.getSoldQuantity();
        double fillRate = ticketType.getQuantity() > 0 ?
                (ticketType.getSoldQuantity() * 100.0 / ticketType.getQuantity()) : 0;

        String message = String.format(Locale.getDefault(),
                "📊 Thống kê khu vực: %s\n\n" +
                "💰 Giá vé: %s VNĐ\n" +
                "📦 Tổng số ghế: %d\n" +
                "✅ Đã bán: %d ghế\n" +
                "🆓 Còn trống: %d ghế\n" +
                "📈 Tỷ lệ lấp đầy: %.1f%%",
                ticketType.getCode(),
                currencyFormat.format(ticketType.getPrice()),
                ticketType.getQuantity(),
                ticketType.getSoldQuantity(),
                available,
                fillRate
        );

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Chi tiết khu vực")
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void setupListeners() {
        btnEditLayout.setOnClickListener(v -> {
            if (selectedTicketType == null) {
                Toast.makeText(getContext(), "Vui lòng chọn loại vé", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditLayoutDialog();
        });
    }

    private void showEditLayoutDialog() {
        EditSeatLayoutDialog dialog = new EditSeatLayoutDialog(
                requireContext(),
                selectedTicketType,
                ticketType -> {
                    ticketTypeViewModel.updateTicket(ticketType);
                    generateSeatMap(ticketType);
                    updateLegend(ticketType);
                    Toast.makeText(getContext(), "Đã cập nhật bố cục ghế", Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }

    private void generateSeatMap(TicketType ticketType) {
        seatMapGrid.removeAllViews();

        int rows = ticketType.getSeatRows();
        int cols = ticketType.getSeatColumns();

        if (rows <= 0 || cols <= 0) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Chưa cấu hình bố cục ghế.\nVui lòng bấm 'CHỈNH SỬA BỐ CỤC' để thiết lập.");
            emptyView.setTextSize(14);
            emptyView.setPadding(16, 32, 16, 32);
            seatMapGrid.addView(emptyView);
            return;
        }

        seatMapGrid.setColumnCount(cols);
        seatMapGrid.setRowCount(rows);

        int soldQuantity = ticketType.getSoldQuantity();
        int totalSeats = rows * cols;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int seatIndex = i * cols + j;
                String seatNumber = getSeatLabel(i, j);

                Button seatButton = new Button(getContext());
                seatButton.setText(seatNumber);
                seatButton.setTextSize(10);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (int) (40 * getResources().getDisplayMetrics().density);
                params.height = (int) (40 * getResources().getDisplayMetrics().density);
                params.setMargins(4, 4, 4, 4);
                seatButton.setLayoutParams(params);

                // Giả định: ghế đầu tiên là đã bán
                boolean isBooked = seatIndex < soldQuantity;

                if (isBooked) {
                    seatButton.setBackgroundColor(0xFFBDBDBD); // Gray - Đã bán
                } else {
                    seatButton.setBackgroundColor(0xFF4CAF50); // Green - Còn trống
                }

                seatButton.setOnClickListener(v -> showSeatInfo(seatNumber, isBooked));

                seatMapGrid.addView(seatButton);
            }
        }
    }

    private String getSeatLabel(int row, int col) {
        char rowLetter = (char) ('A' + row);
        return rowLetter + String.valueOf(col + 1);
    }

    private void showSeatInfo(String seatNumber, boolean isBooked) {
        String status = isBooked ? "Đã bán" : "Còn trống";
        String message = "Ghế: " + seatNumber + "\nTrạng thái: " + status;

        new AlertDialog.Builder(requireContext())
                .setTitle("Thông tin ghế")
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void updateLegend(TicketType ticketType) {
        // Clear existing legend
        legendLayout.removeAllViews();

        int available = ticketType.getQuantity() - ticketType.getSoldQuantity();
        int sold = ticketType.getSoldQuantity();

        // Create available seats legend
        LinearLayout availableLayout = createLegendItem(
                0xFF4CAF50,
                "Còn trống (" + available + ")"
        );
        legendLayout.addView(availableLayout);

        // Add margin
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) availableLayout.getLayoutParams();
        params.setMarginEnd((int) (16 * getResources().getDisplayMetrics().density));
        availableLayout.setLayoutParams(params);

        // Create booked seats legend
        LinearLayout bookedLayout = createLegendItem(
                0xFFBDBDBD,
                "Đã bán (" + sold + ")"
        );
        legendLayout.addView(bookedLayout);
    }

    private LinearLayout createLegendItem(int color, String text) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        View colorBox = new View(getContext());
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density)
        );
        colorBox.setLayoutParams(boxParams);
        colorBox.setBackgroundColor(color);

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(14);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMarginStart((int) (4 * getResources().getDisplayMetrics().density));
        textView.setLayoutParams(textParams);

        layout.addView(colorBox);
        layout.addView(textView);

        return layout;
    }
}
