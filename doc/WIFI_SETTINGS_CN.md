# WiFi 网络连接设置页面

## 概述

此功能为 ROS2-Mobile-Android 应用程序提供了一个 WiFi 网络连接设置页面。它允许用户配置 WiFi 连接并在网络上发现 ROS 设备。

## 功能特性

### 1. WiFi 连接配置
- **WiFi 名称输入框**：用于输入 WiFi 网络名称（SSID）
- **WiFi 密码输入框**：带有显示/隐藏切换功能的安全密码输入框
- **记住密码**：复选框，用于保存 WiFi 凭据以供将来使用
- **连接按钮**："下一步 - 连接到 WiFi" 按钮，用于启动连接

### 2. 设备发现
WiFi 连接成功后，应用程序会自动：
- 扫描本地网络上的 ROS 设备
- 检查常用的 ROS 端口（11311 用于 ROS1 Master，9090 用于 rosbridge）
- 在列表中显示发现的设备及其 IP 地址

### 3. 保存的凭据
应用程序使用 SharedPreferences 存储：
- WiFi SSID（网络名称）
- WiFi 密码（如果选中了"记住密码"）
- 密码记住偏好设置

## 实现详情

### 新增文件

1. **WifiSettingsFragment.java**
   - 位置：`app/src/main/java/com/schneewittchen/rosandroid/ui/fragments/wifi/`
   - 用途：WiFi 设置页面的 UI 控制器

2. **WifiSettingsViewModel.java**
   - 位置：`app/src/main/java/com/schneewittchen/rosandroid/viewmodel/`
   - 用途：WiFi 连接和设备发现的业务逻辑

3. **DeviceListAdapter.java**
   - 位置：`app/src/main/java/com/schneewittchen/rosandroid/ui/fragments/wifi/`
   - 用途：用于显示已发现设备的 RecyclerView 适配器

4. **fragment_wifi_settings.xml**
   - 位置：`app/src/main/res/layout/`
   - 用途：WiFi 设置页面的布局文件

5. **item_discovered_device.xml**
   - 位置：`app/src/main/res/layout/`
   - 用途：列表中单个设备项的布局

### 导航

WiFi 设置片段已集成到应用的导航图中：
- 片段 ID：`wifiSettingsFragment`
- 操作 ID：`action_to_wifiSettingsFragment`

从任何片段导航到 WiFi 设置：
```java
navController.navigate(R.id.action_to_wifiSettingsFragment);
```

### 权限

已向 AndroidManifest.xml 添加以下权限：
- `CHANGE_WIFI_STATE` - 以编程方式连接到 WiFi 所需
- `CHANGE_NETWORK_STATE` - 网络配置所需

现有权限已涵盖：
- `ACCESS_WIFI_STATE` - 读取 WiFi 状态
- `ACCESS_NETWORK_STATE` - 读取网络状态
- `ACCESS_FINE_LOCATION` - Android 8.1+ 上的 WiFi 扫描所需
- `INTERNET` - 网络通信

## 使用说明

### 添加 WiFi 设置标签

要将 WiFi 设置标签添加到主导航，请修改 `fragment_main.xml`：

```xml
<com.google.android.material.tabs.TabItem
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="WiFi" />
```

并在 `MainFragment.java` 中添加导航处理：

```java
case "WiFi":
    navController.navigate(R.id.action_to_wifiSettingsFragment);
    break;
```

### 直接访问

从应用的任何部分直接打开 WiFi 设置：

```java
NavController navController = Navigation.findNavController(view);
navController.navigate(R.id.action_to_wifiSettingsFragment);
```

## Android 版本兼容性

### Android 10+ (API 29+)
- 应用无法以编程方式连接到 WiFi 网络
- 实现使用 `WifiNetworkSpecifier` 来建议网络
- 用户必须通过系统对话框批准连接

### Android 9 及以下 (API 28-)
- 传统方法尝试以编程方式进行 WiFi 连接
- 使用已弃用的 `WifiConfiguration` API

## 设备发现

设备发现过程：
1. 获取当前设备的 IP 地址
2. 提取网络前缀（例如 "192.168.1."）
3. 扫描子网中的前 20 个 IP 地址
4. 测试常用 ROS 端口的连接性：
   - 端口 11311：ROS1 Master
   - 端口 9090：rosbridge
5. 在列表中显示响应的设备

## 自定义

### 调整发现范围

要扫描更多 IP 地址，请修改 `WifiSettingsViewModel.java`：

```java
// 从 20 更改为所需范围
for (int i = 1; i <= 50; i++) {  // 现在扫描前 50 个地址
```

### 添加更多端口

要检查其他端口，请修改端口检查逻辑：

```java
if (Utils.isHostAvailable(testIp, 11311, 500) || 
    Utils.isHostAvailable(testIp, 9090, 500) ||
    Utils.isHostAvailable(testIp, YOUR_PORT, 500)) {
    devices.add(testIp);
}
```

## 字符串资源

所有面向用户的文本都在 `strings.xml` 中外部化：
- `wifi_settings_title` - 页面标题
- `wifi_settings_description` - 页面描述
- `wifi_ssid_hint` - SSID 字段提示
- `wifi_password_hint` - 密码字段提示
- `remember_password` - 记住密码复选框文本
- `wifi_connect_button` - 连接按钮文本
- `connecting_to_wifi` - 连接状态消息
- `wifi_connected` - 成功消息
- `wifi_connection_failed` - 失败消息
- `discovering_devices` - 设备发现状态
- `discovered_devices_title` - 已发现设备部分标题
- `no_devices_found` - 未找到设备消息

## 使用流程

1. 用户打开 WiFi 设置页面
2. 输入 WiFi 名称（SSID）
3. 输入 WiFi 密码
4. 可选择勾选"记住密码"
5. 点击"下一步 - 连接到 WiFi"按钮
6. 应用尝试连接到 WiFi（Android 10+ 会显示系统对话框）
7. 连接成功后，自动扫描网络上的 ROS 设备
8. 显示发现的设备列表
9. 用户可以点击设备进行选择

## 故障排除

### Android 10+ 上 WiFi 连接不工作

这是预期行为。Android 10+ 要求用户批准 WiFi 连接。应用将显示一个系统对话框供用户批准连接。

### 设备发现未返回设备

1. 确保设备已连接到 WiFi 网络
2. 检查 ROS 设备是否在网络上运行
3. 验证防火墙设置允许 ROS 端口（11311，9090）上的连接
4. 如果设备在较高的 IP 地址上，增加扫描范围
5. 如果您的 ROS 设置使用不同的端口，添加更多端口进行扫描

## 未来增强

潜在改进：
1. 支持 WPA3 和其他安全协议
2. 显示 WiFi 信号强度
3. 显示更多设备信息（主机名、MAC 地址）
4. 支持手动 IP 配置
5. 网络速度测试
6. 保存多个 WiFi 配置
7. 导入/导出 WiFi 设置
8. 二维码扫描 WiFi 凭据
