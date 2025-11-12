# 🌿 AR Demo App (Android + Node.js)

Dự án demo ứng dụng **AR (Augmented Reality)** hiển thị mô hình 3D trên màn hình điện thoại Android.  
App được viết bằng **Java (Android Studio)** và **Node.js (Express)** làm server.

---

## 📱 Giới thiệu

Ứng dụng cho phép người dùng:
1. Kết nối tới server Node.js để lấy đường dẫn mô hình 3D (.glb)
2. Hiển thị mô hình đó trong không gian AR (sử dụng camera)
3. Tương tác (xoay, phóng to, thu nhỏ) mô hình trong môi trường ảo

---

## 🧩 Công nghệ sử dụng

### 🖥 Android App
- **Ngôn ngữ:** Java  
- **Công cụ:** Android Studio  
- **Thư viện chính:**
  - `com.google.ar:core` — ARCore (AR engine của Google)
  - `com.gorisse.thomas.sceneform:ux` — Sceneform Community (hiển thị mô hình 3D)
  - `com.squareup.retrofit2` — Retrofit (kết nối REST API)
  - `com.squareup.okhttp3:logging-interceptor` — log request/response

### 🌐 Node.js Server
- **Ngôn ngữ:** JavaScript  
- **Thư viện:** Express, CORS, Path  
- **Nhiệm vụ:** phục vụ file `.glb` và trả về JSON chứa URL model

---

## 🗂 Cấu trúc dự án

