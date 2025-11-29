package com.example.midterm.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.Ticket;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = true;
    private AppDatabase db; // Khai báo Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        db = AppDatabase.getInstance(getApplicationContext()); // Khởi tạo Database

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quét mã QR Check-in");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Kiểm tra quyền camera
        if (checkCameraPermission()) {
            startScanning();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Cần quyền truy cập camera để quét QR code",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startScanning() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && isScanning) {
                    isScanning = false; // Dừng quét tạm thời để xử lý
                    handleQRCodeScanned(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
    }

    private void handleQRCodeScanned(String scannedCode) {
        // Pause scanning
        barcodeView.pause();

        new Thread(() -> {
            // 1. Tìm vé trong Database
            Ticket ticket = db.ticketDAO().getTicketByQr(scannedCode);

            runOnUiThread(() -> {
                if (ticket == null) {
                    // Vé không tồn tại
                    Toast.makeText(this, "Vé không hợp lệ!", Toast.LENGTH_SHORT).show();
                } else {
                    if ("used".equals(ticket.status)) {
                        // Vé đã dùng rồi
                        Toast.makeText(this, "Cảnh báo: Vé này ĐÃ ĐƯỢC SỬ DỤNG trước đó!", Toast.LENGTH_LONG).show();
                        // TODO: playSound(R.raw.error_sound);
                    } else {
                        // Vé hợp lệ -> Cập nhật trạng thái
                        ticket.status = "used";
                        new Thread(() -> db.ticketDAO().updateTicket(ticket)).start();

                        Toast.makeText(this, "Check-in THÀNH CÔNG! Xin mời vào.", Toast.LENGTH_SHORT).show();
                        // TODO: Hiển thị thông tin khách hàng lên màn hình để đối chiếu
                        // showTicketInfoDialog(ticket);
                    }
                }
                // Reset lại scanner để quét người tiếp theo sau 2 giây
                barcodeView.postDelayed(() -> {
                    isScanning = true;
                    barcodeView.resume();
                }, 2000);
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkCameraPermission()) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
