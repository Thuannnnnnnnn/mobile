package com.example.midterm.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateVoucherActivity extends AppCompatActivity {
    private AutoCompleteTextView actvEventSelect, actvDiscountType;
    private TextInputEditText etCode, etValue, etMinPurchase, etMaxDiscount, etUsageLimit;
    private TextInputEditText etStartDate, etStartTime, etEndDate, etEndTime;
    private SwitchMaterial switchActive;
    private Button btnSave;
    private ImageButton btnBack;
    
    private DiscountViewModel discountViewModel;
    private Discount currentDiscount; // Voucher đang được sửa
    private List<Event> eventList = new ArrayList<>();
    private int selectedEventId = -1;
    private int organizerId = -1;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_voucher); // Nhớ tạo file xml này

        //Nhận dữ liệu từ Intent
        currentDiscount = (Discount) getIntent().getSerializableExtra("discount_item");
        if (currentDiscount == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu voucher", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        selectedEventId = currentDiscount.getEventId(); // Lấy event ID cũ

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        organizerId = prefs.getInt("user_id", -1);

        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        initViews();
        setupDateTimePickers();

        // Đổ dữ liệu cũ lên màn hình
        populateData();

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
        btnSave = findViewById(R.id.btn_save_changes); // ID nút lưu

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> validateAndUpdate());

        // Setup Discount Type Dropdown
        String[] types = {"Phần trăm (%)", "Số tiền (VNĐ)"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        actvDiscountType.setAdapter(typeAdapter);
    }

    private void populateData() {
        etCode.setText(currentDiscount.getCode());
        etCode.setEnabled(false);
        
        if (currentDiscount.getDiscountValue() == (long) currentDiscount.getDiscountValue()) {
            etValue.setText(String.format(Locale.getDefault(), "%d", (long) currentDiscount.getDiscountValue()));
        } else {
            etValue.setText(String.valueOf(currentDiscount.getDiscountValue()));
        }

        // Loại giảm giá
        String typeDisplay = "percentage".equals(currentDiscount.getDiscountType()) ? "Phần trăm (%)" : "Số tiền (VNĐ)";
        actvDiscountType.setText(typeDisplay, false);
        
        if (currentDiscount.getMinPurchase() > 0) etMinPurchase.setText(String.valueOf(currentDiscount.getMinPurchase()));
        if (currentDiscount.getMaxDiscount() > 0) etMaxDiscount.setText(String.valueOf(currentDiscount.getMaxDiscount()));
        if (currentDiscount.getUsageLimit() > 0) etUsageLimit.setText(String.valueOf(currentDiscount.getUsageLimit()));

        switchActive.setChecked(currentDiscount.isActive());
        
        try {
            if (currentDiscount.getStartDate() != null) {
                Date start = dbFormat.parse(currentDiscount.getStartDate());
                if (start != null) {
                    startCalendar.setTime(start);
                    updateLabel(startCalendar, etStartDate, etStartTime);
                }
            }
            if (currentDiscount.getEndDate() != null) {
                Date end = dbFormat.parse(currentDiscount.getEndDate());
                if (end != null) {
                    endCalendar.setTime(end);
                    updateLabel(endCalendar, etEndDate, etEndTime);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        discountViewModel.getAllEvents(organizerId).observe(this, events -> {
            this.eventList = events;
            List<String> eventNames = new ArrayList<>();
            int selectedIndex = -1;

            for (int i = 0; i < eventList.size(); i++) {
                Event e = eventList.get(i);
                eventNames.add(e.getEventName());
                if (e.getId() == selectedEventId) {
                    selectedIndex = i;
                }
            }

            ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, eventNames);
            actvEventSelect.setAdapter(eventAdapter);
            
            if (selectedIndex != -1) {
                actvEventSelect.setText(eventNames.get(selectedIndex), false);
            }

            actvEventSelect.setOnItemClickListener((parent, view, position, id) -> {
                selectedEventId = eventList.get(position).getId();
            });
        });
    }

    private void validateAndUpdate() {

        if (selectedEventId == -1) {
            Toast.makeText(this, "Vui lòng chọn sự kiện", Toast.LENGTH_SHORT).show(); return;
        }
        String valStr = etValue.getText().toString().trim();
        if (TextUtils.isEmpty(valStr)) { etValue.setError("Nhập giá trị giảm"); return; }
        double value = Double.parseDouble(valStr);
        String typeStr = actvDiscountType.getText().toString();
        String discountType = typeStr.contains("Phần trăm") ? "percentage" : "fixed";
        if (discountType.equals("percentage") && value > 100) { etValue.setError("Phần trăm không được quá 100%"); return; }
        if (startCalendar.after(endCalendar)) { Toast.makeText(this, "Ngày kết thúc lỗi", Toast.LENGTH_SHORT).show(); return; }

        // Lấy các giá trị Optional
        double minPurchase = 0;
        if (!TextUtils.isEmpty(etMinPurchase.getText())) minPurchase = Double.parseDouble(etMinPurchase.getText().toString());
        double maxDiscount = 0;
        if (!TextUtils.isEmpty(etMaxDiscount.getText())) maxDiscount = Double.parseDouble(etMaxDiscount.getText().toString());
        int usageLimit = 0;
        if (!TextUtils.isEmpty(etUsageLimit.getText())) usageLimit = Integer.parseInt(etUsageLimit.getText().toString());

        String startDateStr = dbFormat.format(startCalendar.getTime());
        String endDateStr = dbFormat.format(endCalendar.getTime());

        currentDiscount.setEventId(selectedEventId);
        currentDiscount.setDiscountType(discountType);
        currentDiscount.setDiscountValue(value);
        currentDiscount.setMinPurchase(minPurchase);
        currentDiscount.setMaxDiscount(maxDiscount);
        currentDiscount.setUsageLimit(usageLimit);
        currentDiscount.setStartDate(startDateStr);
        currentDiscount.setEndDate(endDateStr);
        currentDiscount.setActive(switchActive.isChecked());

        //GỌI VIEWMODEL UPDATE
        discountViewModel.updateDiscount(currentDiscount, new DiscountRepository.OnActionCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(UpdateVoucherActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(UpdateVoucherActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupDateTimePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(startCalendar, etStartDate));
        etStartTime.setOnClickListener(v -> showTimePicker(startCalendar, etStartTime));
        etEndDate.setOnClickListener(v -> showDatePicker(endCalendar, etEndDate));
        etEndTime.setOnClickListener(v -> showTimePicker(endCalendar, etEndTime));
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
}