package com.example.midterm.view.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTicketTypeDialog extends Dialog {
    private TextInputEditText etTicketName, etPrice, etQuantity, etDescription;
    private TextInputEditText etOpenDate, etCloseDate;
    private Button btnSave, btnCancel;
    private OnTicketTypeSavedListener listener;
    private int eventId;
    private TicketType editingTicketType;
    private Calendar selectedOpenDate;
    private Calendar selectedCloseDate;

    public interface OnTicketTypeSavedListener {
        void onTicketTypeSaved(TicketType ticketType);
    }

    public AddTicketTypeDialog(@NonNull Context context, int eventId, OnTicketTypeSavedListener listener) {
        super(context);
        this.eventId = eventId;
        this.listener = listener;
        this.selectedOpenDate = Calendar.getInstance();
        this.selectedCloseDate = Calendar.getInstance();
    }

    public AddTicketTypeDialog(@NonNull Context context, int eventId, TicketType ticketType, OnTicketTypeSavedListener listener) {
        this(context, eventId, listener);
        this.editingTicketType = ticketType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_ticket_type, null);
        setContentView(view);

        etTicketName = view.findViewById(R.id.et_ticket_name);
        etPrice = view.findViewById(R.id.et_price);
        etQuantity = view.findViewById(R.id.et_quantity);
        etDescription = view.findViewById(R.id.et_description);
        etOpenDate = view.findViewById(R.id.et_open_date);
        etCloseDate = view.findViewById(R.id.et_close_date);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        if (editingTicketType != null) {
            setTitle("Chỉnh sửa loại vé");
            fillData(editingTicketType);
        } else {
            setTitle("Thêm loại vé mới");
        }

        setupDatePickers();
        setupButtons();
    }

    private void fillData(TicketType ticketType) {
        etTicketName.setText(ticketType.getCode());
        etPrice.setText(String.valueOf(ticketType.getPrice()));
        etQuantity.setText(String.valueOf(ticketType.getQuantity()));
        etDescription.setText(ticketType.getDescription());
        etOpenDate.setText(ticketType.getOpenTicketDate());
        etCloseDate.setText(ticketType.getCloseTicketDate());
    }

    private void setupDatePickers() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        etOpenDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedOpenDate.set(year, month, dayOfMonth);
                        etOpenDate.setText(dateFormat.format(selectedOpenDate.getTime()));
                    },
                    selectedOpenDate.get(Calendar.YEAR),
                    selectedOpenDate.get(Calendar.MONTH),
                    selectedOpenDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        etCloseDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedCloseDate.set(year, month, dayOfMonth);
                        etCloseDate.setText(dateFormat.format(selectedCloseDate.getTime()));
                    },
                    selectedCloseDate.get(Calendar.YEAR),
                    selectedCloseDate.get(Calendar.MONTH),
                    selectedCloseDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                TicketType ticketType = createTicketTypeFromInputs();
                if (listener != null) {
                    listener.onTicketTypeSaved(ticketType);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private boolean validateInputs() {
        String name = etTicketName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên loại vé", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập giá vé", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số lượng vé", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                Toast.makeText(getContext(), "Giá vé phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá vé không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), "Số lượng vé phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số lượng vé không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private TicketType createTicketTypeFromInputs() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        TicketType ticketType = editingTicketType != null ? editingTicketType : new TicketType();

        ticketType.setEventID(eventId);
        ticketType.setCode(etTicketName.getText().toString().trim());
        ticketType.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        ticketType.setQuantity(Integer.parseInt(etQuantity.getText().toString().trim()));
        ticketType.setDescription(etDescription.getText().toString().trim());
        ticketType.setOpenTicketDate(etOpenDate.getText().toString().trim());
        ticketType.setCloseTicketDate(etCloseDate.getText().toString().trim());

        if (editingTicketType == null) {
            ticketType.setSoldQuantity(0);
            ticketType.setCreatedAt(dateFormat.format(Calendar.getInstance().getTime()));
        }

        return ticketType;
    }
}
