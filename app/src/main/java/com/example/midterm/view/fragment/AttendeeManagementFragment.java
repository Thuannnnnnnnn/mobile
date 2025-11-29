package com.example.midterm.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.dto.TicketWithDetails;
import com.example.midterm.model.entity.Event;
import com.example.midterm.utils.ExportUtils;
import com.example.midterm.view.Adapter.AttendeeAdapter;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.TicketViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendeeManagementFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";
    private int eventId;

    private TextInputEditText etSearch;
    private ImageButton btnExport;
    private RecyclerView rvAttendees;

    private AttendeeAdapter attendeeAdapter;
    private TicketViewModel ticketViewModel;
    private EventViewModel eventViewModel;
    private Event currentEvent;

    private ExecutorService executorService;

    public static AttendeeManagementFragment newInstance(int eventId) {
        AttendeeManagementFragment fragment = new AttendeeManagementFragment();
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
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_attendees, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        setupRecyclerView();
        setupSearch();
        loadData();
        setupListeners();
    }

    private void initViews(View view) {
        com.google.android.material.textfield.TextInputLayout inputLayout = view.findViewById(R.id.input_search);
        etSearch = (TextInputEditText) inputLayout.getEditText();
        btnExport = view.findViewById(R.id.btn_export);
        rvAttendees = view.findViewById(R.id.rv_attendees);
    }

    private void initViewModels() {
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        eventViewModel.getEventById(eventId).observe(getViewLifecycleOwner(), event -> {
            currentEvent = event;
        });
    }

    private void setupRecyclerView() {
        attendeeAdapter = new AttendeeAdapter();
        rvAttendees.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAttendees.setAdapter(attendeeAdapter);

        attendeeAdapter.setOnAttendeeClickListener(attendee -> {
            Toast.makeText(getContext(),
                "Khách: " + attendee.getBuyerName() + "\nMã vé: " + attendee.getQrCode(),
                Toast.LENGTH_LONG).show();
        });
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (attendeeAdapter != null) {
                        attendeeAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadData() {
        ticketViewModel.getTicketsWithDetailsByEvent(eventId).observe(getViewLifecycleOwner(), tickets -> {
            attendeeAdapter.setAttendees(tickets);
        });
    }

    private void setupListeners() {
        btnExport.setOnClickListener(v -> showExportDialog());
    }

    private void showExportDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_export_options, null);
        dialog.setContentView(dialogView);

        MaterialButton btnExportExcel = dialogView.findViewById(R.id.btn_export_excel);
        MaterialButton btnExportPDF = dialogView.findViewById(R.id.btn_export_pdf);

        btnExportExcel.setOnClickListener(v -> {
            exportToExcel();
            dialog.dismiss();
        });

        btnExportPDF.setOnClickListener(v -> {
            exportToPDF();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void exportToExcel() {
        if (attendeeAdapter.getAttendees().isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            List<ExportUtils.AttendeeData> attendeeDataList = convertToAttendeeData(attendeeAdapter.getAttendees());
            String eventName = currentEvent != null ? currentEvent.getEventName() : "Event";
            String filePath = ExportUtils.exportAttendeesToExcel(requireContext(), eventName, attendeeDataList);

            requireActivity().runOnUiThread(() -> {
                if (filePath != null) {
                    Toast.makeText(getContext(), "Đã xuất file Excel thành công", Toast.LENGTH_SHORT).show();
                    openFile(filePath);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi xuất file", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void exportToPDF() {
        if (attendeeAdapter.getAttendees().isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            List<ExportUtils.AttendeeData> attendeeDataList = convertToAttendeeData(attendeeAdapter.getAttendees());
            String eventName = currentEvent != null ? currentEvent.getEventName() : "Event";
            String filePath = ExportUtils.exportAttendeesToPDF(requireContext(), eventName, attendeeDataList);

            requireActivity().runOnUiThread(() -> {
                if (filePath != null) {
                    Toast.makeText(getContext(), "Đã xuất file PDF thành công", Toast.LENGTH_SHORT).show();
                    openFile(filePath);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi xuất file", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private List<ExportUtils.AttendeeData> convertToAttendeeData(List<TicketWithDetails> tickets) {
        List<ExportUtils.AttendeeData> attendeeDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (TicketWithDetails ticket : tickets) {
            String purchaseDate = formatDate(ticket.getPurchaseDate(), dateFormat);
            String status = getStatusText(ticket.getStatus());

            ExportUtils.AttendeeData data = new ExportUtils.AttendeeData(
                    ticket.getBuyerName() != null ? ticket.getBuyerName() : "N/A",
                    ticket.getBuyerEmail() != null ? ticket.getBuyerEmail() : "N/A",
                    "", // Phone not available in TicketWithDetails
                    ticket.getTicketTypeCode() != null ? ticket.getTicketTypeCode() : "N/A",
                    ticket.getQrCode() != null ? ticket.getQrCode() : "N/A",
                    purchaseDate,
                    status
            );
            attendeeDataList.add(data);
        }

        return attendeeDataList;
    }

    private String formatDate(String dateString, SimpleDateFormat outputFormat) {
        if (dateString == null || dateString.isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : dateString;
        } catch (Exception e) {
            return dateString;
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "Đã đặt";

        switch (status) {
            case "checked_in":
                return "Đã check-in";
            case "cancelled":
                return "Đã hủy";
            case "booked":
            default:
                return "Đã đặt";
        }
    }

    private void openFile(String filePath) {
        try {
            File file = new File(filePath);
            Uri fileUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (filePath.endsWith(".xlsx")) {
                intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else if (filePath.endsWith(".pdf")) {
                intent.setDataAndType(fileUri, "application/pdf");
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Mở file"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Không thể mở file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
