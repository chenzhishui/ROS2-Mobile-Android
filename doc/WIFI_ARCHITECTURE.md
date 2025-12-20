# WiFi Settings Architecture and Flow

## Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     MainActivity                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              MainFragment                             │  │
│  │  ┌──────────────────────────────────────────────┐     │  │
│  │  │        Navigation Controller                 │     │  │
│  │  │                                              │     │  │
│  │  │  ┌────────────────────────────────────┐     │     │  │
│  │  │  │   WifiSettingsFragment (UI)       │     │     │  │
│  │  │  │  - SSID Input                     │     │     │  │
│  │  │  │  - Password Input                 │     │     │  │
│  │  │  │  - Remember Password Checkbox     │     │     │  │
│  │  │  │  - Connect Button                 │     │     │  │
│  │  │  │  - Progress Indicator             │     │     │  │
│  │  │  │  - Status Text                    │     │     │  │
│  │  │  │  - Device List (RecyclerView)     │     │     │  │
│  │  │  └────────────┬───────────────────────┘     │     │  │
│  │  │               │                             │     │  │
│  │  │               │ observes LiveData           │     │  │
│  │  │               │                             │     │  │
│  │  │  ┌────────────▼───────────────────────┐     │     │  │
│  │  │  │   WifiSettingsViewModel           │     │     │  │
│  │  │  │  (Business Logic)                 │     │     │  │
│  │  │  │  - WiFi Connection Handler        │     │     │  │
│  │  │  │  - Device Discovery Engine        │     │     │  │
│  │  │  │  - Credentials Storage            │     │     │  │
│  │  │  │  - Connection Status LiveData     │     │     │  │
│  │  │  │  - Discovered Devices LiveData    │     │     │  │
│  │  │  └────────────┬───────────────────────┘     │     │  │
│  │  │               │                             │     │  │
│  │  │               │ uses                        │     │  │
│  │  │               │                             │     │  │
│  │  │  ┌────────────▼───────────────────────┐     │     │  │
│  │  │  │   System Services                 │     │     │  │
│  │  │  │  - WifiManager                    │     │     │  │
│  │  │  │  - ConnectivityManager            │     │     │  │
│  │  │  │  - SharedPreferences              │     │     │  │
│  │  │  └───────────────────────────────────┘     │     │  │
│  │  │                                              │     │  │
│  │  │  ┌────────────────────────────────────┐     │     │  │
│  │  │  │   DeviceListAdapter               │     │     │  │
│  │  │  │  (RecyclerView Adapter)           │     │     │  │
│  │  │  │  - Displays Discovered Devices    │     │     │  │
│  │  │  │  - Device Click Listener          │     │     │  │
│  │  │  └───────────────────────────────────┘     │     │  │
│  │  └──────────────────────────────────────────────┘     │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## User Flow Diagram

```
┌─────────────┐
│   Start     │
│   App       │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ Navigate to     │
│ WiFi Settings   │
│ Fragment        │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Enter WiFi      │
│ Credentials     │
│ - SSID          │
│ - Password      │
│ - Remember?     │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Click "Next"    │
│ Button          │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────────┐
│ Save            │      │ Android 10+      │
│ Credentials     │      │ Show System      │
│ to SharedPrefs  │      │ Dialog           │
└──────┬──────────┘      └────────┬─────────┘
       │                          │
       ├──────────────────────────┤
       │                          │
       ▼                          ▼
┌─────────────────┐      ┌──────────────────┐
│ Connect to      │      │ User Approves    │
│ WiFi Network    │      │ Connection       │
└──────┬──────────┘      └────────┬─────────┘
       │                          │
       └──────────┬───────────────┘
                  │
                  ▼
         ┌────────────────┐
         │ Connection     │
         │ Successful?    │
         └────┬──────┬────┘
              │      │
        Yes   │      │   No
              │      │
              ▼      ▼
      ┌───────────┐ ┌────────────────┐
      │ Start     │ │ Show Error     │
      │ Device    │ │ Message        │
      │ Discovery │ └────────────────┘
      └─────┬─────┘
            │
            ▼
    ┌───────────────────┐
    │ Scan Network      │
    │ - Get Device IP   │
    │ - Extract Subnet  │
    │ - Test IPs 1-20   │
    │ - Check Ports:    │
    │   * 11311 (ROS1)  │
    │   * 9090 (bridge) │
    └─────┬─────────────┘
          │
          ▼
    ┌───────────────────┐
    │ Display           │
    │ Discovered        │
    │ Devices           │
    │ in List           │
    └─────┬─────────────┘
          │
          ▼
    ┌───────────────────┐
    │ User Selects      │
    │ Device            │
    │ (Optional)        │
    └───────────────────┘
```

## Data Flow

```
User Input
    ↓
Fragment (UI Layer)
    │
    ├── getText() from EditText
    ├── isChecked() from CheckBox
    │
    ↓
ViewModel (Business Logic)
    │
    ├── saveWifiCredentials()
    │   └→ SharedPreferences
    │
    ├── connectToWifi()
    │   ├→ WifiManager (Android 9-)
    │   └→ WifiNetworkSpecifier (Android 10+)
    │
    ├── discoverDevices()
    │   ├→ Utils.getIPAddress()
    │   ├→ Utils.isHostAvailable()
    │   └→ Update discoveredDevices LiveData
    │
    └── LiveData Updates
        ├→ connectionStatus
        └→ discoveredDevices
            ↓
Fragment (UI Layer)
    │
    ├── observe(connectionStatus)
    │   └→ Update UI (progress, status text)
    │
    └── observe(discoveredDevices)
        └→ Update RecyclerView
            └→ DeviceListAdapter
                └→ Display device list
```

## State Diagram

```
┌──────────────┐
│    IDLE      │ ◄─────────────┐
└──────┬───────┘                │
       │                        │
       │ Click Connect          │
       │                        │
       ▼                        │
┌──────────────┐                │
│ CONNECTING   │                │
└──────┬───────┘                │
       │                        │
       │                        │
       ├─────────┬──────────────┤
       │         │              │
  Success        │           Failure
       │         │              │
       ▼         ▼              │
┌──────────┐  ┌─────────┐      │
│CONNECTED │  │ FAILED  │──────┘
└────┬─────┘  └─────────┘
     │
     │ Start Discovery
     │
     ▼
┌──────────────────┐
│ DISCOVERING      │
│ (still CONNECTED)│
└────┬─────────────┘
     │
     │ Discovery Complete
     │
     ▼
┌──────────────────┐
│ CONNECTED        │
│ (with device     │
│  list)           │
└──────────────────┘
```

## File Dependencies

```
WifiSettingsFragment.java
    │
    ├── Uses: R.layout.fragment_wifi_settings
    │   └── fragment_wifi_settings.xml
    │       └── Uses: item_discovered_device.xml
    │
    ├── Uses: WifiSettingsViewModel
    │   └── WifiSettingsViewModel.java
    │       ├── Uses: WifiManager (Android API)
    │       ├── Uses: ConnectivityManager (Android API)
    │       ├── Uses: SharedPreferences (Android API)
    │       └── Uses: Utils.java
    │
    ├── Uses: DeviceListAdapter
    │   └── DeviceListAdapter.java
    │       └── Uses: R.layout.item_discovered_device
    │
    └── Uses: String Resources
        └── strings.xml
            └── WiFi-related strings
```

## Navigation Integration

```
main_navigation.xml
    │
    ├── Fragment: wifiSettingsFragment
    │   ├── name: WifiSettingsFragment
    │   └── layout: fragment_wifi_settings
    │
    └── Action: action_to_wifiSettingsFragment
        └── destination: wifiSettingsFragment

MainFragment.java
    │
    └── Tab Selection Listener
        └── case "WiFi":
            └── navController.navigate(R.id.action_to_wifiSettingsFragment)
```

## Permission Flow

```
AndroidManifest.xml
    │
    ├── CHANGE_WIFI_STATE ──────┐
    ├── CHANGE_NETWORK_STATE ───┤
    ├── ACCESS_WIFI_STATE ──────┤
    ├── ACCESS_NETWORK_STATE ───┤
    ├── ACCESS_FINE_LOCATION ───┤
    └── INTERNET ───────────────┤
                                │
                                ▼
                    WifiSettingsViewModel
                                │
                                ├── WiFi Connection
                                ├── Network Discovery
                                └── Device Scanning
```

## Threading Model

```
Main Thread (UI Thread)
    │
    ├── Fragment
    │   ├── UI Updates
    │   └── LiveData Observation
    │
    └── ViewModel
        └── LiveData.postValue()

Background Threads
    │
    └── ViewModel
        ├── WiFi Connection (async)
        ├── Device Discovery
        │   └── new Thread() {
        │       ├── Network Scanning
        │       ├── Port Testing
        │       └── LiveData.postValue()
        │   }
        └── NetworkCallback
            └── onAvailable() / onUnavailable()
```

## Key Design Patterns Used

1. **MVVM (Model-View-ViewModel)**
   - Fragment = View
   - ViewModel = Business Logic
   - LiveData = Observable Data

2. **Observer Pattern**
   - LiveData observes changes
   - Fragment updates UI automatically

3. **Repository Pattern**
   - SharedPreferences for data persistence
   - ViewModel acts as repository

4. **Adapter Pattern**
   - DeviceListAdapter for RecyclerView

5. **Singleton Pattern**
   - ViewModel scoped to Fragment lifecycle

## Error Handling

```
Try-Catch Blocks
    │
    ├── WiFi Connection Errors
    │   └── Post FAILED status
    │
    ├── Network Discovery Errors
    │   └── Return empty list
    │
    └── Permission Errors
        └── Runtime permission checks
```

This architecture ensures:
- ✅ Clean separation of concerns
- ✅ Testability
- ✅ Maintainability
- ✅ Scalability
- ✅ Android best practices
