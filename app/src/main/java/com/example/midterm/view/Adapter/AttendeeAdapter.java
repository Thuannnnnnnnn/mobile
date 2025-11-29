package com.example.midterm.view.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.dto.TicketWithDetails;

import java.util.ArrayList;
import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private List<TicketWithDetails> attendees = new ArrayList<>();
    private List<TicketWithDetails> attendeesFiltered = new ArrayList<>();
    private OnAttendeeClickListener listener;

    public interface OnAttendeeClickListener {
        void onAttendeeClick(TicketWithDetails attendee);
    }

    public void setOnAttendeeClickListener(OnAttendeeClickListener listener) {
        this.listener = listener;
    }

    public void setAttendees(List<TicketWithDetails> attendees) {
        this.attendees = attendees != null ? attendees : new ArrayList<>();
        this.attendeesFiltered = new ArrayList<>(this.attendees);
        notifyDataSetChanged();
    }

    public List<TicketWithDetails> getAttendees() {
        return attendees;
    }

    public void filter(String query) {
        attendeesFiltered.clear();

        if (query == null || query.isEmpty()) {
            attendeesFiltered.addAll(attendees);
        } else {
            String lowerQuery = query.toLowerCase();
            for (TicketWithDetails attendee : attendees) {
                if (matchesQuery(attendee, lowerQuery)) {
                    attendeesFiltered.add(attendee);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matchesQuery(TicketWithDetails attendee, String query) {
        if (attendee.getBuyerName() != null && attendee.getBuyerName().toLowerCase().contains(query)) {
            return true;
        }
        if (attendee.getBuyerEmail() != null && attendee.getBuyerEmail().toLowerCase().contains(query)) {
            return true;
        }
        if (attendee.getQrCode() != null && attendee.getQrCode().toLowerCase().contains(query)) {
            return true;
        }
        if (attendee.getTicketTypeCode() != null && attendee.getTicketTypeCode().toLowerCase().contains(query)) {
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketWithDetails attendee = attendeesFiltered.get(position);
        holder.bind(attendee);
    }

    @Override
    public int getItemCount() {
        return attendeesFiltered.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAttendeeName;
        private TextView tvAttendeeEmail;
        private TextView tvTicketType;
        private TextView tvTicketCode;
        private ImageView imgCheckinStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAttendeeName = itemView.findViewById(R.id.tv_attendee_name);
            tvAttendeeEmail = itemView.findViewById(R.id.tv_attendee_email);
            tvTicketType = itemView.findViewById(R.id.tv_ticket_type);
            tvTicketCode = itemView.findViewById(R.id.tv_ticket_code);
            imgCheckinStatus = itemView.findViewById(R.id.img_checkin_status);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAttendeeClick(attendeesFiltered.get(getAdapterPosition()));
                }
            });
        }

        public void bind(TicketWithDetails attendee) {
            tvAttendeeName.setText(attendee.getBuyerName() != null ? attendee.getBuyerName() : "Khách hàng");
            tvAttendeeEmail.setText(attendee.getBuyerEmail() != null ? attendee.getBuyerEmail() : "");
            tvTicketType.setText(attendee.getTicketTypeCode() != null ? attendee.getTicketTypeCode() : "N/A");

            String ticketCode = attendee.getQrCode() != null ? attendee.getQrCode() : "N/A";
            if (ticketCode.length() > 10) {
                ticketCode = ticketCode.substring(0, 10) + "...";
            }
            tvTicketCode.setText(ticketCode);

            updateCheckinStatusIcon(attendee.getStatus());
        }

        private void updateCheckinStatusIcon(String status) {
            if (status == null) {
                status = "booked";
            }

            switch (status) {
                case "checked_in":
                    imgCheckinStatus.setImageResource(R.drawable.checked__1_);
                    imgCheckinStatus.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "cancelled":
                    imgCheckinStatus.setImageResource(R.drawable.cancel);
                    imgCheckinStatus.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case "booked":
                default:
                    imgCheckinStatus.setImageResource(R.drawable.time__1_);
                    imgCheckinStatus.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                    break;
            }
        }
    }
}
