Đề tài
Xây dựng ứng dụng Thực tế tăng cường (AR) trên Android hiển thị mô hình 3D từ server theo kiến trúc Client–Server.
________________________________________
Nội dung cần thực hiện
1.	Xây dựng server backend
o	Cung cấp API trả về thông tin và URL của mô hình 3D định dạng .glb
o	Lưu trữ và phân phối file 3D cho client Android
2.	Phát triển ứng dụng Android AR
o	Sử dụng ARCore để hiển thị mô hình 3D trong không gian thực thông qua camera
o	Kết nối đến server để lấy URL mô hình 3D
o	Tải và hiển thị mô hình 3D trong môi trường AR
o	Cho phép người dùng tương tác với mô hình (xoay, phóng to, thu nhỏ)
3.	Thiết lập môi trường kiểm thử
o	Chạy server trên máy tính cá nhân
o	Thiết bị Android kết nối cùng mạng để truy cập server và hiển thị AR
Các bước lần lượt cần làm : 
1) Chạy server Node.js (local trên PC)
Bước 1. Mở terminal tại thư mục server

Ví dụ:
C:\Users\Admin\ar-server

Bước 2. Chạy server
node server.js

Kết quả đúng cần thấy

Terminal in:

Server chạy tại http://localhost:3000

2) Test server bằng trình duyệt (PC)
Bước 3. Test API trả URL model

Mở:

http://localhost:3000/api/models/tree

Kết quả đúng cần thấy

Trả JSON:

{"id":"tree","url":"http://localhost:3000/models/tree.glb"}

Bước 4. Test file GLB

Mở:

http://localhost:3000/models/tree.glb

Kết quả đúng cần thấy

Trình duyệt tải được file (không 404)

👉 Nếu 2 bước này OK ⇒ server đạt yêu cầu (cung cấp model qua HTTP + trả URL qua API).

3) Chạy Android trên Android Studio (2 chế độ)
Chế độ A — Emulator (chỉ kiểm tra kết nối API)

Emulator chỉ để test Retrofit/network, không bắt buộc phải đặt AR.

Bước 5A. Đảm bảo BASE_URL đúng cho emulator

Trong MainActivity:

BASE_URL = "http://10.0.2.2:3000/";

Bước 6A. Run app trên emulator

Android Studio → Run (chọn emulator)

Kết quả đúng cần thấy

App mở lên và hiển thị thông báo (Toast) kiểu:

“Model sẵn sàng”

Logcat không báo lỗi mạng

👉 Kết quả này chứng minh: Android client gọi được API server và lấy được URL file .glb.

Chế độ B — Máy thật (kết quả cuối cùng của project)

Đây là phần bắt buộc để “đạt” đề tài AR.

Bước 5B. Đổi BASE_URL sang IP của PC

Ví dụ:

BASE_URL = "http://192.168.1.10:3000/";

Bước 6B. Cho điện thoại và PC cùng mạng

Laptop phát Wi-Fi hoặc cùng router

Bước 7B. Test bằng Chrome trên điện thoại (trước khi chạy app)

Mở:

http://192.168.1.10:3000/api/models/tree

Kết quả đúng cần thấy

Điện thoại thấy JSON trả về (không timeout)

Bước 8B. Run app từ Android Studio lên điện thoại

Cắm USB debugging

Android Studio → Run → chọn thiết bị thật

Bước 9B. Trải nghiệm AR

App mở camera

Di chuyển điện thoại để nhận diện mặt phẳng (plane)

Chạm lên mặt phẳng để đặt model

Kết quả đúng cần thấy (tiêu chí hoàn thành)

Model 3D xuất hiện trong không gian thực (AR)

Model lấy từ server (không nhúng cứng trong app)

Người dùng thao tác được:

phóng to/thu nhỏ

xoay

(tuỳ chọn) di chuyển/đặt lại

👉 Nếu bạn đạt 3 ý này trên máy thật, thì thỏa mãn yêu cầu đề tài Client–Server AR.

Tiêu chí “đạt” của project (chốt ngắn gọn)

Bạn chỉ cần chứng minh được:

Server Node.js cung cấp model .glb và API trả URL

Android app gọi API lấy URL model

Android ARCore tải model từ URL và hiển thị trong AR + tương tác cơ bản
