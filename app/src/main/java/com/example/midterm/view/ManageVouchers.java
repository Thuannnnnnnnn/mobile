package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.repository.DiscountRepository;
import com.example.midterm.view.Adapter.DiscountAdapter;
import com.example.midterm.viewModel.DiscountViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class ManageVouchers extends AppCompatActivity {
    private AutoCompleteTextView actvEventFilter;
    private RecyclerView rvVouchers;
    private TextView tvEmpty;
    private DiscountAdapter adapter;
    private DiscountViewModel discountViewModel;
    private List<Event> eventList = new ArrayList<>();
    private int selectedEventId = -1; // -1 means All or None

    private LiveData<List<Discount>> currentDiscountsLiveData;

    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_vouchers);

        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        initViews();

        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            finish();
            return;
        }

        loadEventsToDropdown();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // Refresh list when coming back from Create Screen
    @Override
    protected void onResume() {
        super.onResume();
        if (selectedEventId != -1) {
            loadVouchers(selectedEventId);
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        actvEventFilter = findViewById(R.id.actv_event_filter);
        rvVouchers = findViewById(R.id.rv_vouchers);
        tvEmpty = findViewById(R.id.tv_empty_voucher);
        FloatingActionButton fab = findViewById(R.id.fab_add_voucher);

        // Cần import LinearLayoutManager
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiscountAdapter(new ArrayList<>(), this::showOptionsDialog);
        rvVouchers.setAdapter(adapter);

        // Nút thêm mới
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateVoucher.class); // Đảm bảo tên class đúng (CreateVoucher hay CreateDiscountActivity)
            if (selectedEventId != -1) {
                intent.putExtra("pre_selected_event_id", selectedEventId);
            }
            startActivity(intent);
        });

        // Xử lý khi chọn Event từ Dropdown
        actvEventFilter.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventList.get(position);
            selectedEventId = selectedEvent.getId();
            loadVouchers(selectedEventId);
        });
    }

    private void showOptionsDialog(Discount discount) {
        String[] options = {"Cập nhật", "Xóa"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thao tác cho mã: " + discount.getCode());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Chọn Cập nhật
                handleUpdateVoucher(discount);
            } else if (which == 1) {
                // Chọn Xóa -> Hiện tiếp dialog xác nhận
                showDeleteConfirmDialog(discount);
            }
        });
        builder.show();
    }
    private void showDeleteConfirmDialog(Discount discount) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa voucher " + discount.getCode() + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteVoucher(discount);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void deleteVoucher(Discount discount) {
        discountViewModel.deleteDiscount(discount, new DiscountRepository.OnActionCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() ->
                        Toast.makeText(ManageVouchers.this, "Đã xóa voucher thành công", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(ManageVouchers.this, "Lỗi khi xóa: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    private void handleUpdateVoucher(Discount discount) {
        Intent intent = new Intent(this, UpdateVoucherActivity.class);
        intent.putExtra("discount_item", discount);
        startActivity(intent);
    }
    private void loadEventsToDropdown() {

        discountViewModel.getAllEvents(userId).observe(this, events -> {
            eventList = events;
            setupDropdownData(events);
        });
    }

    private void setupDropdownData(List<Event> events) {
        List<String> eventNames = new ArrayList<>();
        for (Event e : events) {
            eventNames.add(e.getEventName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                eventNames
        );
        actvEventFilter.setAdapter(adapter);

        // Mặc định chọn event đầu tiên
        if (!events.isEmpty() && selectedEventId == -1) {
            actvEventFilter.setText(events.get(0).getEventName(), false);
            selectedEventId = events.get(0).getId();
            loadVouchers(selectedEventId);
        } else if (selectedEventId != -1) {
            // Logic để set lại text nếu quay lại màn hình (Optional)
        }
    }

    private void loadVouchers(int eventId) {
        if (currentDiscountsLiveData != null) {
            currentDiscountsLiveData.removeObservers(this);
        }

        // Gọi ViewModel để lấy LiveData
        currentDiscountsLiveData = discountViewModel.getDiscountsByEvent(eventId);

        // Quan sát dữ liệu
        currentDiscountsLiveData.observe(this, vouchers -> {
            if (vouchers == null || vouchers.isEmpty()) {
                rvVouchers.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                rvVouchers.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                // Cập nhật Adapter
                adapter.setDiscounts(vouchers);
            }
        });
    }
}