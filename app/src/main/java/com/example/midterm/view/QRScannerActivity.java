package com.example.midterm.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider; // Cần thêm import này

import com.example.midterm.R;
import com.example.midterm.viewModel.TicketViewModel; // Import ViewModel
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = true;

    // Sửa: Dùng ViewModel thay vì AppDatabase trực tiếp
    private TicketViewModel ticketViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);

        // Khởi tạo ViewModel
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quét mã QR Check-in");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
                Toast.makeText(this, "Cần quyền camera để quét vé!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startScanning() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && isScanning) {
                    isScanning = false;
                    handleQRCodeScanned(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    private void handleQRCodeScanned(String qrCode) {
        barcodeView.pause();

        // Lấy thời gian check-in hiện tại
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            // GỌI VIEWMODEL (Code chuẩn):
            // Hàm này trong TicketViewModel đã xử lý logic:
            // 1. Tìm vé -> 2. Check trạng thái -> 3. Update thành 'checked_in'
            boolean isSuccess = ticketViewModel.checkInTicketByQrCode(qrCode, currentDateTime);

            runOnUiThread(() -> {
                if (isSuccess) {
                    // Check-in thành công
                    Toast.makeText(QRScannerActivity.this, "✅ Check-in THÀNH CÔNG!", Toast.LENGTH_SHORT).show();
                    // (Optional) Phát âm thanh "Beep" thành công tại đây
                } else {
                    // Thất bại (Vé giả hoặc Vé đã dùng rồi)
                    Toast.makeText(QRScannerActivity.this, "❌ LỖI: Vé không hợp lệ hoặc đã dùng!", Toast.LENGTH_LONG).show();
                    // (Optional) Phát âm thanh báo lỗi tại đây
                }

                // Quét tiếp sau 2 giây
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