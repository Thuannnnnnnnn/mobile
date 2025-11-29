package com.example.midterm.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.EventSection;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.entity.TicketType;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EditSeatLayoutDialog extends Dialog {
    private TextView tvTicketTypeName;
    private TextView tvTotalSeatsInfo;
    private TextInputEditText etRows;
    private TextInputEditText etColumns;
    private Button btnSave;
    private Button btnCancel;

    private TicketType ticketType;
    private OnLayoutSavedListener listener;

    public interface OnLayoutSavedListener {
        void onLayoutSaved(TicketType ticketType);
    }

    public EditSeatLayoutDialog(@NonNull Context context, TicketType ticketType, OnLayoutSavedListener listener) {
        super(context);
        this.ticketType = ticketType;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_seat_layout, null);
        setContentView(view);

        setTitle("Chỉnh sửa bố cục ghế");

        initViews(view);
        fillData();
        setupListeners();
    }

    private void initViews(View view) {
        tvTicketTypeName = view.findViewById(R.id.tv_ticket_type_name);
        tvTotalSeatsInfo = view.findViewById(R.id.tv_total_seats_info);
        etRows = view.findViewById(R.id.et_rows);
        etColumns = view.findViewById(R.id.et_columns);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void fillData() {
        tvTicketTypeName.setText("Loại vé: " + ticketType.getCode());

        if (ticketType.getSeatRows() > 0) {
            etRows.setText(String.valueOf(ticketType.getSeatRows()));
        }

        if (ticketType.getSeatColumns() > 0) {
            etColumns.setText(String.valueOf(ticketType.getSeatColumns()));
        }

        updateTotalSeatsInfo();
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalSeatsInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etRows.addTextChangedListener(textWatcher);
        etColumns.addTextChangedListener(textWatcher);

        btnSave.setOnClickListener(v -> {
            if (validateAndSave()) {
                if (listener != null) {
                    listener.onLayoutSaved(ticketType);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateTotalSeatsInfo() {
        String rowsStr = etRows.getText().toString().trim();
        String colsStr = etColumns.getText().toString().trim();

        int rows = 0;
        int cols = 0;

        try {
            if (!rowsStr.isEmpty()) rows = Integer.parseInt(rowsStr);
            if (!colsStr.isEmpty()) cols = Integer.parseInt(colsStr);
        } catch (NumberFormatException e) {
            // Ignore
        }

        int total = rows * cols;
        int maxSeats = ticketType.getQuantity();

        String info = "Tổng số ghế: " + total + " (Tối đa: " + maxSeats + ")";
        tvTotalSeatsInfo.setText(info);

        if (total > maxSeats) {
            tvTotalSeatsInfo.setTextColor(0xFFFF0000); // Red
        } else {
            tvTotalSeatsInfo.setTextColor(0xFF666666); // Gray
        }
    }

    private boolean validateAndSave() {
        String rowsStr = etRows.getText().toString().trim();
        String colsStr = etColumns.getText().toString().trim();

        if (rowsStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số hàng", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (colsStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số cột", Toast.LENGTH_SHORT).show();
            return false;
        }

        int rows = 0;
        int cols = 0;

        try {
            rows = Integer.parseInt(rowsStr);
            cols = Integer.parseInt(colsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số hàng và số cột phải là số nguyên", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rows <= 0) {
            Toast.makeText(getContext(), "Số hàng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cols <= 0) {
            Toast.makeText(getContext(), "Số cột phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        int total = rows * cols;
        if (total > ticketType.getQuantity()) {
            Toast.makeText(getContext(),
                "Tổng số ghế (" + total + ") không được vượt quá số lượng vé (" + ticketType.getQuantity() + ")",
                Toast.LENGTH_LONG).show();
            return false;
        }

        ticketType.setSeatRows(rows);
        ticketType.setSeatColumns(cols);

        // Generate seats in background
        generateSeats(ticketType, rows, cols);

        return true;
    }

    private void generateSeats(TicketType ticketType, int rows, int cols) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                String currentTime = dateFormat.format(new Date());

                // Get or create EventSection for this ticket type
                List<EventSection> sections = db.eventSectionDAO().getEventSectionsByEventIdSync(ticketType.getEventID());
                EventSection section = null;

                if (sections != null) {
                    for (EventSection s : sections) {
                        if (ticketType.getCode().equals(s.getName())) {
                            section = s;
                            break;
                        }
                    }
                }

                // Create new section if not found
                if (section == null) {
                    section = new EventSection(
                            ticketType.getEventID(),
                            ticketType.getCode(),
                            "seated",
                            ticketType.getQuantity(),
                            rows,
                            cols,
                            0
                    );
                    long sectionId = db.eventSectionDAO().insert(section);
                    section.setSectionId(sectionId);
                } else {
                    // Update existing section
                    section.setCapacity(ticketType.getQuantity());
                    section.setMapTotalRows(rows);
                    section.setMapTotalCols(cols);
                    db.eventSectionDAO().update(section);
                }

                // Delete old seats for this section and ticket type
                db.seatDAO().deleteByTicketTypeId(ticketType.getId());

                // Create new seats
                List<Seat> seatsToInsert = new ArrayList<>();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        char rowLetter = (char) ('A' + i);
                        String seatRow = String.valueOf(rowLetter);
                        String seatNumber = String.valueOf(j + 1);

                        Seat seat = new Seat(
                                section.getSectionId(),
                                ticketType.getId(),
                                seatRow,
                                seatNumber,
                                "available",
                                currentTime,
                                currentTime
                        );
                        seatsToInsert.add(seat);
                    }
                }

                // Bulk insert seats
                for (Seat seat : seatsToInsert) {
                    db.seatDAO().insert(seat);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
