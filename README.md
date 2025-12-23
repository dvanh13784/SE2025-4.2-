# 📱 Ứng dụng Thực tế tăng cường (AR) Android  
## Kiến trúc Client–Server – Hiển thị mô hình 3D (.glb) bằng ARCore

**Môn học:** Công nghệ phần mềm
**Đề tài:** Xây dựng ứng dụng AR Android hiển thị mô hình 3D từ Server  
**Kiến trúc:** Client–Server  
**Công nghệ:** Android • ARCore • Sceneform • Node.js • Nginx  

---

## 📑 Mục lục
1. [Tổng quan đề tài](#1-tổng-quan-đề-tài)  
2. [Goals – Mục tiêu tổng quát](#2-goals--mục-tiêu-tổng-quát)  
3. [Objectives – Mục tiêu chi tiết](#3-objectives--mục-tiêu-chi-tiết)  
4. [Kiến trúc hệ thống](#4-kiến-trúc-hệ-thống)  
5. [Môi trường phát triển & kiểm thử](#5-môi-trường-phát-triển--kiểm-thử)  
6. [Vấn đề gặp phải & cách giải quyết](#6-vấn-đề-gặp-phải--cách-giải-quyết)  
7. [Kết quả đạt được](#7-kết-quả-đạt-được)  
8. [Hướng phát triển tương lai](#8-hướng-phát-triển-tương-lai)  
9. [Kết luận](#9-kết-luận)  

---

## 1. Tổng quan đề tài

Đề tài tập trung xây dựng một hệ thống **Thực tế tăng cường (Augmented Reality – AR)** theo kiến trúc **Client–Server**, trong đó:

- **Server** chịu trách nhiệm lưu trữ, quản lý và phân phối các mô hình 3D định dạng `.glb / .gltf`
- **Ứng dụng Android** đóng vai trò client, kết nối đến server để tải mô hình và hiển thị chúng trong không gian thực thông qua camera bằng **ARCore**

### 🎯 Ứng dụng hướng đến
- Trưng bày sản phẩm bằng AR  
- Thiết kế nội thất AR  
- Giáo dục – mô phỏng 3D  
- Triển lãm và demo công nghệ  

---

## 2. Goals – Mục tiêu tổng quát

### 🎯 Mục tiêu chung

Xây dựng thành công một ứng dụng AR Android hoàn chỉnh, trong đó:

- Mô hình 3D **không nhúng cứng** trong ứng dụng
- Dữ liệu được **quản lý tập trung trên server**
- Ứng dụng có thể **tải và hiển thị mô hình động** trong không gian thực

### ⚙️ Mục tiêu kỹ thuật

- Thiết kế và triển khai **server backend**
- Phát triển **ứng dụng Android AR** sử dụng ARCore
- Kết nối **Android ↔ Server** thông qua HTTP REST API
- Triển khai và kiểm thử trên **server thật (public)**

---

## 3. Objectives – Mục tiêu chi tiết

### 3.1 🖥️ Backend Server (Node.js)

Backend Server đóng vai trò **trung tâm quản lý toàn bộ mô hình 3D** trong hệ thống AR Client–Server.  
Server không chỉ cung cấp API cho ứng dụng Android, mà còn triển khai **giao diện Web quản trị (AR Cloud Manager)** để thao tác trực tiếp với dữ liệu mô hình.

---

#### 🎯 Vai trò của Backend trong hệ thống

- Lưu trữ tập trung các mô hình 3D (.glb / .gltf)
- Phân phối mô hình cho ứng dụng Android thông qua HTTP
- Cho phép upload / quản lý model **không cần build lại app**
- Đóng vai trò **AR Cloud Server** trong kiến trúc Client–Server

---

#### 🧱 Công nghệ sử dụng

- **Node.js + Express**: xây dựng server backend
- **Multer**: xử lý upload file 3D
- **Filesystem (fs)**: quản lý file vật lý
- **Nginx**: reverse proxy, public server ra Internet
- **CORS**: cho phép Android client truy cập API
- **Linux Server**: triển khai thực tế với IP public

---

#### 🌐 Giao diện Web quản lý – AR Cloud Manager

Server cung cấp một **trang web quản lý trực quan**, truy cập trực tiếp qua trình duyệt:
http://136.111.208.187

<img width="1763" height="919" alt="image" src="https://github.com/user-attachments/assets/49db8b29-8c36-4f71-b6bc-5667aa3c7210" />

---

### 3.2 📱 Ứng dụng Android AR

Ứng dụng Android đóng vai trò **client AR**, chịu trách nhiệm hiển thị và tương tác với mô hình 3D.

#### ✔ Các chức năng đã hoàn thành
- Kết nối server, lấy danh sách model
- Hiển thị danh sách model cho người dùng chọn
- Tải model từ server bằng URL
- Nhận diện mặt phẳng (plane detection)
- Đặt model trong không gian thực
- Đặt **nhiều model** trong một phiên

#### 🖐 Tương tác người dùng
- Di chuyển model  
- Xoay model  
- Phóng to / thu nhỏ  

#### 🔧 Khác
- Xử lý lỗi khi tải model
- Hỗ trợ HTTP (`usesCleartextTraffic`)
- Kiểm thử trên **thiết bị thật**

---

### 3.3 🧠 Tương tác AR

#### ✔ Đã triển khai
- Hướng dẫn quét mặt phẳng
- Đặt model bằng thao tác chạm
- Điều chỉnh kích thước model

#### 🚀 Có thể mở rộng
- Chọn / xoá model
- Thay thế model
- Xem trước model
- Đo khoảng cách
- Chụp ảnh / quay video AR
- Lưu & khôi phục AR Scene

---

## 4. Kiến trúc hệ thống

### 🧩 Sơ đồ kiến trúc

Android AR App
|
| HTTP REST API
v
Nginx (Port 80)
|
| Reverse Proxy
v
Node.js Server (Port 3000)
|
v
uploads/ (.glb files)


---

## 5. Môi trường phát triển & kiểm thử

### 🖥️ Server
- OS: Linux
- Backend: Node.js + Express
- Reverse Proxy: Nginx
- IP public: `136.111.208.187`

### 📱 Android
- Thiết bị: Xiaomi Redmi Note 11S
- Android: 13 (Tiramisu)
- IDE: Android Studio
- Ngôn ngữ: Java + XML
- AR Framework: ARCore + Sceneform

---

## 6. Vấn đề gặp phải & cách giải quyết

| Vấn đề | Nguyên nhân | Giải pháp |
|------|------------|----------|
| App crash | Xung đột ARCore – Sceneform | Tắt Light Estimation |
| Upload lỗi | Giới hạn Nginx | Cấu hình `client_max_body_size` |
| App không load server | Sai port / IP | Dùng Nginx + Port 80 |

---

## 7. Kết quả đạt được

- ✅ Hoàn thiện hệ thống AR Client–Server  
- ✅ Load mô hình 3D từ server thật  
- ✅ Hiển thị mô hình trong không gian thực  
- ✅ Tương tác mượt mà  
- ✅ Kiến trúc rõ ràng, dễ mở rộng  

---

## 8. Hướng phát triển tương lai

- Metadata cho model (scale, rotation)
- Cache model
- Lưu trạng thái AR scene
- Xác thực người dùng
- HTTPS
- Tối ưu hiệu năng

---

## 9. Kết luận

Đề tài đã triển khai thành công một ứng dụng **AR Android theo kiến trúc Client–Server**, kết hợp giữa **ARCore, Android và Backend Server**.  
Hệ thống đáp ứng đầy đủ yêu cầu học thuật và có khả năng mở rộng cho các ứng dụng AR thực tế.

---

### 📌 Ghi chú
Dự án được phát triển và kiểm thử trên **thiết bị thật** và **server public**, đảm bảo tính thực tiễn và ổn định.

