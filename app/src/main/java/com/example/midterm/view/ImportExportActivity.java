package com.example.midterm.view;

import android.content.Intent;
import android.graphics.Color;
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

import com.example.midterm.R;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.Ticket;
import com.example.midterm.utils.ExportUtils;
import com.example.midterm.utils.OTPUtils;
import com.example.midterm.viewModel.GuestViewModel;
import com.example.midterm.viewModel.TicketTypeViewModel;
import com.example.midterm.viewModel.TicketViewModel;
import com.google.android.material.snackbar.Snackbar;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportExportActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1001;

    private TextView tvEventName, tvImportStatus, tvExportStatus;
    private Button btnImport, btnExportExcel, btnExportPDF, btnDownloadTemplate;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private GuestViewModel guestViewModel;
    private TicketTypeViewModel ticketTypeViewModel;
    private TicketViewModel ticketViewModel;

    private int eventId;
    private String eventName;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_import_export);

        initViews();

        eventId = getIntent().getIntExtra("event_id", -1);
        eventName = getIntent().getStringExtra("event_name");

        if (eventId == -1) {
            finish();
            return;
        }

        tvEventName.setText(eventName != null ? eventName : "Sự kiện");

        guestViewModel = new ViewModelProvider(this).get(GuestViewModel.class);
        ticketTypeViewModel = new ViewModelProvider(this).get(TicketTypeViewModel.class);
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);

        executorService = Executors.newSingleThreadExecutor();

        btnImport.setOnClickListener(v -> selectFileToImport());
        btnExportExcel.setOnClickListener(v -> exportToExcel());
        btnExportPDF.setOnClickListener(v -> exportToPDF());
        btnDownloadTemplate.setOnClickListener(v -> downloadTemplate());
        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tv_event_name);
        tvImportStatus = findViewById(R.id.tv_import_status);
        tvExportStatus = findViewById(R.id.tv_export_status);
        btnImport = findViewById(R.id.btn_import);
        btnExportExcel = findViewById(R.id.btn_export_excel);
        btnExportPDF = findViewById(R.id.btn_export_pdf);
        btnDownloadTemplate = findViewById(R.id.btn_download_template);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void selectFileToImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn file Excel"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                importFromExcel(fileUri);
            }
        }
    }

    private void importFromExcel(Uri fileUri) {
        progressBar.setVisibility(View.VISIBLE);
        tvImportStatus.setText("Đang import...");

        executorService.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                List<Guest> guests = new ArrayList<>();
                List<Ticket> tickets = new ArrayList<>();

                // Skip header row
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String name = getCellValue(row, 0);
                    String email = getCellValue(row, 1);
                    String phone = getCellValue(row, 2);
                    String ticketType = getCellValue(row, 3);

                    if (name.isEmpty()) continue;

                    // Create guest
                    Guest guest = new Guest();
                    guest.setName(name);
                    guest.setEmail(email);
                    guest.setPhone(phone);
                    guests.add(guest);

                    // Create ticket with QR code
                    Ticket ticket = new Ticket();
                    ticket.setQrCode(UUID.randomUUID().toString());
                    ticket.setPurchaseDate(OTPUtils.getCurrentTimestamp());
                    ticket.setStatus("booked");
                    ticket.setCreatedAt(OTPUtils.getCurrentTimestamp());
                    tickets.add(ticket);
                }

                workbook.close();
                inputStream.close();

                int count = guests.size();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (count > 0) {
                        tvImportStatus.setText(String.format("Đã import %d người tham dự", count));
                        Snackbar.make(btnImport, "Import thành công " + count + " người", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(Color.parseColor("#4CAF50"))
                                .setTextColor(Color.WHITE)
                                .show();
                    } else {
                        tvImportStatus.setText("Không có dữ liệu để import");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvImportStatus.setText("Lỗi import: " + e.getMessage());
                    Snackbar.make(btnImport, "Lỗi khi import file", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#ED2A2A"))
                            .setTextColor(Color.WHITE)
                            .show();
                });
            }
        });
    }

    private String getCellValue(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null) return "";
        try {
            return row.getCell(cellIndex).getStringCellValue().trim();
        } catch (Exception e) {
            try {
                return String.valueOf((int) row.getCell(cellIndex).getNumericCellValue());
            } catch (Exception ex) {
                return "";
            }
        }
    }

    private void exportToExcel() {
        progressBar.setVisibility(View.VISIBLE);
        tvExportStatus.setText("Đang xuất Excel...");

        // Get tickets for this event and export
        ticketViewModel.getTicketsByEvent(eventId).observe(this, tickets -> {
            if (tickets != null) {
                executorService.execute(() -> {
                    List<ExportUtils.AttendeeData> attendees = new ArrayList<>();

                    for (Ticket ticket : tickets) {
                        attendees.add(new ExportUtils.AttendeeData(
                                "Người tham dự", // Would need to join with account
                                "", // Email
                                "", // Phone
                                "Standard", // Ticket type
                                ticket.getQrCode(),
                                ticket.getPurchaseDate(),
                                ticket.getStatus()
                        ));
                    }

                    String filePath = ExportUtils.exportAttendeesToExcel(this, eventName, attendees);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (filePath != null) {
                            tvExportStatus.setText("Đã xuất file Excel");
                            shareFile(filePath);
                        } else {
                            tvExportStatus.setText("Lỗi khi xuất file");
                        }
                    });
                });
            }
        });
    }

    private void exportToPDF() {
        progressBar.setVisibility(View.VISIBLE);
        tvExportStatus.setText("Đang xuất PDF...");

        ticketViewModel.getTicketsByEvent(eventId).observe(this, tickets -> {
            if (tickets != null) {
                executorService.execute(() -> {
                    List<ExportUtils.AttendeeData> attendees = new ArrayList<>();

                    for (Ticket ticket : tickets) {
                        attendees.add(new ExportUtils.AttendeeData(
                                "Người tham dự",
                                "",
                                "",
                                "Standard",
                                ticket.getQrCode(),
                                ticket.getPurchaseDate(),
                                ticket.getStatus()
                        ));
                    }

                    String filePath = ExportUtils.exportAttendeesToPDF(this, eventName, attendees);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (filePath != null) {
                            tvExportStatus.setText("Đã xuất file PDF");
                            shareFile(filePath);
                        } else {
                            tvExportStatus.setText("Lỗi khi xuất file");
                        }
                    });
                });
            }
        });
    }

    private void downloadTemplate() {
        executorService.execute(() -> {
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Template");

                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Họ tên", "Email", "Số điện thoại", "Loại vé"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Sample data row
                Row sampleRow = sheet.createRow(1);
                sampleRow.createCell(0).setCellValue("Nguyễn Văn A");
                sampleRow.createCell(1).setCellValue("example@email.com");
                sampleRow.createCell(2).setCellValue("0123456789");
                sampleRow.createCell(3).setCellValue("VIP");

                // Save
                File exportDir = new File(getExternalFilesDir(null), "exports");
                if (!exportDir.exists()) exportDir.mkdirs();
                File file = new File(exportDir, "import_template.xlsx");

                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                workbook.write(fos);
                fos.close();
                workbook.close();

                runOnUiThread(() -> {
                    shareFile(file.getAbsolutePath());
                    Toast.makeText(this, "Đã tạo file mẫu", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tạo file mẫu", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void shareFile(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (filePath.endsWith(".pdf")) {
            shareIntent.setType("application/pdf");
        } else {
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ file"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
