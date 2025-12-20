package com.schneewittchen.rosandroid.ui.fragments.wifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schneewittchen.rosandroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying discovered devices
 *
 * @author Generated for ROS2-Mobile-Android
 * @version 1.0.0
 * @created on 2025-12-20
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private List<String> devices;
    private OnDeviceClickListener onDeviceClickListener;

    public interface OnDeviceClickListener {
        void onDeviceClick(String deviceIp);
    }

    public DeviceListAdapter() {
        this.devices = new ArrayList<>();
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.onDeviceClickListener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discovered_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        String deviceIp = devices.get(position);
        holder.bind(deviceIp);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final TextView deviceIpTextView;
        private final TextView deviceNameTextView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIpTextView = itemView.findViewById(R.id.device_ip);
            deviceNameTextView = itemView.findViewById(R.id.device_name);

            itemView.setOnClickListener(v -> {
                if (onDeviceClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onDeviceClickListener.onDeviceClick(devices.get(position));
                    }
                }
            });
        }

        public void bind(String deviceIp) {
            deviceIpTextView.setText(deviceIp);
            deviceNameTextView.setText("ROS Device");
        }
    }
}
