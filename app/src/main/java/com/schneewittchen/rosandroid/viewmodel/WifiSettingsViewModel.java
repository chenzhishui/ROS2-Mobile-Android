package com.schneewittchen.rosandroid.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schneewittchen.rosandroid.utility.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for WiFi Settings Fragment
 * Handles WiFi connection and device discovery
 *
 * @author Generated for ROS2-Mobile-Android
 * @version 1.0.0
 * @created on 2025-12-20
 */
public class WifiSettingsViewModel extends AndroidViewModel {

    private static final String TAG = WifiSettingsViewModel.class.getSimpleName();
    private static final String PREFS_NAME = "wifi_settings_prefs";
    private static final String KEY_WIFI_SSID = "wifi_ssid";
    private static final String KEY_WIFI_PASSWORD = "wifi_password";
    private static final String KEY_REMEMBER_PASSWORD = "remember_password";
    
    // Device discovery constants
    private static final int MAX_SCAN_RANGE = 20;
    private static final int ROS_MASTER_PORT = 11311;
    private static final int ROSBRIDGE_PORT = 9090;
    private static final int CONNECTION_TIMEOUT_MS = 500;
    private static final int WIFI_CONNECTION_WAIT_MS = 3000;

    private final MutableLiveData<ConnectionStatus> connectionStatus;
    private final MutableLiveData<List<String>> discoveredDevices;
    private final WifiManager wifiManager;
    private final ConnectivityManager connectivityManager;
    private final SharedPreferences sharedPreferences;

    public enum ConnectionStatus {
        IDLE,
        CONNECTING,
        CONNECTED,
        FAILED
    }

    public WifiSettingsViewModel(@NonNull Application application) {
        super(application);

        connectionStatus = new MutableLiveData<>(ConnectionStatus.IDLE);
        discoveredDevices = new MutableLiveData<>(new ArrayList<>());

        wifiManager = (WifiManager) application.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) application.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    public LiveData<List<String>> getDiscoveredDevices() {
        return discoveredDevices;
    }

    /**
     * Save WiFi credentials to SharedPreferences
     */
    public void saveWifiCredentials(String ssid, String password, boolean rememberPassword) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_WIFI_SSID, ssid);
        editor.putBoolean(KEY_REMEMBER_PASSWORD, rememberPassword);

        if (rememberPassword) {
            editor.putString(KEY_WIFI_PASSWORD, password);
        } else {
            editor.remove(KEY_WIFI_PASSWORD);
        }

        editor.apply();
    }

    /**
     * Get saved WiFi SSID
     */
    public String getSavedWifiSsid() {
        return sharedPreferences.getString(KEY_WIFI_SSID, "");
    }

    /**
     * Get saved WiFi password
     */
    public String getSavedWifiPassword() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER_PASSWORD, false)) {
            return sharedPreferences.getString(KEY_WIFI_PASSWORD, "");
        }
        return "";
    }

    /**
     * Check if password should be remembered
     */
    public boolean isRememberPassword() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_PASSWORD, false);
    }

    /**
     * Connect to WiFi network
     * Note: On Android 10+ (API 29+), apps cannot programmatically connect to WiFi
     * This method attempts connection for older Android versions
     */
    public void connectToWifi(String ssid, String password) {
        Log.d(TAG, "Attempting to connect to WiFi: " + ssid);
        connectionStatus.postValue(ConnectionStatus.CONNECTING);

        // For Android 10+ (API 29+), WiFi connection must be done by the user through system settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, we can suggest a network but cannot connect automatically
            Log.d(TAG, "Using WifiNetworkSpecifier for Android 10+");
            suggestWifiNetwork(ssid, password);
        } else {
            // For older Android versions, attempt programmatic connection
            Log.d(TAG, "Using legacy WiFi connection method");
            connectToWifiLegacy(ssid, password);
        }
    }

    /**
     * Suggest WiFi network for Android 10+ using NetworkRequest
     */
    private void suggestWifiNetwork(String ssid, String password) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(ssid);
            
            if (password != null && !password.isEmpty()) {
                builder.setWpa2Passphrase(password);
            }

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(builder.build())
                    .build();

            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    Log.d(TAG, "WiFi network available");
                    connectionStatus.postValue(ConnectionStatus.CONNECTED);
                    discoverDevices();
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.w(TAG, "WiFi network unavailable");
                    connectionStatus.postValue(ConnectionStatus.FAILED);
                }
            };

            connectivityManager.requestNetwork(request, networkCallback);
        }
    }

    /**
     * Legacy method for connecting to WiFi on Android versions before 10
     */
    @SuppressWarnings("deprecation")
    private void connectToWifiLegacy(String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        
        if (password != null && !password.isEmpty()) {
            wifiConfig.preSharedKey = String.format("\"%s\"", password);
        } else {
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        int netId = wifiManager.addNetwork(wifiConfig);
        
        if (netId != -1) {
            wifiManager.disconnect();
            boolean enabled = wifiManager.enableNetwork(netId, true);
            boolean reconnected = wifiManager.reconnect();

            if (enabled && reconnected) {
                // Give it some time to connect
                new Thread(() -> {
                    try {
                        Thread.sleep(WIFI_CONNECTION_WAIT_MS);
                        if (isConnectedToWifi()) {
                            connectionStatus.postValue(ConnectionStatus.CONNECTED);
                            discoverDevices();
                        } else {
                            connectionStatus.postValue(ConnectionStatus.FAILED);
                            Log.w(TAG, "WiFi connection failed - not connected after timeout");
                        }
                    } catch (InterruptedException e) {
                        Log.w(TAG, "WiFi connection interrupted", e);
                        connectionStatus.postValue(ConnectionStatus.FAILED);
                    }
                }).start();
            } else {
                connectionStatus.postValue(ConnectionStatus.FAILED);
            }
        } else {
            connectionStatus.postValue(ConnectionStatus.FAILED);
        }
    }

    /**
     * Check if device is connected to WiFi
     */
    private boolean isConnectedToWifi() {
        if (wifiManager == null) {
            return false;
        }
        return wifiManager.isWifiEnabled() && wifiManager.getConnectionInfo().getNetworkId() != -1;
    }

    /**
     * Discover devices on the network
     * This method scans the local network for potential ROS devices
     */
    public void discoverDevices() {
        Log.d(TAG, "Starting device discovery");
        new Thread(() -> {
            List<String> devices = new ArrayList<>();

            // Get the current IP address
            String currentIp = Utils.getIPAddress(true);
            
            if (currentIp != null && !currentIp.isEmpty()) {
                // Extract network prefix (e.g., "192.168.1.")
                String[] ipParts = currentIp.split("\\.");
                if (ipParts.length == 4) {
                    String networkPrefix = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";

                    // Scan common ROS ports on local network
                    // Scanning addresses based on MAX_SCAN_RANGE for performance
                    for (int i = 1; i <= MAX_SCAN_RANGE; i++) {
                        String testIp = networkPrefix + i;
                        
                        // Skip current device IP
                        if (testIp.equals(currentIp)) {
                            continue;
                        }

                        // Check common ROS ports: 11311 (ROS1 Master), 9090 (rosbridge)
                        if (Utils.isHostAvailable(testIp, ROS_MASTER_PORT, CONNECTION_TIMEOUT_MS) || 
                            Utils.isHostAvailable(testIp, ROSBRIDGE_PORT, CONNECTION_TIMEOUT_MS)) {
                            Log.d(TAG, "Discovered ROS device at: " + testIp);
                            devices.add(testIp);
                        }
                    }
                }
            }

            Log.d(TAG, "Device discovery completed. Found " + devices.size() + " device(s)");
            discoveredDevices.postValue(devices);
        }).start();
    }

    /**
     * Reset connection status
     */
    public void resetConnectionStatus() {
        connectionStatus.postValue(ConnectionStatus.IDLE);
        discoveredDevices.postValue(new ArrayList<>());
    }
}
