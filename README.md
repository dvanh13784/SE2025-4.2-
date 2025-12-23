📱 Ứng dụng Thực tế tăng cường (AR) Android theo mô hình Client–Server

Hiển thị mô hình 3D (.glb) trong không gian thực bằng ARCore

1. Tổng quan đề tài

Đề tài tập trung xây dựng một hệ thống Thực tế tăng cường (Augmented Reality – AR) theo kiến trúc Client–Server, trong đó:

Server chịu trách nhiệm lưu trữ, quản lý và phân phối các mô hình 3D định dạng .glb

Ứng dụng Android đóng vai trò client, kết nối đến server để tải mô hình và hiển thị chúng trong không gian thực thông qua camera bằng ARCore

Hệ thống cho phép người dùng:

Tải mô hình 3D từ server

Đặt mô hình vào môi trường thực

Tương tác trực tiếp với mô hình (di chuyển, xoay, phóng to/thu nhỏ)

Đây là nền tảng cho các ứng dụng AR thực tế như:

Trưng bày sản phẩm AR

Thiết kế nội thất AR

Giáo dục – mô phỏng 3D

Triển lãm và demo công nghệ AR

2. Goals – Mục tiêu tổng quát
2.1 Mục tiêu chung

Xây dựng thành công một ứng dụng AR Android hoàn chỉnh hoạt động theo mô hình Client–Server, trong đó:

Mô hình 3D không được nhúng cứng trong app

Dữ liệu 3D được quản lý tập trung trên server

Ứng dụng Android có thể tải và hiển thị mô hình động trong không gian thực

2.2 Mục tiêu kỹ thuật

Thiết kế và triển khai server backend cung cấp API và file 3D

Phát triển ứng dụng Android AR sử dụng ARCore

Kết nối Android app với server qua HTTP

Hiển thị và tương tác mô hình 3D trong môi trường thực

Triển khai hệ thống trên server thật (public server), không chỉ chạy local

3. Objectives – Mục tiêu chi tiết
3.1 Backend Server (Node.js)

Backend đóng vai trò trung tâm quản lý mô hình 3D.

Các mục tiêu đã thực hiện:

Xây dựng server bằng Node.js + Express

Cung cấp các API REST:

GET /api/models: trả về danh sách mô hình 3D

POST /upload: upload file .glb / .gltf lên server

Phục vụ file mô hình 3D thông qua HTTP

Hỗ trợ CORS cho Android client

Lưu file mô hình trong thư mục uploads/

Tránh ghi đè file bằng cơ chế timestamp

Tích hợp Nginx reverse proxy

Mở cổng và cấu hình firewall cho server public

Cho phép upload và download mô hình từ Internet

Kết quả:

Server truy cập công khai tại:

http://136.111.208.187


Android app có thể tải model từ server thật

3.2 Ứng dụng Android AR

Ứng dụng Android là client AR, chịu trách nhiệm hiển thị và tương tác.

Mục tiêu chính:

Sử dụng ARCore để nhận diện mặt phẳng

Render mô hình .glb bằng Sceneform

Tải mô hình động từ server thông qua URL

Đặt mô hình vào không gian thực

Các chức năng đã thực hiện:

Gọi API server để lấy danh sách model

Hiển thị danh sách model cho người dùng chọn

Tải mô hình từ server khi người dùng chọn

Chạm mặt phẳng để đặt mô hình

Đặt nhiều mô hình trong một phiên AR

Tương tác với mô hình:

Di chuyển (drag)

Xoay (rotate)

Phóng to / thu nhỏ (scale)

Xử lý lỗi tải mô hình

Hỗ trợ HTTP trên Android 9+ (usesCleartextTraffic)

Kiểm thử trên thiết bị thật (Xiaomi Redmi Note 11S)

3.3 Mục tiêu về tương tác AR

Nhằm tạo trải nghiệm AR tự nhiên và trực quan.

Các tương tác đã có:

Nhận diện mặt phẳng (plane detection)

Hướng dẫn người dùng quét mặt phẳng

Đặt mô hình bằng thao tác chạm

Điều chỉnh kích thước mô hình bằng cử chỉ

Các hướng mở rộng:

Chọn và xoá mô hình

Thay thế mô hình tại vị trí cũ

Xem trước mô hình trước khi đặt

Đo kích thước trong không gian AR

Chụp ảnh, quay video AR

Lưu và khôi phục scene AR

4. Kiến trúc hệ thống
4.1 Sơ đồ kiến trúc
Ứng dụng Android AR
        |
        | HTTP API
        v
   Nginx (Cổng 80)
        |
        | Reverse Proxy
        v
 Node.js Server (Cổng 3000)
        |
        v
   Thư mục uploads (.glb)

4.2 Luồng hoạt động Client–Server

Người dùng upload file .glb lên server qua web

Server lưu file và tạo URL truy cập

Android app gọi API /api/models

Người dùng chọn mô hình

App tải mô hình từ server

Mô hình được hiển thị trong không gian thực

Người dùng tương tác với mô hình AR

5. Môi trường phát triển & kiểm thử
5.1 Server

Hệ điều hành: Linux

Backend: Node.js + Express

Reverse Proxy: Nginx

IP public: 136.111.208.187

5.2 Android

Thiết bị: Xiaomi Redmi Note 11S

Android: 13 (Tiramisu)

IDE: Android Studio

Ngôn ngữ: Java + XML

AR Framework: ARCore + Sceneform

6. Các vấn đề gặp phải và cách giải quyết
Vấn đề 1: App crash do xung đột ARCore – Sceneform

Nguyên nhân: gọi API HDR không tồn tại

Giải pháp: tắt Light Estimation và Depth Mode

Vấn đề 2: Upload file GLB bị lỗi

Nguyên nhân: giới hạn kích thước của Nginx

Giải pháp: cấu hình client_max_body_size

Vấn đề 3: App chạy local nhưng không chạy trên server thật

Nguyên nhân: sai BASE_URL và port

Giải pháp: dùng Nginx reverse proxy + port 80

7. Kết quả đạt được

✔ Xây dựng thành công hệ thống AR Client–Server
✔ Tải mô hình 3D từ server thật
✔ Hiển thị mô hình trong không gian thực
✔ Tương tác trực tiếp với mô hình
✔ Kiến trúc rõ ràng, có thể mở rộng

8. Hướng phát triển trong tương lai

Metadata cho mô hình (scale, rotation, category)

Cache model để giảm tải mạng

Lưu trạng thái scene AR

Xác thực người dùng

HTTPS và bảo mật

Tối ưu hiệu năng cho model lớn

9. Kết luận

Đề tài đã triển khai thành công một ứng dụng AR Android theo kiến trúc Client–Server, kết hợp giữa ARCore, Android và backend server.
Hệ thống không chỉ đáp ứng yêu cầu học thuật mà còn có khả năng ứng dụng thực tế và mở rộng trong tương lai.
