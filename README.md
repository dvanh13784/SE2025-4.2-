# 📱 Ứng dụng Thực tế tăng cường (AR) Android  
## Kiến trúc Client–Server – Hiển thị mô hình 3D (.glb) bằng ARCore

**Môn học:** Công nghệ phần mềm / Thực tế tăng cường  
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

Backend đóng vai trò **trung tâm quản lý mô hình 3D**.

#### ✔ Các chức năng đã thực hiện
- Server xây dựng bằng **Node.js + Express**
- Cung cấp REST API:
  - `GET /api/models` – Lấy danh sách model
  - `POST /upload` – Upload model `.glb / .gltf`
- Phục vụ file 3D qua HTTP
- Hỗ trợ **CORS**
- Lưu trữ model trong thư mục `uploads/`
- Tránh ghi đè file bằng **timestamp**
- Tích hợp **Nginx reverse proxy**
- Mở cổng và cấu hình firewall

#### 🌐 Server public
http://136.111.208.187
<img width="1763" height="919" alt="image" src="https://github.com/user-attachments/assets/49db8b29-8c36-4f71-b6bc-5667aa3c7210" />

