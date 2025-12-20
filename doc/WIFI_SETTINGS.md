# WiFi Connection Settings Page

## Overview

This feature provides a WiFi connection settings page for the ROS2-Mobile-Android application. It allows users to configure WiFi connections and discover ROS devices on the network.

## Features

### 1. WiFi Connection Configuration
- **WiFi SSID Input**: Text field to enter the WiFi network name
- **WiFi Password Input**: Secure password field with show/hide toggle
- **Remember Password**: Checkbox to save WiFi credentials for future use
- **Connect Button**: "Next - Connect to WiFi" button to initiate connection

### 2. Device Discovery
After a successful WiFi connection, the application automatically:
- Scans the local network for ROS devices
- Checks common ROS ports (11311 for ROS1 Master, 9090 for rosbridge)
- Displays discovered devices in a list with their IP addresses

### 3. Saved Credentials
The application uses SharedPreferences to store:
- WiFi SSID
- WiFi password (if "Remember Password" is checked)
- Password remember preference

## Implementation Details

### Files Added

1. **WifiSettingsFragment.java**
   - Location: `app/src/main/java/com/schneewittchen/rosandroid/ui/fragments/wifi/`
   - Purpose: UI controller for WiFi settings page

2. **WifiSettingsViewModel.java**
   - Location: `app/src/main/java/com/schneewittchen/rosandroid/viewmodel/`
   - Purpose: Business logic for WiFi connection and device discovery

3. **DeviceListAdapter.java**
   - Location: `app/src/main/java/com/schneewittchen/rosandroid/ui/fragments/wifi/`
   - Purpose: RecyclerView adapter for displaying discovered devices

4. **fragment_wifi_settings.xml**
   - Location: `app/src/main/res/layout/`
   - Purpose: Layout for WiFi settings page

5. **item_discovered_device.xml**
   - Location: `app/src/main/res/layout/`
   - Purpose: Layout for individual device items in the list

### Navigation

The WiFi settings fragment has been integrated into the app's navigation graph:
- Fragment ID: `wifiSettingsFragment`
- Action ID: `action_to_wifiSettingsFragment`

To navigate to WiFi settings from any fragment:
```java
navController.navigate(R.id.action_to_wifiSettingsFragment);
```

### Permissions

The following permissions were added to AndroidManifest.xml:
- `CHANGE_WIFI_STATE` - Required to programmatically connect to WiFi
- `CHANGE_NETWORK_STATE` - Required for network configuration

Existing permissions already cover:
- `ACCESS_WIFI_STATE` - Read WiFi state
- `ACCESS_NETWORK_STATE` - Read network state
- `ACCESS_FINE_LOCATION` - Required for WiFi scanning on Android 8.1+
- `INTERNET` - Network communication

## Usage Instructions

### Adding WiFi Settings Tab

To add a WiFi Settings tab to the main navigation, modify `MainFragment.java`:

```java
// In onViewCreated method, add a case in the tab selection listener:
case "WiFi":
    navController.navigate(R.id.action_to_wifiSettingsFragment);
    break;
```

### Direct Access

To directly open WiFi settings from any part of the app:

```java
NavController navController = Navigation.findNavController(view);
navController.navigate(R.id.action_to_wifiSettingsFragment);
```

## Android Version Compatibility

### Android 10+ (API 29+)
- Apps cannot programmatically connect to WiFi networks
- The implementation uses `WifiNetworkSpecifier` to suggest a network
- Users must approve the connection through a system dialog

### Android 9 and below (API 28-)
- Legacy method attempts programmatic WiFi connection
- Uses deprecated `WifiConfiguration` API

## Device Discovery

The device discovery process:
1. Retrieves the current device IP address
2. Extracts the network prefix (e.g., "192.168.1.")
3. Scans the first 20 IP addresses in the subnet
4. Tests connectivity on common ROS ports:
   - Port 11311: ROS1 Master
   - Port 9090: rosbridge
5. Displays responsive devices in a list

## Customization

### Adjusting Discovery Range

To scan more IP addresses, modify `WifiSettingsViewModel.java`:

```java
// Change from 20 to your desired range
for (int i = 1; i <= 20; i++) {
```

### Adding More Ports

To check additional ports, modify the port checking logic:

```java
if (Utils.isHostAvailable(testIp, 11311, 500) || 
    Utils.isHostAvailable(testIp, 9090, 500) ||
    Utils.isHostAvailable(testIp, YOUR_PORT, 500)) {
    devices.add(testIp);
}
```

## String Resources

All user-facing text is externalized in `strings.xml`:
- `wifi_settings_title` - Page title
- `wifi_settings_description` - Page description
- `wifi_ssid_hint` - SSID field hint
- `wifi_password_hint` - Password field hint
- `remember_password` - Remember password checkbox text
- `wifi_connect_button` - Connect button text
- `connecting_to_wifi` - Connecting status message
- `wifi_connected` - Success message
- `wifi_connection_failed` - Failure message
- `discovering_devices` - Device discovery status
- `discovered_devices_title` - Discovered devices section title
- `no_devices_found` - No devices found message

## Future Enhancements

Potential improvements for future versions:
1. Support for WPA3 and other security protocols
2. Show WiFi signal strength
3. Display more device information (hostname, MAC address)
4. Support for manual IP configuration
5. Network speed testing
6. Save multiple WiFi configurations
7. Import/export WiFi settings
8. QR code scanning for WiFi credentials
