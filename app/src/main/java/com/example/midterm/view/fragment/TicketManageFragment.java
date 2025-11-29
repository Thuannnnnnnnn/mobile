package com.example.midterm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.view.Adapter.SoldTicketsAdapter;
import com.example.midterm.view.Adapter.TicketTypeManagementAdapter;
import com.example.midterm.view.dialog.AddTicketTypeDialog;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.TicketTypeViewModel;
import com.example.midterm.viewModel.TicketViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class TicketManageFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";
    private int eventId;

    private TextView tvTotalRevenue;
    private TextView tvTicketsSold;
    private Button btnAddTicketType;
    private RecyclerView rvTicketTypes;
    private RecyclerView rvSoldTickets;

    private TicketTypeManagementAdapter ticketTypeAdapter;
    private SoldTicketsAdapter soldTicketsAdapter;

    private TicketTypeViewModel ticketTypeViewModel;
    private TicketViewModel ticketViewModel;
    private EventViewModel eventViewModel;

    public static TicketManageFragment newInstance(int eventId) {
        TicketManageFragment fragment = new TicketManageFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        setupRecyclerViews();
        loadData();
        setupListeners();
    }

    private void initViews(View view) {
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvTicketsSold = view.findViewById(R.id.tv_tickets_sold);
        btnAddTicketType = view.findViewById(R.id.btn_add_ticket_type);
        rvTicketTypes = view.findViewById(R.id.rv_ticket_types);
        rvSoldTickets = view.findViewById(R.id.rv_sold_tickets);
    }

    private void initViewModels() {
        ticketTypeViewModel = new ViewModelProvider(this).get(TicketTypeViewModel.class);
        ticketViewModel = new ViewModelProvider(this).get(TicketViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
    }

    private void setupRecyclerViews() {
        ticketTypeAdapter = new TicketTypeManagementAdapter();
        rvTicketTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTicketTypes.setAdapter(ticketTypeAdapter);

        ticketTypeAdapter.setOnTicketTypeClickListener(ticketType -> {
            showEditTicketTypeDialog(ticketType);
        });

        soldTicketsAdapter = new SoldTicketsAdapter();
        rvSoldTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSoldTickets.setAdapter(soldTicketsAdapter);

        soldTicketsAdapter.setOnTicketClickListener(ticket -> {
            Toast.makeText(getContext(), "Mã vé: " + ticket.getQrCode(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadData() {
        ticketTypeViewModel.getTicketsByEventId(eventId).observe(getViewLifecycleOwner(), ticketTypes -> {
            ticketTypeAdapter.setTicketTypes(ticketTypes);
        });

        ticketViewModel.getTicketsWithDetailsByEvent(eventId).observe(getViewLifecycleOwner(), tickets -> {
            soldTicketsAdapter.setTickets(tickets);
        });

        eventViewModel.getTotalRevenueForEvent(eventId).observe(getViewLifecycleOwner(), revenue -> {
            if (revenue != null) {
                NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvTotalRevenue.setText(currencyFormat.format(revenue) + " VNĐ");
            } else {
                tvTotalRevenue.setText("0 VNĐ");
            }
        });

        eventViewModel.getTotalTicketsSoldForEvent(eventId).observe(getViewLifecycleOwner(), soldCount -> {
            eventViewModel.getTotalCapacityForEvent(eventId).observe(getViewLifecycleOwner(), capacity -> {
                if (soldCount != null && capacity != null) {
                    tvTicketsSold.setText(soldCount + " / " + capacity + " vé");
                } else if (soldCount != null) {
                    tvTicketsSold.setText(soldCount + " vé");
                } else {
                    tvTicketsSold.setText("0 vé");
                }
            });
        });
    }

    private void setupListeners() {
        btnAddTicketType.setOnClickListener(v -> showAddTicketTypeDialog());
    }

    private void showAddTicketTypeDialog() {
        AddTicketTypeDialog dialog = new AddTicketTypeDialog(
                requireContext(),
                eventId,
                ticketType -> {
                    ticketTypeViewModel.insertTicket(ticketType);
                    Toast.makeText(getContext(), "Đã thêm loại vé mới", Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }

    private void showEditTicketTypeDialog(TicketType ticketType) {
        AddTicketTypeDialog dialog = new AddTicketTypeDialog(
                requireContext(),
                eventId,
                ticketType,
                updatedTicketType -> {
                    ticketTypeViewModel.updateTicket(updatedTicketType);
                    Toast.makeText(getContext(), "Đã cập nhật loại vé", Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }
}
