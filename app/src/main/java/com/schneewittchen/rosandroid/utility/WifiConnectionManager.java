package com.schneewittchen.rosandroid.utility;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager class for WiFi connection operations
 * Handles scanning and connecting to WiFi networks
 */
public class WifiConnectionManager {
    private static final String TAG = "WifiConnectionManager";
    private static final long SCAN_TIMEOUT_MS = 10000; // 10 seconds timeout for scan

    private final Context context;
    private Handler timeoutHandler;
    private Runnable scanTimeoutRunnable;

    public WifiConnectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.timeoutHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Scan for WiFi network before connecting
     * This addresses the issue where onScanFailed is not a valid method
     * 
     * @param ssid SSID of the network to connect to
     * @param password Password for the network
     * @param callback Callback for connection result
     */
    public void performPreConnectScan(String ssid, String password, WifiConnectCallback callback) {
        Log.d(TAG, "连接前先扫描WiFi网络...");
        
        // Clear any existing timeout before setting a new one
        synchronized (this) {
            if (scanTimeoutRunnable != null) {
                timeoutHandler.removeCallbacks(scanTimeoutRunnable);
            }
        }
        
        final AtomicBoolean scanCompleted = new AtomicBoolean(false);
        
        // Setup timeout for scan operation
        scanTimeoutRunnable = () -> {
            if (scanCompleted.compareAndSet(false, true)) {
                Log.e(TAG, "扫描超时");
                callback.onConnectFailed("网络扫描超时，请重试");
            }
        };
        timeoutHandler.postDelayed(scanTimeoutRunnable, SCAN_TIMEOUT_MS);
        
        try {
            WifiUtils.withContext(context)
                    .scanWifi(results -> {
                        if (!scanCompleted.compareAndSet(false, true)) {
                            // Scan already completed (timeout occurred)
                            return;
                        }
                        timeoutHandler.removeCallbacks(scanTimeoutRunnable);
                        
                        if (results == null || results.isEmpty()) {
                            Log.e(TAG, "扫描结果为空");
                            callback.onConnectFailed("未检测到任何WiFi网络");
                            return;
                        }
                        
                        boolean networkFound = false;
                        
                        // Check if target network exists in scan results
                        for (ScanResult result : results) {
                            if (result.SSID != null && result.SSID.equals(ssid)) {
                                networkFound = true;
                                break;
                            }
                        }
                        
                        if (networkFound) {
                            Log.d(TAG, "找到目标网络: " + ssid);
                            // Network exists, proceed with connection
                            performConnect(ssid, password, callback);
                        } else {
                            Log.e(TAG, "未找到目标网络: " + ssid);
                            callback.onConnectFailed("未找到目标网络，请确保设备在网络范围内");
                        }
                    })
                    .start();
        } catch (Exception e) {
            if (scanCompleted.compareAndSet(false, true)) {
                timeoutHandler.removeCallbacks(scanTimeoutRunnable);
                Log.e(TAG, "扫描启动失败: " + e.getMessage(), e);
                callback.onConnectFailed("网络扫描启动失败: " + e.getMessage());
            }
        }
    }

    /**
     * Perform WiFi connection
     * 
     * @param ssid SSID of the network
     * @param password Password for the network
     * @param callback Callback for connection result
     */
    private void performConnect(String ssid, String password, WifiConnectCallback callback) {
        Log.d(TAG, "开始连接WiFi: " + ssid);
        
        WifiUtils.withContext(context)
                .connectWith(ssid, password)
                .setTimeout(15000) // 15 seconds timeout for connection
                .onConnectionResult(new ConnectionSuccessListener() {
                    @Override
                    public void success() {
                        Log.d(TAG, "WiFi连接成功: " + ssid);
                        callback.onConnectSuccess();
                    }

                    @Override
                    public void failed(ConnectionErrorCode errorCode) {
                        Log.e(TAG, "WiFi连接失败: " + errorCode.toString());
                        String errorMessage = getErrorMessage(errorCode);
                        callback.onConnectFailed(errorMessage);
                    }
                })
                .start();
    }

    /**
     * Get user-friendly error message based on error code
     * 
     * @param errorCode Connection error code
     * @return User-friendly error message
     */
    private String getErrorMessage(ConnectionErrorCode errorCode) {
        switch (errorCode) {
            case TIMEOUT_OCCURRED:
                return "连接超时，请检查密码是否正确";
            case COULD_NOT_CONNECT:
                return "无法连接到网络，请检查密码";
            case DID_NOT_FIND_NETWORK_BY_SCANNING:
                return "未找到指定网络";
            default:
                return "连接失败: " + errorCode.toString();
        }
    }

    /**
     * Clean up resources
     * Thread-safe method to remove any pending timeout callbacks
     */
    public synchronized void cleanup() {
        if (timeoutHandler != null && scanTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(scanTimeoutRunnable);
            scanTimeoutRunnable = null;
        }
    }
}
