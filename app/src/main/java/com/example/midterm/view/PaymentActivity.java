package com.example.midterm.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.midterm.R;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.entity.Ticket;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvEventName, tvSubtotal, tvDiscount, tvTotal;
    private LinearLayout llOrderItems;
    private RadioGroup rgPaymentMethod;
    private Button btnConfirmPayment;
    private ProgressBar progressBar;

    private int eventId, userId, discountId = -1;
    private String eventName;
    private double subtotal, discount, total;
    private ArrayList<Integer> ticketTypeIds;
    private ArrayList<Integer> quantities;
    private double[] prices;
    private ArrayList<String> ticketNames;
    private ArrayList<Long> seatIds;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        getIntentData();
        initViews();
        setupToolbar();
        displayOrderSummary();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        eventId = intent.getIntExtra("EVENT_ID", -1);
        eventName = intent.getStringExtra("EVENT_NAME");
        userId = intent.getIntExtra("USER_ID", -1);
        subtotal = intent.getDoubleExtra("SUBTOTAL", 0);
        discount = intent.getDoubleExtra("DISCOUNT", 0);
        total = intent.getDoubleExtra("TOTAL", 0);
        discountId = intent.getIntExtra("DISCOUNT_ID", -1);

        ticketTypeIds = intent.getIntegerArrayListExtra("TICKET_TYPE_IDS");
        quantities = intent.getIntegerArrayListExtra("QUANTITIES");
        prices = intent.getDoubleArrayExtra("PRICES");
        ticketNames = intent.getStringArrayListExtra("TICKET_NAMES");
        seatIds = (ArrayList<Long>) intent.getSerializableExtra("SEAT_IDS");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEventName = findViewById(R.id.tv_event_name);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvTotal = findViewById(R.id.tv_total);
        llOrderItems = findViewById(R.id.ll_order_items);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayOrderSummary() {
        tvEventName.setText(eventName);
        tvSubtotal.setText(currencyFormatter.format(subtotal));
        tvDiscount.setText("-" + currencyFormatter.format(discount));
        tvTotal.setText(currencyFormatter.format(total));

        // Display order items
        llOrderItems.removeAllViews();
        for (int i = 0; i < ticketNames.size(); i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_order_summary, llOrderItems, false);
            TextView tvName = itemView.findViewById(R.id.tv_item_name);
            TextView tvPrice = itemView.findViewById(R.id.tv_item_price);

            String name = ticketNames.get(i) + " x" + quantities.get(i);
            double itemTotal = prices[i] * quantities.get(i);

            tvName.setText(name);
            tvPrice.setText(currencyFormatter.format(itemTotal));
            llOrderItems.addView(itemView);
        }
    }

    private void setupListeners() {
        btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnConfirmPayment.setEnabled(false);

        // Simulate payment processing
        btnConfirmPayment.postDelayed(() -> {
            // Generate tickets
            generateTickets();
        }, 2000);
    }

    private void generateTickets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                String currentTime = dateFormat.format(new Date());
                List<String> generatedQRCodes = new ArrayList<>();

                // Create tickets for each ticket type
                int seatIndex = 0;
                for (int i = 0; i < ticketTypeIds.size(); i++) {
                    int ticketTypeId = ticketTypeIds.get(i);
                    int quantity = quantities.get(i);

                    for (int j = 0; j < quantity; j++) {
                        String qrCode = UUID.randomUUID().toString();
                        generatedQRCodes.add(qrCode);

                        Ticket ticket = new Ticket();
                        ticket.setTicketTypeID(ticketTypeId);
                        ticket.setBuyerID(userId);
                        ticket.setQrCode(qrCode);
                        ticket.setPurchaseDate(currentTime);
                        ticket.setStatus("booked");
                        ticket.setCreatedAt(currentTime);
                        ticket.setUpdatedAt(currentTime);

                        // Set seat ID if available
                        if (seatIds != null && seatIndex < seatIds.size()) {
                            ticket.setSeatId(seatIds.get(seatIndex));
                            seatIndex++;
                        }

                        db.ticketDAO().insert(ticket);
                    }

                    // Cập nhập số lượng vé đã bán của loại vé đó
                    db.ticketTypeDAO().incrementSoldQuantity(ticketTypeId, quantity);
                }

                // Đánh dấu ghé đã được đặt
                if (seatIds != null && !seatIds.isEmpty()) {
                    db.seatDAO().bookSeats(seatIds, currentTime);
                }

                // Update discount usage if applied
                if (discountId > 0) {
                    db.discountDAO().incrementUsedCount(discountId);
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showConfirmationDialog(generatedQRCodes);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnConfirmPayment.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi tạo vé: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showConfirmationDialog(List<String> qrCodes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ticket_confirmation, null);

        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        ImageView ivQrCode = dialogView.findViewById(R.id.iv_qr_code);
        TextView tvTicketCount = dialogView.findViewById(R.id.tv_ticket_count);
        Button btnViewTickets = dialogView.findViewById(R.id.btn_view_tickets);
        Button btnBackHome = dialogView.findViewById(R.id.btn_back_home);

        tvMessage.setText("Thanh toán thành công!");
        tvTicketCount.setText("Bạn đã mua " + qrCodes.size() + " vé");

        // Generate and display first QR code
        if (!qrCodes.isEmpty()) {
            Bitmap qrBitmap = generateQRCode(qrCodes.get(0));
            if (qrBitmap != null) {
                ivQrCode.setImageBitmap(qrBitmap);
            }
        }

        AlertDialog dialog = builder.setView(dialogView)
                .setCancelable(false)
                .create();

        btnViewTickets.setOnClickListener(v -> {
            dialog.dismiss();
            // Chuyển hướng đến vé của tôi
            Intent intent = new Intent(this, MyTicketsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        btnViewTickets.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, Homepage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("FROM_PAYMENT", true);
            intent.putExtra("user_id", userId);

            startActivity(intent);
            finish();
        });

        btnBackHome.setOnClickListener(v -> {
            dialog.dismiss();
            // Chuyển hướng đến homepage
            Intent intent = new Intent(this, Homepage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
