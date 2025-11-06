# Test App (Flutter)

Một app Flutter cơ bản để test trên Android.

## Mục tiêu
- Cung cấp `lib/main.dart` và `pubspec.yaml` để bạn có thể nhanh chóng chạy app trên thiết bị/emulator Android.

## Hướng dẫn nhanh
1. Mở terminal và chuyển đến thư mục dự án:

```bash
cd /Users/macbook/Documents/Code/Mobile/Flutter/Project/test-app
```

2. Nếu bạn chưa có thư mục nền tảng (`android/`), tạo chúng bằng `flutter create .`:

```bash
flutter create .
```

3. Lấy dependencies:

```bash
flutter pub get
```

4. Chạy app trên thiết bị/emulator Android:

```bash
# liệt kê thiết bị
flutter devices
# chạy trên thiết bị hoặc emulator (thay <deviceId> nếu cần)
flutter run
```

## Ghi chú
- Cần cài đặt Flutter SDK và thiết lập Android SDK/Emulator trên máy của bạn.
- Nếu `flutter` chưa được cài, xem: https://flutter.dev/docs/get-started/install
