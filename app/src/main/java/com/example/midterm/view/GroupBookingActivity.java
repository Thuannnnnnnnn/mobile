package com.example.midterm.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class GroupBookingActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvEventName, tvTicketInfo, tvTotalAmount, tvPerPerson;
    private RecyclerView rvMembers;
    private MaterialButton btnAddMember, btnShareLink, btnProceed;

    private MemberAdapter memberAdapter;
    private List<GroupMember> members = new ArrayList<>();

    private int eventId;
    private String eventName;
    private int ticketCount;
    private double totalAmount;
    private String groupCode;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_booking);

        getIntentData();
        initViews();
        setupToolbar();
        setupRecyclerView();
        generateGroupCode();
        updateUI();
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
        ticketCount = intent.getIntExtra("TICKET_COUNT", 1);
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0);

        // Add current user as first member
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Bạn");
        members.add(new GroupMember(userName, true, totalAmount / ticketCount));
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvEventName = findViewById(R.id.tv_event_name);
        tvTicketInfo = findViewById(R.id.tv_ticket_info);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvPerPerson = findViewById(R.id.tv_per_person);
        rvMembers = findViewById(R.id.rv_members);
        btnAddMember = findViewById(R.id.btn_add_member);
        btnShareLink = findViewById(R.id.btn_share_link);
        btnProceed = findViewById(R.id.btn_proceed);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đặt vé tập thể");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter(members, position -> {
            if (position > 0) { // Can't remove organizer
                members.remove(position);
                memberAdapter.notifyDataSetChanged();
                updateUI();
            }
        });
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);
    }

    private void generateGroupCode() {
        groupCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void updateUI() {
        tvEventName.setText(eventName);
        tvTicketInfo.setText(ticketCount + " vé");
        tvTotalAmount.setText(currencyFormatter.format(totalAmount));

        double perPerson = totalAmount / members.size();
        tvPerPerson.setText(currencyFormatter.format(perPerson) + "/người");

        // Update each member's amount
        for (GroupMember member : members) {
            member.amount = perPerson;
        }
        memberAdapter.notifyDataSetChanged();

        btnProceed.setEnabled(members.size() >= 1);
    }

    private void setupListeners() {
        btnAddMember.setOnClickListener(v -> showAddMemberDialog());

        btnShareLink.setOnClickListener(v -> shareGroupLink());

        btnProceed.setOnClickListener(v -> proceedToPayment());
    }

    private void showAddMemberDialog() {
        if (members.size() >= ticketCount) {
            Toast.makeText(this, "Số thành viên không thể vượt quá số vé", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        EditText etName = dialogView.findViewById(R.id.et_member_name);
        EditText etPhone = dialogView.findViewById(R.id.et_member_phone);

        builder.setView(dialogView)
                .setTitle("Thêm thành viên")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        members.add(new GroupMember(name, false, 0));
                        memberAdapter.notifyDataSetChanged();
                        updateUI();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void shareGroupLink() {
        String shareText = "Mời bạn tham gia đặt vé tập thể!\n\n" +
                "Sự kiện: " + eventName + "\n" +
                "Số vé: " + ticketCount + "\n" +
                "Mã nhóm: " + groupCode + "\n\n" +
                "Mỗi người trả: " + currencyFormatter.format(totalAmount / members.size());

        // Copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Group Booking", shareText);
        clipboard.setPrimaryClip(clip);

        // Share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }

    private void proceedToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("EVENT_NAME", eventName);
        intent.putExtra("SUBTOTAL", totalAmount);
        intent.putExtra("DISCOUNT", 0.0);
        intent.putExtra("TOTAL", totalAmount);
        intent.putExtra("IS_GROUP_BOOKING", true);
        intent.putExtra("GROUP_CODE", groupCode);
        intent.putExtra("MEMBER_COUNT", members.size());

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        intent.putExtra("USER_ID", prefs.getInt("user_id", -1));

        startActivity(intent);
    }

    // Data class
    public static class GroupMember {
        public String name;
        public boolean isOrganizer;
        public double amount;
        public boolean hasPaid;

        public GroupMember(String name, boolean isOrganizer, double amount) {
            this.name = name;
            this.isOrganizer = isOrganizer;
            this.amount = amount;
            this.hasPaid = false;
        }
    }

    // Adapter
    public static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private final List<GroupMember> members;
        private final OnRemoveListener listener;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        public interface OnRemoveListener {
            void onRemove(int position);
        }

        public MemberAdapter(List<GroupMember> members, OnRemoveListener listener) {
            this.members = members;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroupMember member = members.get(position);
            holder.tvName.setText(member.name + (member.isOrganizer ? " (Người tổ chức)" : ""));
            holder.tvAmount.setText(currencyFormatter.format(member.amount));

            // Hide remove button for organizer
            holder.btnRemove.setVisibility(member.isOrganizer ? View.GONE : View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> listener.onRemove(position));
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAmount;
            ImageView btnRemove;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_member_name);
                tvAmount = itemView.findViewById(R.id.tv_member_amount);
                btnRemove = itemView.findViewById(R.id.btn_remove);
            }
        }
    }
}
