package com.schneewittchen.rosandroid.utility;

/**
 * Callback interface for WiFi connection operations
 */
public interface WifiConnectCallback {
    /**
     * Called when WiFi connection is successful
     */
    void onConnectSuccess();

    /**
     * Called when WiFi connection fails
     * @param reason The reason for failure
     */
    void onConnectFailed(String reason);
}
