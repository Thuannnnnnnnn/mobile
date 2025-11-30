package com.example.midterm.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class EditSeatLayoutDialog extends Dialog {
    private TextView tvTicketTypeName;
    private TextView tvTotalSeatsInfo;
    private TextInputEditText etRows;
    private TextInputEditText etColumns;
    private TextInputEditText etAisles; // Nhập các cột là lối đi (ví dụ: 5, 10)
    private Button btnSave;
    private Button btnCancel;

    private TicketType ticketType;
    private OnLayoutSavedListener listener;

    // Interface để báo cho Activity biết khi đã lưu xong
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
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Ẩn tiêu đề mặc định

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_seat_layout, null);
        setContentView(view);

        // Chỉnh kích thước dialog cho đẹp (95% chiều rộng màn hình)
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews(view);
        fillData();
        setupListeners();
    }

    private void initViews(View view) {
        tvTicketTypeName = view.findViewById(R.id.tv_ticket_type_name);
        tvTotalSeatsInfo = view.findViewById(R.id.tv_total_seats_info);
        etRows = view.findViewById(R.id.et_rows);
        etColumns = view.findViewById(R.id.et_columns);
        etAisles = view.findViewById(R.id.et_aisles); // Đảm bảo ID này có trong XML
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void fillData() {
        tvTicketTypeName.setText("Cấu hình sơ đồ: " + ticketType.getCode());

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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalSeatsInfo();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etRows.addTextChangedListener(textWatcher);
        etColumns.addTextChangedListener(textWatcher);

        btnSave.setOnClickListener(v -> {
            if (validateAndSave()) {
                // Không dismiss ngay mà đợi generate xong hoặc dismiss luôn tùy UX
                // Ở đây dismiss luôn và chạy background
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateTotalSeatsInfo() {
        int rows = parseInteger(etRows);
        int cols = parseInteger(etColumns);
        int total = rows * cols;
        int maxSeats = ticketType.getQuantity();

        String info = String.format(Locale.getDefault(),
                "Lưới: %d dòng x %d cột = %d ô\nVé bán ra: %d", rows, cols, total, maxSeats);

        tvTotalSeatsInfo.setText(info);

        // Cảnh báo nếu lưới nhỏ hơn số vé (không đủ chỗ xếp)
        if (total < maxSeats && total > 0) {
            tvTotalSeatsInfo.setTextColor(0xFFFF0000); // Đỏ
        } else {
            tvTotalSeatsInfo.setTextColor(0xFF666666); // Xám
        }
    }

    private int parseInteger(TextInputEditText et) {
        try {
            String s = et.getText().toString().trim();
            return s.isEmpty() ? 0 : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean validateAndSave() {
        int rows = parseInteger(etRows);
        int cols = parseInteger(etColumns);

        if (rows <= 0 || cols <= 0) {
            Toast.makeText(getContext(), "Số hàng và cột phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Logic lối đi (Aisles)
        Set<Integer> aisleCols = new HashSet<>();
        String aisleStr = etAisles.getText().toString().trim();
        if (!aisleStr.isEmpty()) {
            String[] parts = aisleStr.split(",");
            for (String p : parts) {
                try {
                    int colIndex = Integer.parseInt(p.trim());
                    if (colIndex > 0 && colIndex <= cols) {
                        aisleCols.add(colIndex);
                    }
                } catch (Exception e) {
                    // Bỏ qua lỗi nhập liệu
                }
            }
        }

        // Cập nhật thông tin vào TicketType (chỉ tạm thời để lưu logic)
        ticketType.setSeatRows(rows);
        ticketType.setSeatColumns(cols);

        // Bắt đầu tạo ghế trong Database
        generateSeatsInBackground(ticketType, rows, cols, aisleCols);

        return true;
    }

    private void generateSeatsInBackground(TicketType ticketType, int rows, int cols, Set<Integer> aisleCols) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // 1. Tìm hoặc Tạo EventSection tương ứng
                List<EventSection> sections = db.eventSectionDAO().getEventSectionsByEventIdSync(ticketType.getEventID());
                EventSection section = null;
                for (EventSection s : sections) {
                    // Giả định tên Section trùng tên loại vé hoặc logic map của bạn
                    if (s.getName().equals(ticketType.getCode())) {
                        section = s;
                        break;
                    }
                }

                long sectionId;
                if (section == null) {
                    section = new EventSection(
                            ticketType.getEventID(),
                            ticketType.getCode(),
                            "seated",
                            ticketType.getQuantity(),
                            rows, cols, 0
                    );
                    sectionId = db.eventSectionDAO().insert(section);
                } else {
                    sectionId = section.getSectionId();
                    section.setMapTotalRows(rows);
                    section.setMapTotalCols(cols);
                    db.eventSectionDAO().update(section);
                }

                // 2. Xóa ghế cũ để tạo lại
                db.seatDAO().deleteByTicketTypeId(ticketType.getId());

                // 3. Tạo danh sách ghế mới
                List<Seat> seats = new ArrayList<>();
                int seatCounter = 1;

                for (int r = 0; r < rows; r++) {
                    char rowName = (char) ('A' + r); // A, B, C...
                    // Nếu hết bảng chữ cái (Z), có thể dùng logic AA, AB... (chưa xử lý ở đây)

                    for (int c = 1; c <= cols; c++) {
                        // Mặc định là ghế trống
                        String status = "available";
                        String seatNumDisplay = String.valueOf(seatCounter);
                        Integer typeId = ticketType.getId();

                        // Nếu cột này là lối đi -> set status hidden
                        if (aisleCols.contains(c)) {
                            status = "hidden";
                            seatNumDisplay = ""; // Không có số
                            typeId = null; // Không gán loại vé
                        } else {
                            seatCounter++;
                        }

                        // Nếu số lượng ghế đã vượt quá số vé bán ra -> set unassigned (hoặc vẫn để available tùy logic)
                        // Ở đây ta cứ tạo available để người dùng thấy và xóa bớt bằng tay nếu muốn.

                        Seat seat = new Seat(
                                sectionId,
                                typeId,
                                String.valueOf(rowName),
                                String.valueOf(c), // Vị trí cột thực tế (để vẽ grid)
                                status,
                                now, now
                        );
                        // Lưu tên hiển thị vào seatNumber (hoặc dùng field riêng nếu có)
                        if (!status.equals("hidden")) {
                            seat.setSeatNumber(seatNumDisplay);
                        } else {
                            seat.setSeatNumber("AISLE");
                        }

                        seats.add(seat);
                    }
                }

                // 4. Lưu vào DB
                for (Seat s : seats) {
                    db.seatDAO().insert(s);
                }

                // Callback về UI thread
                if (listener != null) {
                    // Cần chạy trên UI thread nếu listener thao tác UI
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            listener.onLayoutSaved(ticketType)
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}