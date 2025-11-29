package com.example.midterm.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.EventSection;
import com.example.midterm.view.Adapter.EventSectionAdapter;
import com.example.midterm.viewModel.EventSectionViewModel;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.GuestViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;


public class CreateEventSection extends AppCompatActivity implements EventSectionAdapter.OnSectionActionListener {
    private ImageButton btnBack;
    private Button btnSaveAndAddNew, btnFinishSetup;
    private TextInputLayout inputLayoutName, inputLayoutCapacity, inputLayoutRows, inputLayoutCols, inputLayoutOrder;
    private EditText editName, editCapacity, editRows, editCols, editOrder;
    private RadioGroup rgSectionType;
    private RadioButton rbSeated;
    private LinearLayout layoutSeatedDetails;
    private RecyclerView rvSectionsList;
    private TextView tvEmptySectionList;
    private View rootView;

    // ViewModels
    private EventSectionViewModel eventSectionViewModel;
    private EventViewModel eventViewModel;
    private GuestViewModel guestViewModel;

    // Adapter
    private EventSectionAdapter adapter;
    // Data
    private long currentEventId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event_section); // Đảm bảo tên layout đúng

        initViews();

        currentEventId = getIntent().getLongExtra("room_id", -1L);
        if (currentEventId == -1L) {
            finish();
            return;
        }

        initViewModels();
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        rootView = findViewById(R.id.main);

        btnBack = findViewById(R.id.btn_back);
        btnSaveAndAddNew = findViewById(R.id.btn_save_and_add_new_section);
        btnFinishSetup = findViewById(R.id.btn_finish_section_setup);

        inputLayoutName = findViewById(R.id.input_section_name);
        inputLayoutCapacity = findViewById(R.id.input_section_capacity);
        inputLayoutRows = findViewById(R.id.input_section_rows);
        inputLayoutCols = findViewById(R.id.input_section_cols);
        inputLayoutOrder = findViewById(R.id.input_display_order);

        editName = findViewById(R.id.edit_section_name);
        editCapacity = findViewById(R.id.edit_section_capacity);
        editRows = findViewById(R.id.edit_section_rows);
        editCols = findViewById(R.id.edit_section_cols);
        editOrder = findViewById(R.id.edit_display_order);

        rgSectionType = findViewById(R.id.rg_section_type);
        rbSeated = findViewById(R.id.rb_seated);
        layoutSeatedDetails = findViewById(R.id.layout_seated_details);

        // RecyclerView
        rvSectionsList = findViewById(R.id.rv_sections_list);
        tvEmptySectionList = findViewById(R.id.tv_empty_section_list);
    }

    private void initViewModels() {
        eventSectionViewModel = new ViewModelProvider(this).get(EventSectionViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        guestViewModel = new ViewModelProvider(this).get(GuestViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new EventSectionAdapter(this, this);
        rvSectionsList.setLayoutManager(new LinearLayoutManager(this));
        rvSectionsList.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());  // Nút Back/Hủy
        btnSaveAndAddNew.setOnClickListener(v -> validateAndSaveSection());  // Nút Thêm Mới

        // Nút Hoàn tất
        btnFinishSetup.setOnClickListener(v -> {
            //Chuyển sang bước tạo Loại Vé
            Intent intent = new Intent(this, CreateTicketType.class);
            intent.putExtra("room_id", currentEventId);
            startActivity(intent);
        });

        // Radio Group: Ẩn/hiện ô nhập Hàng/Cột
        rgSectionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_seated) {
                layoutSeatedDetails.setVisibility(View.VISIBLE);
            } else {
                layoutSeatedDetails.setVisibility(View.GONE);
            }
        });
    }

    //Lắng nghe LiveData từ ViewModel
    private void observeViewModel() {
        eventSectionViewModel.getSectionsByEventId(currentEventId).observe(this, sections -> {
            adapter.setSections(sections);
            // Xử lý trạng thái rỗng
            if (sections == null || sections.isEmpty()) {
                rvSectionsList.setVisibility(View.GONE);
                tvEmptySectionList.setVisibility(View.VISIBLE);
            } else {
                rvSectionsList.setVisibility(View.VISIBLE);
                tvEmptySectionList.setVisibility(View.GONE);
            }
        });
    }

    //Kiểm tra và lưu khu vực mới
    private void validateAndSaveSection() {
        // Xóa lỗi cũ
        inputLayoutName.setError(null);
        inputLayoutCapacity.setError(null);
        inputLayoutOrder.setError(null);
        inputLayoutRows.setError(null);
        inputLayoutCols.setError(null);

        // Lấy dữ liệu
        String name = editName.getText().toString().trim();
        String capacityStr = editCapacity.getText().toString().trim();
        String orderStr = editOrder.getText().toString().trim();
        String rowsStr = editRows.getText().toString().trim();
        String colsStr = editCols.getText().toString().trim();

        boolean isSeated = rbSeated.isChecked();
        String sectionType = isSeated ? "seated" : "standing";

        boolean hasError = false;
        int capacity = 0;
        int displayOrder = 0;
        Integer rows = null; // Dùng Integer để cho phép null
        Integer cols = null;

        // Validate
        if (TextUtils.isEmpty(name)) {
            inputLayoutName.setError("Vui lòng nhập tên khu vực");
            hasError = true;
        }
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            inputLayoutCapacity.setError("Sức chứa phải là số > 0");
            hasError = true;
        }
        try {
            displayOrder = Integer.parseInt(orderStr);
        } catch (NumberFormatException e) {
            inputLayoutOrder.setError("Vui lòng nhập thứ tự");
            hasError = true;
        }
        if (isSeated) {
            try {
                rows = Integer.parseInt(rowsStr);
                if (rows <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                inputLayoutRows.setError("Số hàng phải > 0");
                hasError = true;
            }
            try {
                cols = Integer.parseInt(colsStr);
                if (cols <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                inputLayoutCols.setError("Số cột phải > 0");
                hasError = true;
            }
        }

        if (hasError) return;
        EventSection section = new EventSection((int) currentEventId, name, sectionType, capacity, rows, cols, displayOrder);
        eventSectionViewModel.insert(section);

        showSnackbar("Đã thêm khu vực: " + name, false);
        clearInputs();
    }

    private void clearInputs() {
        editName.setText("");
        editCapacity.setText("");
        editRows.setText("");
        editCols.setText("");
        editOrder.setText("");
        rbSeated.setChecked(true); // Đặt lại về mặc định
        editName.requestFocus();
    }
    //Ghi đè nút back vật lý
    @Override
    public void onBackPressed() {
        showCancelConfirmationDialog();
    }
    //Hiển thị cảnh báo Hủy
    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy tạo sự kiện?")
                .setMessage("Nếu bạn quay lại, sự kiện, khách mời, và các khu vực đã thêm sẽ bị xóa. Bạn có chắc chắn muốn hủy không?")
                .setPositiveButton("Xóa & Hủy", (dialog, which) -> {
                    deleteEventAndFinish();
                })
                .setNegativeButton("Tiếp tục", null)
                .setIcon(R.drawable.warning)
                .show();
    }
    //Xóa tất cả dữ liệu đã tạo và quay về Homepage
    private void deleteEventAndFinish() {
        // Xóa tất cả (Thứ tự: Con -> Cha)
        eventSectionViewModel.deleteSectionsByEventId(currentEventId);
        guestViewModel.deleteGuestsAndCrossRefsByEventId(currentEventId);
        eventViewModel.deleteEventById(currentEventId);

        showSnackbar("Đã hủy tạo sự kiện.", true);

        // Quay về Homepage
        Intent intent = new Intent(this, HomepageOrganizer.class); // Thay bằng Homepage của bạn
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

    //Sự kiện click Xóa từ Adapter
    @Override
    public void onDeleteClick(EventSection section) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khu vực")
                .setMessage("Bạn có chắc muốn xóa khu vực \"" + section.name + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    eventSectionViewModel.delete(section);
                    showSnackbar("Đã xóa khu vực: " + section.name, false);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}