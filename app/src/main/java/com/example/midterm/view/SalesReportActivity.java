package com.example.midterm.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.utils.ExportUtils;
import com.example.midterm.view.Adapter.SalesReportAdapter;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.TicketTypeViewModel;
import com.example.midterm.viewModel.TicketViewModel;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SalesReportActivity extends AppCompatActivity {

    private TextView tvEventName, tvTotalRevenue, tvTotalSold, tvTotalCapacity, tvFillRate;
    private TextView tvCheckedIn, tvPending, tvCancelled;
    private RecyclerView rvTicketTypes;
    private Button btnExportExcel, btnExportPDF;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private MaterialCardView cardSummary;

    private EventViewModel eventViewModel;
    private TicketTypeViewModel ticketTypeViewModel;
    private TicketViewModel ticketViewModel;

    private SalesReportAdapter adapter;
    private int eventId;
    private String eventName;

    private List<TicketType> ticketTypes = new ArrayList<>();
    private double totalRevenue = 0;
    private int totalSold = 0;
    private int totalCapacity = 0;
    private int checkedIn = 0;
    private int pending = 0;
    private int cancelled = 0;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sales_report);

        initViews();
        setupRecyclerView();

        eventId = getIntent().getIntExtra("event_id", -1);
        eventName = getIntent().getStringExtra("event_name");

        if (eventId == -1) {
            finish();
            return;
        }

        tvEventName.setText(eventName != null ? eventName : "Sự kiện");

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        ticketTypeViewModel = new ViewModelProvider(this).get(TicketTypeViewModel.class);
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);

        executorService = Executors.newSingleThreadExecutor();

        loadSalesData();

        btnExportExcel.setOnClickListener(v -> exportToExcel());
        btnExportPDF.setOnClickListener(v -> exportToPDF());
        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tv_event_name);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalSold = findViewById(R.id.tv_total_sold);
        tvTotalCapacity = findViewById(R.id.tv_total_capacity);
        tvFillRate = findViewById(R.id.tv_fill_rate);
        tvCheckedIn = findViewById(R.id.tv_checked_in);
        tvPending = findViewById(R.id.tv_pending);
        tvCancelled = findViewById(R.id.tv_cancelled);
        rvTicketTypes = findViewById(R.id.rv_ticket_types);
        btnExportExcel = findViewById(R.id.btn_export_excel);
        btnExportPDF = findViewById(R.id.btn_export_pdf);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);
        cardSummary = findViewById(R.id.card_summary);
    }

    private void setupRecyclerView() {
        adapter = new SalesReportAdapter(this, new ArrayList<>());
        rvTicketTypes.setLayoutManager(new LinearLayoutManager(this));
        rvTicketTypes.setAdapter(adapter);
    }

    private void loadSalesData() {
        progressBar.setVisibility(View.VISIBLE);

        // Load ticket types for this event
        ticketTypeViewModel.getTicketsByEventId(eventId).observe(this, types -> {
            if (types != null) {
                ticketTypes = types;
                adapter.updateData(types);
                calculateTotals(types);
            }
            progressBar.setVisibility(View.GONE);
        });

        // Load ticket statistics
        eventViewModel.getTotalTicketsSoldForEvent(eventId).observe(this, sold -> {
            if (sold != null) {
                totalSold = sold;
                tvTotalSold.setText(String.valueOf(sold));
                updateFillRate();
            }
        });

        eventViewModel.getTotalRevenueForEvent(eventId).observe(this, revenue -> {
            if (revenue != null) {
                totalRevenue = revenue;
                tvTotalRevenue.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", revenue));
            }
        });

        eventViewModel.getTotalCapacityForEvent(eventId).observe(this, capacity -> {
            if (capacity != null) {
                totalCapacity = capacity;
                tvTotalCapacity.setText(String.valueOf(capacity));
                updateFillRate();
            }
        });

        // Load check-in statistics
        ticketViewModel.countCheckedInForEvent(eventId).observe(this, count -> {
            if (count != null) {
                checkedIn = count;
                tvCheckedIn.setText(String.valueOf(count));
            }
        });

        ticketViewModel.countTicketsSoldForEvent(eventId).observe(this, count -> {
            if (count != null) {
                pending = count - checkedIn;
                tvPending.setText(String.valueOf(Math.max(0, pending)));
            }
        });

        ticketViewModel.countCancelledForEvent(eventId).observe(this, count -> {
            if (count != null) {
                cancelled = count;
                tvCancelled.setText(String.valueOf(count));
            }
        });
    }

    private void calculateTotals(List<TicketType> types) {
        int capacity = 0;
        for (TicketType type : types) {
            capacity += type.getQuantity();
        }
        totalCapacity = capacity;
        tvTotalCapacity.setText(String.valueOf(capacity));
        updateFillRate();
    }

    private void updateFillRate() {
        if (totalCapacity > 0) {
            double fillRate = (totalSold * 100.0) / totalCapacity;
            tvFillRate.setText(String.format(Locale.getDefault(), "%.1f%%", fillRate));
        } else {
            tvFillRate.setText("0%");
        }
    }

    private void exportToExcel() {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            List<ExportUtils.SalesData> salesDataList = new ArrayList<>();
            for (TicketType type : ticketTypes) {
                salesDataList.add(new ExportUtils.SalesData(
                        type.getCode(),
                        type.getPrice(),
                        type.getSoldQuantity(),
                        type.getQuantity()
                ));
            }

            ExportUtils.SalesSummary summary = new ExportUtils.SalesSummary(
                    totalSold, totalRevenue, checkedIn, pending, cancelled, totalCapacity
            );

            String filePath = ExportUtils.exportSalesReportToExcel(this, eventName, salesDataList, summary);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (filePath != null) {
                    shareFile(filePath);
                    Toast.makeText(this, "Đã xuất báo cáo Excel", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void exportToPDF() {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            List<ExportUtils.SalesData> salesDataList = new ArrayList<>();
            for (TicketType type : ticketTypes) {
                salesDataList.add(new ExportUtils.SalesData(
                        type.getCode(),
                        type.getPrice(),
                        type.getSoldQuantity(),
                        type.getQuantity()
                ));
            }

            ExportUtils.SalesSummary summary = new ExportUtils.SalesSummary(
                    totalSold, totalRevenue, checkedIn, pending, cancelled, totalCapacity
            );

            String filePath = ExportUtils.exportSalesReportToPDF(this, eventName, salesDataList, summary);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (filePath != null) {
                    shareFile(filePath, "application/pdf");
                    Toast.makeText(this, "Đã xuất báo cáo PDF", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void shareFile(String filePath) {
        shareFile(filePath, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private void shareFile(String filePath, String mimeType) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ báo cáo"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
