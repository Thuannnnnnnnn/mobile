package com.example.midterm.view.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.midterm.view.fragment.AttendeeManagementFragment;
import com.example.midterm.view.fragment.ManageSeatFragment;
import com.example.midterm.view.fragment.OverviewEventFragment;
import com.example.midterm.view.fragment.TicketManageFragment;

public class ViewEventDetailsOrganizerAdapter extends FragmentStateAdapter {
    private int eventId;

    public ViewEventDetailsOrganizerAdapter(@NonNull FragmentActivity fragmentActivity, int eventId) {
        super(fragmentActivity);
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OverviewEventFragment.newInstance(eventId);
            case 1:
                return TicketManageFragment.newInstance(eventId);
            case 2:
                return AttendeeManagementFragment.newInstance(eventId);
            case 3:
                return ManageSeatFragment.newInstance(eventId);
            default:
                return OverviewEventFragment.newInstance(eventId);
        }
    }
    @Override
    public int getItemCount() {
        return 4; // Số tab
    }
}