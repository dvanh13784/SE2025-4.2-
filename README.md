🌿 AR Demo App (Android + Node.js)

Đây là một ứng dụng demo AR (Augmented Reality – Thực tế tăng cường) cho phép hiển thị mô hình 3D trên màn hình điện thoại Android.
Ứng dụng được xây dựng bằng Java (Android Studio) và một server backend sử dụng Node.js (Express).

📱 Giới thiệu

Ứng dụng hỗ trợ người dùng:

Kết nối đến server Node.js để lấy URL của mô hình 3D (.glb)

Hiển thị mô hình đó trong không gian AR thông qua camera

Tương tác với mô hình: xoay, phóng to, thu nhỏ ngay trong môi trường thực

🧩 Công nghệ sử dụng
🖥 Android App

Ngôn ngữ: Java

Công cụ phát triển: Android Studio

Các thư viện chính:

com.google.ar:core — Google ARCore, nền tảng AR của Google

com.gorisse.thomas.sceneform:ux — Sceneform Community dùng để hiển thị mô hình 3D

com.squareup.retrofit2 — Retrofit để gọi REST API

com.squareup.okhttp3:logging-interceptor — hỗ trợ log request và response

🌐 Node.js Server

Ngôn ngữ: JavaScript

Thư viện: Express, CORS, Path

Chức năng: cung cấp file .glb và trả về JSON chứa URL của mô hình để app tải về

⚙️ Cài đặt & chạy
🔹 1. Chuẩn bị môi trường

Cài Node.js tại: https://nodejs.org

Cài Android Studio

Tạo Android Emulator có hỗ trợ Google Play Services for AR

🔹 2. Cài đặt & chạy server Node.js
cd server
npm init -y
npm install express cors
node server.js

# Server chạy tại:
http://localhost:3000
