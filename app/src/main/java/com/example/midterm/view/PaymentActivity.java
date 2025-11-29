package com.example.midterm.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull; // Thêm import này
import androidx.appcompat.app.AppCompatActivity;

import com.example.midterm.R;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {

    Button btnPayNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Khởi tạo ZaloPay Sandbox
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ZaloPaySDK.init(2553, Environment.SANDBOX); // AppID Sandbox mặc định

        btnPayNow = findViewById(R.id.btn_confirm_payment); // ID nút thanh toán của bạn

        btnPayNow.setOnClickListener(v -> requestZaloPay());
    }

    private void requestZaloPay() {
        // Tạo đơn hàng giả lập (Trong thực tế cần gọi API Backend của bạn để lấy token)
        // Đây là code gọi trực tiếp CreateOrder của ZaloPay (chỉ dùng cho Demo đồ án)
        vn.zalopay.sdk.ZaloPaySDK.getInstance().payOrder(this, "token_don_hang_tu_server", "zp_trans_token", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                Toast.makeText(PaymentActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                // 1. Cập nhật Database: Vé đã thanh toán
                // 2. Chuyển sang màn hình vé
                finish();
            }

            @Override
            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                Toast.makeText(PaymentActivity.this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                Toast.makeText(PaymentActivity.this, "Lỗi thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onNewIntent(@NonNull Intent intent) { // Thêm @NonNull
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}