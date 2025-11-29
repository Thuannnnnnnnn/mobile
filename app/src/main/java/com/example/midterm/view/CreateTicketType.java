package com.example.midterm.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.view.Adapter.TicketTypeAdapter;
import com.example.midterm.viewModel.EventSectionViewModel;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.GuestViewModel;
import com.example.midterm.viewModel.TicketTypeViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateTicketType extends AppCompatActivity implements TicketTypeAdapter.OnTicketTypeActionListener {

    private TextInputEditText inputTicketType, inputTicketPrice, inputTicketQuantity, inputDescription,
            inputSaleDateStart, inputSaleTimeStart, inputSaleDateEnd, inputSaleTimeEnd;
    private TextView tvErrorTicketType, tvErrorTicketPrice, tvErrorTicketQuantity;
    private Button btnFinishCreateTicketType, btnSaveAndAddNewTicketType;
    private ImageButton btnBack;
    private TicketTypeViewModel ticketTypeViewModel;
    private EventViewModel eventViewModel;
    private EventSectionViewModel eventSectionViewModel;
    private GuestViewModel guestViewModel;
    private RecyclerView rvTicketTypesList;
    private TicketTypeAdapter ticketTypeAdapter;
    private long roomId;
    private View rootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_ticket);

        initViews();
        pickerTimeAndDate();

        roomId = getIntent().getLongExtra("room_id", -1);
        if (roomId == -1) {
            return;
        }
        ticketTypeViewModel = new ViewModelProvider(this).get(TicketTypeViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        guestViewModel = new ViewModelProvider(this).get(GuestViewModel.class);

        ticketTypeAdapter = new TicketTypeAdapter(this, this);
        rvTicketTypesList.setLayoutManager(new LinearLayoutManager(this));
        rvTicketTypesList.setAdapter(ticketTypeAdapter);

        ticketTypeViewModel.getTicketsByEventId((int) roomId).observe(this, ticketTypes -> {

            ticketTypeAdapter.setTicketTypes(ticketTypes);

            TextView tvEmpty = findViewById(R.id.tv_ticket_list_empty);
            if (ticketTypes != null && ticketTypes.isEmpty()) {
                rvTicketTypesList.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                rvTicketTypesList.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            }
        });

        btnSaveAndAddNewTicketType.setOnClickListener(v -> handleAddTicket());
        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());

        btnFinishCreateTicketType.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateSeatMap.class);
            intent.putExtra("room_id", roomId);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // Ghi đè nút back điện thoại
    @Override
    public void onBackPressed() {
        showCancelConfirmationDialog();
    }
    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy tạo sự kiện?")
                .setMessage("Nếu bạn quay lại, sự kiện, khách mời, và các vé đã thêm sẽ bị xóa. Bạn có chắc chắn muốn hủy không?")
                .setPositiveButton("Xóa & Hủy", (dialog, which) -> {
                    deleteEventAndFinish();
                })
                .setNegativeButton("Tiếp tục", null)
                .setIcon(R.drawable.warning)
                .show();
    }

    private void deleteEventAndFinish() {

        ticketTypeViewModel.deleteTicketsByEventId(roomId);
        guestViewModel.deleteGuestsAndCrossRefsByEventId(roomId);
        eventViewModel.deleteEventById(roomId);
        eventSectionViewModel.deleteSectionsByEventId(roomId);

        showSnackbar("Đã hủy tạo sự kiện.", true);

        // Quay về Homepage
        Intent intent = new Intent(CreateTicketType.this, HomepageOrganizer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void handleAddTicket() {
        String type = inputTicketType.getText().toString().trim();
        String priceStr = inputTicketPrice.getText().toString().trim();
        String quantityStr = inputTicketQuantity.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(type)) {
            tvErrorTicketType.setText("Loại vé không được để trống.");
            tvErrorTicketType.setVisibility(View.VISIBLE);
            isValid = false;
        } else tvErrorTicketType.setVisibility(View.GONE);

        double price = 0;
        if (TextUtils.isEmpty(priceStr)) {
            tvErrorTicketPrice.setText("Giá vé không được để trống.");
            tvErrorTicketPrice.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                    tvErrorTicketPrice.setText("Giá vé không hợp lệ.");
                    throw new NumberFormatException();
                }
                tvErrorTicketPrice.setVisibility(View.GONE);
            } catch (NumberFormatException e) {
                tvErrorTicketPrice.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        int quantity = 0;
        if (TextUtils.isEmpty(quantityStr)) {
            tvErrorTicketQuantity.setText("Số lượng vé không được để trống.");
            tvErrorTicketQuantity.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) throw new NumberFormatException();
                tvErrorTicketQuantity.setVisibility(View.GONE);
            } catch (NumberFormatException e) {
                tvErrorTicketQuantity.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        if (!isValid) return;

        String openDate = inputSaleDateStart.getText() + " " + inputSaleTimeStart.getText();
        String closeDate = inputSaleDateEnd.getText() + " " + inputSaleTimeEnd.getText();

        TicketType ticket = new TicketType((int) roomId, type, price, quantity, 0,
                description, openDate, closeDate, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime())
        );

        ticketTypeViewModel.insertTicket(ticket);
        showSnackbar("Thêm vé thành công!", false);
        clearInputs();
    }
    @Override
    public void onDeleteClick(TicketType ticketType) {
        showDeleteConfirmationDialog(ticketType); // Gọi Dialog xác nhận trước khi xóa
    }
    private void showDeleteConfirmationDialog(TicketType ticketType) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa vé")
                .setMessage("Bạn có chắc chắn muốn xóa loại vé \"" + ticketType.getCode() + "\" này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi ViewModel để xóa đối tượng TicketType
                    ticketTypeViewModel.deleteTicket(ticketType);
                    showSnackbar("Đã xóa loại vé: " + ticketType.getCode(), false);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void clearInputs(){
        inputTicketType.setText("");
        inputTicketPrice.setText("");
        inputTicketQuantity.setText("");
        inputDescription.setText("");
        inputSaleDateStart.setText("");
        inputSaleTimeStart.setText("");
        inputSaleDateEnd.setText("");
        inputSaleTimeEnd.setText("");

        tvErrorTicketType.setVisibility(View.GONE);
        tvErrorTicketPrice.setVisibility(View.GONE);
        tvErrorTicketQuantity.setVisibility(View.GONE);

        inputTicketType.requestFocus();
    }
    private void initViews(){
        rootView = findViewById(R.id.main);
        inputTicketType = findViewById(R.id.edit_ticket_type);
        inputTicketPrice = findViewById(R.id.edit_ticket_price);
        inputTicketQuantity = findViewById(R.id.edit_quantity);
        inputSaleDateStart = findViewById(R.id.et_sale_date_start);
        inputSaleTimeStart = findViewById(R.id.et_sale_time_start);
        inputSaleDateEnd = findViewById(R.id.et_sale_date_end);
        inputSaleTimeEnd = findViewById(R.id.et_sale_time_end);
        inputDescription = findViewById(R.id.edit_ticketType_description);

        tvErrorTicketType = findViewById(R.id.tvErrorTicketType);
        tvErrorTicketPrice = findViewById(R.id.tvErrorTicketPrice);
        tvErrorTicketQuantity = findViewById(R.id.tvErrorQuantity);

        btnSaveAndAddNewTicketType = findViewById(R.id.btn_save_and_add_new);
        btnFinishCreateTicketType = findViewById(R.id.btn_finish_ticket_setup);
        btnBack = findViewById(R.id.btn_back_dialog);

        rvTicketTypesList = findViewById(R.id.rv_ticket_types_list);
    }
    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        int color = isError ? ContextCompat.getColor(this, R.color.colorError) : ContextCompat.getColor(this, R.color.colorSuccess);
        snackbarView.setBackgroundTintList(ColorStateList.valueOf(color));
        snackbar.show();
    }
    private void pickerTimeAndDate(){
        inputSaleDateStart.setOnClickListener(v -> showDatePicker(inputSaleDateStart));
        inputSaleDateEnd.setOnClickListener(v -> showDatePicker(inputSaleDateEnd));
        inputSaleTimeStart.setOnClickListener(v -> showTimePicker(inputSaleTimeStart));
        inputSaleTimeEnd.setOnClickListener(v -> showTimePicker(inputSaleTimeEnd));
    }
    private void showDatePicker(TextInputEditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            targetEditText.setText(formatted);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showTimePicker(TextInputEditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            targetEditText.setText(formatted);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
}
