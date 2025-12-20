package com.schneewittchen.rosandroid.ui.fragments.wifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.schneewittchen.rosandroid.R;
import com.schneewittchen.rosandroid.viewmodel.WifiSettingsViewModel;

/**
 * Fragment for WiFi Settings
 * Allows users to configure and connect to WiFi networks
 *
 * @author Generated for ROS2-Mobile-Android
 * @version 1.0.0
 * @created on 2025-12-20
 */
public class WifiSettingsFragment extends Fragment {

    private static final String TAG = WifiSettingsFragment.class.getSimpleName();

    private WifiSettingsViewModel viewModel;
    private TextInputEditText wifiSsidInput;
    private TextInputEditText wifiPasswordInput;
    private CheckBox rememberPasswordCheckbox;
    private MaterialButton connectButton;
    private ProgressBar connectionProgress;
    private TextView connectionStatus;
    private TextView discoveredDevicesTitle;
    private RecyclerView discoveredDevicesList;
    private DeviceListAdapter deviceListAdapter;

    public static WifiSettingsFragment newInstance() {
        return new WifiSettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WifiSettingsViewModel.class);

        // Initialize views
        initializeViews(view);

        // Load saved credentials
        loadSavedCredentials();

        // Setup observers
        setupObservers();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews(View view) {
        wifiSsidInput = view.findViewById(R.id.wifi_ssid_input);
        wifiPasswordInput = view.findViewById(R.id.wifi_password_input);
        rememberPasswordCheckbox = view.findViewById(R.id.remember_password_checkbox);
        connectButton = view.findViewById(R.id.connect_button);
        connectionProgress = view.findViewById(R.id.connection_progress);
        connectionStatus = view.findViewById(R.id.connection_status);
        discoveredDevicesTitle = view.findViewById(R.id.discovered_devices_title);
        discoveredDevicesList = view.findViewById(R.id.discovered_devices_list);

        // Setup RecyclerView
        deviceListAdapter = new DeviceListAdapter();
        discoveredDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        discoveredDevicesList.setAdapter(deviceListAdapter);
    }

    private void loadSavedCredentials() {
        String savedSsid = viewModel.getSavedWifiSsid();
        String savedPassword = viewModel.getSavedWifiPassword();
        boolean rememberPassword = viewModel.isRememberPassword();

        if (!savedSsid.isEmpty()) {
            wifiSsidInput.setText(savedSsid);
        }

        if (!savedPassword.isEmpty()) {
            wifiPasswordInput.setText(savedPassword);
        }

        rememberPasswordCheckbox.setChecked(rememberPassword);
    }

    private void setupObservers() {
        // Observe connection status
        viewModel.getConnectionStatus().observe(getViewLifecycleOwner(), status -> {
            updateUIForConnectionStatus(status);
        });

        // Observe discovered devices
        viewModel.getDiscoveredDevices().observe(getViewLifecycleOwner(), devices -> {
            if (devices != null && !devices.isEmpty()) {
                deviceListAdapter.setDevices(devices);
                discoveredDevicesTitle.setVisibility(View.VISIBLE);
                discoveredDevicesList.setVisibility(View.VISIBLE);
            } else {
                if (viewModel.getConnectionStatus().getValue() == WifiSettingsViewModel.ConnectionStatus.CONNECTED) {
                    // Show "no devices found" message if connected but no devices
                    connectionStatus.setText(R.string.no_devices_found);
                    connectionStatus.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupListeners() {
        connectButton.setOnClickListener(v -> {
            String ssid = wifiSsidInput.getText() != null ? wifiSsidInput.getText().toString().trim() : "";
            String password = wifiPasswordInput.getText() != null ? wifiPasswordInput.getText().toString() : "";
            boolean rememberPassword = rememberPasswordCheckbox.isChecked();

            if (ssid.isEmpty()) {
                wifiSsidInput.setError(getString(R.string.wifi_ssid_hint));
                wifiSsidInput.requestFocus();
                return;
            }

            // Save credentials
            viewModel.saveWifiCredentials(ssid, password, rememberPassword);

            // Attempt to connect
            viewModel.connectToWifi(ssid, password);
        });
    }

    private void updateUIForConnectionStatus(WifiSettingsViewModel.ConnectionStatus status) {
        switch (status) {
            case IDLE:
                connectionProgress.setVisibility(View.GONE);
                connectionStatus.setVisibility(View.GONE);
                connectButton.setEnabled(true);
                discoveredDevicesTitle.setVisibility(View.GONE);
                discoveredDevicesList.setVisibility(View.GONE);
                break;

            case CONNECTING:
                connectionProgress.setVisibility(View.VISIBLE);
                connectionStatus.setVisibility(View.VISIBLE);
                connectionStatus.setText(R.string.connecting_to_wifi);
                connectButton.setEnabled(false);
                discoveredDevicesTitle.setVisibility(View.GONE);
                discoveredDevicesList.setVisibility(View.GONE);
                break;

            case CONNECTED:
                connectionProgress.setVisibility(View.VISIBLE);
                connectionStatus.setVisibility(View.VISIBLE);
                connectionStatus.setText(R.string.discovering_devices);
                connectButton.setEnabled(true);
                Toast.makeText(getContext(), R.string.wifi_connected, Toast.LENGTH_SHORT).show();
                break;

            case FAILED:
                connectionProgress.setVisibility(View.GONE);
                connectionStatus.setVisibility(View.VISIBLE);
                connectionStatus.setText(R.string.wifi_connection_failed);
                connectButton.setEnabled(true);
                Toast.makeText(getContext(), R.string.wifi_connection_failed, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.resetConnectionStatus();
    }
}
