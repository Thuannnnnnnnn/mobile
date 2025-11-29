package com.example.midterm.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences; // Thêm import này
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.midterm.R;
import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.repository.DiscountRepository;
import com.example.midterm.viewModel.DiscountViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateVoucher extends AppCompatActivity {

    private AutoCompleteTextView actvEventSelect, actvDiscountType;
    private TextInputEditText etCode, etValue, etMinPurchase, etMaxDiscount, etUsageLimit;
    private TextInputEditText etStartDate, etStartTime, etEndDate, etEndTime;
    private SwitchMaterial switchActive;
    private Button btnSubmit;
    private ImageButton btnBack;

    // Logic Variables
    private DiscountViewModel discountViewModel;
    private List<Event> eventList = new ArrayList<>();
    private int selectedEventId = -1;
    private int organizerId = -1;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_voucher);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        organizerId = prefs.getInt("user_id", -1);

        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        // 3. Lấy ID sự kiện được truyền từ màn hình ManageVouchers (nếu có)
        selectedEventId = getIntent().getIntExtra("pre_selected_event_id", -1);

        initViews();
        setupDropdowns();
        setupDateTimePickers();

        loadEvents();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        actvEventSelect = findViewById(R.id.actv_event_select);
        etCode = findViewById(R.id.edit_discount_code);
        actvDiscountType = findViewById(R.id.actv_discount_type);
        etValue = findViewById(R.id.edit_discount_value);
        etMinPurchase = findViewById(R.id.edit_min_purchase);
        etMaxDiscount = findViewById(R.id.edit_max_discount);
        etUsageLimit = findViewById(R.id.edit_usage_limit);

        etStartDate = findViewById(R.id.et_start_date);
        etStartTime = findViewById(R.id.et_start_time);
        etEndDate = findViewById(R.id.et_end_date);
        etEndTime = findViewById(R.id.et_end_time);

        switchActive = findViewById(R.id.switch_is_active);
        btnSubmit = findViewById(R.id.btn_create_discount_submit);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> validateAndCreateVoucher());
    }

    private void setupDropdowns() {
        // Setup Discount Type Dropdown
        String[] types = {"Phần trăm (%)", "Số tiền cố định (VNĐ)"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        actvDiscountType.setAdapter(typeAdapter);
        actvDiscountType.setText(types[0], false);
    }

    private void loadEvents() {
        if (organizerId == -1) {
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Quan sát LiveData từ ViewModel
        discountViewModel.getAllEvents(organizerId).observe(this, events -> {
            this.eventList = events; // Cập nhật list toàn cục

            List<String> eventNames = new ArrayList<>();
            int preSelectedIndex = -1;

            for (int i = 0; i < eventList.size(); i++) {
                Event e = eventList.get(i);
                eventNames.add(e.getEventName());
                if (e.getId() == selectedEventId) {
                    preSelectedIndex = i;
                }
            }

            ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, eventNames);
            actvEventSelect.setAdapter(eventAdapter);

            // Tự động chọn nếu có ID truyền sang hoặc chọn cái đầu tiên
            if (preSelectedIndex != -1) {
                actvEventSelect.setText(eventNames.get(preSelectedIndex), false);
            } else if (!eventList.isEmpty()) {
                 //Chọn mặc định sự kiện đầu tiên
                 actvEventSelect.setText(eventNames.get(0), false);
                 selectedEventId = eventList.get(0).getId();
            }

            actvEventSelect.setOnItemClickListener((parent, view, position, id) -> {
                selectedEventId = eventList.get(position).getId();
            });
        });
    }

    private void setupDateTimePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(startCalendar, etStartDate));
        etStartTime.setOnClickListener(v -> showTimePicker(startCalendar, etStartTime));
        etEndDate.setOnClickListener(v -> showDatePicker(endCalendar, etEndDate));
        etEndTime.setOnClickListener(v -> showTimePicker(endCalendar, etEndTime));

        updateLabel(startCalendar, etStartDate, etStartTime);
        endCalendar.add(Calendar.DAY_OF_YEAR, 7);
        updateLabel(endCalendar, etEndDate, etEndTime);
    }

    private void showDatePicker(Calendar calendar, TextInputEditText et) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel(calendar, et);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Calendar calendar, TextInputEditText et) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeLabel(calendar, et);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateLabel(Calendar calendar, TextInputEditText etDate, TextInputEditText etTime) {
        updateDateLabel(calendar, etDate);
        updateTimeLabel(calendar, etTime);
    }

    private void updateDateLabel(Calendar calendar, TextInputEditText et) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        et.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeLabel(Calendar calendar, TextInputEditText et) {
        String myFormat = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        et.setText(sdf.format(calendar.getTime()));
    }

    private void validateAndCreateVoucher() {
        if (selectedEventId == -1) {
            Toast.makeText(this, "Vui lòng chọn sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = etCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            etCode.setError("Vui lòng nhập mã");
            return;
        }

        String valStr = etValue.getText().toString().trim();
        if (TextUtils.isEmpty(valStr)) {
            etValue.setError("Nhập giá trị giảm");
            return;
        }
        double value = Double.parseDouble(valStr);

        String typeStr = actvDiscountType.getText().toString();
        String discountType = typeStr.contains("Phần trăm") ? "percentage" : "fixed";

        if (discountType.equals("percentage") && value > 100) {
            etValue.setError("Phần trăm không được quá 100%");
            return;
        }

        double minPurchase = 0;
        if (!TextUtils.isEmpty(etMinPurchase.getText())) {
            minPurchase = Double.parseDouble(etMinPurchase.getText().toString());
        }

        double maxDiscount = 0;
        if (!TextUtils.isEmpty(etMaxDiscount.getText())) {
            maxDiscount = Double.parseDouble(etMaxDiscount.getText().toString());
        }

        int usageLimit = 0;
        if (!TextUtils.isEmpty(etUsageLimit.getText())) {
            usageLimit = Integer.parseInt(etUsageLimit.getText().toString());
        }

        if (startCalendar.after(endCalendar)) {
            Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startDateStr = dbFormat.format(startCalendar.getTime());
        String endDateStr = dbFormat.format(endCalendar.getTime());
        String createdAtStr = dbFormat.format(new Date());

        // --- 5. TẠO ĐỐI TƯỢNG (DÙNG CONSTRUCTOR CỦA BẠN) ---
        Discount discount = new Discount(
                selectedEventId,
                code.toUpperCase(),
                discountType,
                value,
                minPurchase,
                maxDiscount,
                usageLimit,
                startDateStr,
                endDateStr,
                createdAtStr
        );
        discount.setActive(switchActive.isChecked());

        discountViewModel.insertDiscount(discount, new DiscountRepository.OnActionCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(CreateVoucher.this, "Tạo voucher thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateVoucher.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}