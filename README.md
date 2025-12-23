# 📱 Ứng dụng Thực tế tăng cường (AR) Android  
## Kiến trúc Client–Server – Hiển thị mô hình 3D (.glb) bằng ARCore

**Nhóm:** SE2025-4.2-

**Thành Viên:** 
1. Nguyễn Hoàng Anh - 22001539
2. Đỗ Việt Anh - 22001536

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

##### Chức năng giao diện Web:
- Kéo thả file `.glb / .gltf` để upload
- Chọn file từ máy tính
- Hiển thị:
  - Tên file
  - Kích thước
  - Thời điểm upload
- Upload **nhiều model cùng lúc**
- Kiểm soát dữ liệu server **không cần SSH**

👉 Giao diện này đóng vai trò **Admin Panel đơn giản** cho AR Server.

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
- Kiểm thử trên **thiết bị thật:  Xiaomi Redmi Note 11S**

---

### 3.3 🧠 Tương tác AR

#### ✔ Đã triển khai
- Hướng dẫn quét mặt phẳng
- Đặt model bằng thao tác chạm
- Điều chỉnh kích thước model
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

Sau quá trình phân tích, thiết kế, triển khai và kiểm thử, đề tài đã **hoàn thiện thành công toàn bộ hệ thống AR theo kiến trúc Client–Server**, đáp ứng đầy đủ các mục tiêu đã đề ra ban đầu.

### 7.1 Hoàn thiện hệ thống AR Client–Server

Hệ thống được xây dựng theo mô hình **Client–Server rõ ràng**, trong đó:

- **Server (Backend)**:
  - Chịu trách nhiệm lưu trữ và quản lý tập trung các mô hình 3D (.glb, .gltf)
  - Cung cấp API REST để client truy cập
  - Phục vụ file 3D qua HTTP
  - Hoạt động trên **server thật (public IP)**, không phụ thuộc môi trường local

- **Client (Android AR App)**:
  - Không nhúng cứng mô hình 3D trong ứng dụng
  - Kết nối động đến server để lấy dữ liệu
  - Tải model theo thời gian thực và hiển thị bằng ARCore

👉 Kết quả:  
Hệ thống hoạt động ổn định, phân tách rõ ràng giữa **xử lý dữ liệu (server)** và **hiển thị/ tương tác (client)**, đúng với mô hình kiến trúc phần mềm hiện đại.

---

### 7.2 Load mô hình 3D từ server thật (Public Server)

Một trong những kết quả quan trọng nhất của đề tài là:

- Ứng dụng Android **tải mô hình 3D trực tiếp từ server public**
- Server được triển khai tại: http://136.111.208.187
- Dữ liệu model không còn phụ thuộc vào:
- Local machine
- Tài nguyên build sẵn trong APK

Cụ thể:
- Server trả về danh sách model qua API `/api/models`
- Mỗi model có URL public
- Ứng dụng Android dùng URL đó để tải model khi người dùng lựa chọn

👉 Điều này chứng minh:
- Hệ thống có khả năng **mở rộng quy mô**
- Có thể thay đổi / cập nhật model **mà không cần build lại ứng dụng**

---

### 7.3 Hiển thị mô hình 3D trong không gian thực (AR)

Ứng dụng đã hiển thị thành công mô hình 3D trong **môi trường thực tế thông qua camera**, nhờ vào:

- ARCore (Google)
- Sceneform (Filament)

Các chức năng AR đã hoạt động chính xác:
- Nhận diện mặt phẳng (plane detection)
- Đặt model tại vị trí người dùng chạm
- Hiển thị model với tỷ lệ phù hợp
- Cho phép đặt **nhiều model trong cùng một phiên AR**

👉 Kết quả:
- Mô hình được gắn chính xác vào không gian thật
- Không bị trôi vị trí khi người dùng di chuyển
- Trải nghiệm AR ổn định trên thiết bị thật

---

### 7.4 Tương tác mượt mà với mô hình 3D

Ứng dụng hỗ trợ đầy đủ các thao tác AR cơ bản:

- 🖐 **Di chuyển (Move)**: kéo model trong không gian
- 🔄 **Xoay (Rotate)**: xoay model theo trục
- 🔍 **Phóng to / Thu nhỏ (Scale)**: pinch để zoom

Các tương tác này được xử lý thông qua:
- `TransformableNode`
- `TransformationSystem` của Sceneform

👉 Kết quả:
- Tương tác mượt mà, phản hồi nhanh
- Không xảy ra giật lag đáng kể
- Trải nghiệm người dùng trực quan, dễ sử dụng

---

### 7.5 Kiến trúc rõ ràng, dễ mở rộng

Toàn bộ hệ thống được xây dựng với tư duy **dễ bảo trì – dễ mở rộng**, thể hiện qua:

- API tách biệt rõ ràng
- Client không phụ thuộc vào logic server
- Có thể:
- thêm model mới
- thay đổi giao diện
- mở rộng tính năng AR
- nâng cấp bảo mật

👉 Đây là nền tảng tốt cho các dự án AR quy mô lớn hơn trong tương lai.
---

## 8. Hướng phát triển tương lai

Mặc dù hệ thống đã hoạt động ổn định, vẫn còn nhiều hướng phát triển để nâng cao giá trị ứng dụng và tăng độ hoàn thiện.

### 8.1 Metadata cho mô hình 3D

- Lưu thêm thông tin cho mỗi model:
- scale mặc định
- rotation mặc định
- mô tả, danh mục
- Metadata có thể được:
- lưu trong database
- trả về kèm API `/api/models`

👉 Giúp hiển thị model đúng tỷ lệ và ngữ cảnh hơn.

---

### 8.2 Cache mô hình 3D

- Lưu model đã tải về trong bộ nhớ thiết bị
- Tránh tải lại khi:
- người dùng chọn lại model
- mất kết nối mạng

👉 Cải thiện hiệu năng và trải nghiệm người dùng.

---

### 8.3 Lưu và khôi phục trạng thái AR Scene

- Lưu danh sách:
- model đã đặt
- vị trí
- rotation
- scale
- Khôi phục scene khi mở lại ứng dụng

👉 Phù hợp cho:
- thiết kế nội thất
- trình diễn sản phẩm
- demo dài hạn

---

### 8.4 Xác thực người dùng (Authentication)

- Thêm đăng nhập:
- admin upload model
- user chỉ xem
- Phân quyền:
- upload / xoá / xem

👉 Tăng tính bảo mật và quản lý hệ thống.

---

### 8.5 HTTPS và bảo mật

- Triển khai HTTPS (SSL)
- Bảo vệ dữ liệu truyền tải
- Phù hợp với yêu cầu sản phẩm thực tế

---

### 8.6 Tối ưu hiệu năng AR

- Giảm dung lượng model
- Tối ưu texture
- Quản lý bộ nhớ khi hiển thị nhiều model

👉 Giúp ứng dụng chạy mượt trên nhiều thiết bị hơn.

---
## 9. Kết luận

Đề tài đã **triển khai thành công một ứng dụng Thực tế tăng cường (AR) trên Android theo kiến trúc Client–Server**, kết hợp hiệu quả giữa:

- **Android**
- **ARCore & Sceneform**
- **Backend Server (Node.js + Nginx)**

Hệ thống:
- Hoạt động ổn định trên **server thật**
- Hiển thị chính xác mô hình 3D trong không gian thực
- Hỗ trợ tương tác AR mượt mà
- Có kiến trúc rõ ràng, dễ mở rộng

👉 Đề tài **đáp ứng đầy đủ yêu cầu học thuật**, đồng thời có tính **ứng dụng thực tiễn cao**, có thể tiếp tục phát triển thành sản phẩm AR hoàn chỉnh trong các lĩnh vực:
- giáo dục
- trưng bày sản phẩm
- thiết kế
- triển lãm công nghệ

---

