# WiFi Settings Integration Guide

## Quick Start

To add the WiFi Settings tab to your application, follow these steps:

## Option 1: Add WiFi Settings as a Tab

### Step 1: Add WiFi Tab to Layout

Edit `app/src/main/res/layout/fragment_main.xml` and add a new TabItem after the SSH tab:

```xml
<com.google.android.material.tabs.TabItem
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="WiFi" />
```

The complete TabLayout section should look like:

```xml
<com.google.android.material.tabs.TabLayout
    android:id="@+id/tabs"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:tabGravity="center"
    app:tabIndicatorColor="#81D4FA"
    app:tabIndicatorGravity="bottom"
    app:tabMode="auto"
    app:tabPaddingEnd="0dp"
    app:tabPaddingStart="0dp">

    <com.google.android.material.tabs.TabItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Master" />

    <com.google.android.material.tabs.TabItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Viz" />

    <com.google.android.material.tabs.TabItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Details" />

    <com.google.android.material.tabs.TabItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="SSH" />

    <com.google.android.material.tabs.TabItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="WiFi" />
</com.google.android.material.tabs.TabLayout>
```

### Step 2: Add Navigation Handler

Edit `app/src/main/java/com/schneewittchen/rosandroid/ui/fragments/main/MainFragment.java`:

In the `onTabSelected` method, add a new case for "WiFi":

```java
tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.i(TAG, "On Tab selected: " + tab.getText());

        switch (tab.getText().toString()) {
            case "Master":
                navController.navigate(R.id.action_to_masterFragment);
                break;
            case "Details":
                navController.navigate(R.id.action_to_detailFragment);
                break;
            case "SSH":
                navController.navigate(R.id.action_to_sshFragment);
                break;
            case "WiFi":
                navController.navigate(R.id.action_to_wifiSettingsFragment);
                break;
            default:
                navController.navigate(R.id.action_to_vizFragment);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}
});
```

## Option 2: Navigate Programmatically

You can navigate to WiFi Settings from any fragment or activity:

```java
// From a Fragment
NavController navController = Navigation.findNavController(requireView());
navController.navigate(R.id.action_to_wifiSettingsFragment);

// Or from an Activity with a view
NavController navController = Navigation.findNavController(view);
navController.navigate(R.id.action_to_wifiSettingsFragment);
```

## Option 3: Use in Master Fragment

You can add a button or link in the Master Fragment to navigate to WiFi Settings:

```java
// In MasterFragment.java
Button wifiSettingsButton = view.findViewById(R.id.wifi_settings_button);
wifiSettingsButton.setOnClickListener(v -> {
    NavController navController = Navigation.findNavController(v);
    navController.navigate(R.id.action_to_wifiSettingsFragment);
});
```

## Testing the WiFi Settings Page

### Without Building the App

The WiFi Settings page is fully implemented and ready to use. The navigation has been set up in:
- `app/src/main/res/navigation/main_navigation.xml`

### With Building the App

1. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

2. Install on device:
   ```bash
   ./gradlew installDebug
   ```

3. Navigate to the WiFi Settings tab or trigger navigation programmatically.

## Features Available

Once integrated, users can:
1. Enter WiFi SSID and password
2. Choose to remember the password
3. Connect to the WiFi network
4. Automatically discover ROS devices on the network
5. View a list of discovered devices with their IP addresses
6. Click on a device to select it (can be customized)

## Customization

### Change Device Click Behavior

To add functionality when a user clicks on a discovered device, modify `WifiSettingsFragment.java`:

```java
deviceListAdapter.setOnDeviceClickListener(deviceIp -> {
    // Your custom logic here
    // For example, navigate to Master Fragment with the selected IP
    
    // Save the selected IP to ViewModel or SharedPreferences
    // Then navigate to Master Fragment
    NavController navController = Navigation.findNavController(requireView());
    navController.navigate(R.id.action_to_masterFragment);
});
```

### Adjust UI Colors

Modify colors in `fragment_wifi_settings.xml` or `res/values/colors.xml` to match your app theme.

### Change Discovery Range

By default, the app scans the first 20 IP addresses. To change this, edit `WifiSettingsViewModel.java`:

```java
// Change from 20 to your desired range
for (int i = 1; i <= 50; i++) {  // Now scans first 50 addresses
    // ... discovery logic
}
```

## Permissions

The following permissions are already configured in `AndroidManifest.xml`:
- `ACCESS_WIFI_STATE` - Read WiFi information
- `CHANGE_WIFI_STATE` - Modify WiFi connections
- `ACCESS_NETWORK_STATE` - Read network state
- `CHANGE_NETWORK_STATE` - Modify network state
- `ACCESS_FINE_LOCATION` - Required for WiFi scanning on Android 8.1+
- `INTERNET` - Network communication

**Note:** On Android 10+ (API 29+), programmatic WiFi connection requires user approval through a system dialog.

## Troubleshooting

### WiFi Connection Not Working on Android 10+

This is expected behavior. Android 10+ requires user approval for WiFi connections. The app will show a system dialog for the user to approve the connection.

### Device Discovery Returns No Devices

1. Ensure the device is connected to the WiFi network
2. Check that ROS devices are running on the network
3. Verify firewall settings allow connections on ROS ports (11311, 9090)
4. Increase the scan range if devices are on higher IP addresses
5. Add more ports to scan if your ROS setup uses different ports

### Navigation Not Working

Ensure that:
1. The navigation graph is properly configured in `main_navigation.xml`
2. The NavController is properly initialized in your fragment/activity
3. The navigation action ID matches in both the XML and Java code

## Example Screenshots

[Screenshots would appear here when the app is built and run]

## Support

For issues or questions, please refer to:
- Main documentation: `/doc/WIFI_SETTINGS.md`
- Project README: `/README.md`
