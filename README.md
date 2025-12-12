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
1) Chuẩn bị môi trường
1.	Cài Node.js (LTS).
2.	Cài Android Studio + SDK.
3.	Chuẩn bị điện thoại Android hỗ trợ ARCore (khuyến nghị dùng máy thật; emulator thường bất ổn với AR).
4.	Chuẩn bị 1 model mẫu dạng .glb (có thể export từ Blender).
2) Dựng server Node.js (tối thiểu chạy được)
1.	Tạo thư mục server/
2.	Tạo public/models/ và đặt tree.glb (hoặc model khác) vào đó.
3.	Tạo server.js (giống file bạn đã có) gồm:
o	Serve static /models
o	API GET /api/models/:id trả về JSON { id, url }
4.	Chạy server và kiểm tra trên trình duyệt máy tính:
o	http://localhost:3000/api/models/tree
o	http://localhost:3000/models/tree.glb
3) Thiết lập test mạng LAN (đúng kiểu bạn từng test)
1.	Cho laptop phát Wi-Fi (hotspot) hoặc dùng router chung.
2.	Điện thoại kết nối vào cùng mạng.
3.	Lấy IP LAN của laptop (ví dụ 192.168.43.1).
4.	Trên điện thoại mở Chrome test:
o	http://<IP>:3000/api/models/tree
o	http://<IP>:3000/models/tree.glb
Nếu điện thoại truy cập được 2 link này, mạng OK.
4) Tạo dự án Android AR (khung chạy AR trước)
1.	Tạo project Android (Java).
2.	Thêm quyền và cấu hình cần thiết:
o	Camera permission (nếu cần theo mẫu)
o	ARCore dependency
3.	Tích hợp ARFragment / Sceneform để mở camera và detect plane.
4.	Chạy app: đảm bảo mở camera AR và hiện hướng dẫn tìm mặt phẳng (plane).
5) Kết nối Android với server (lấy URL model)
1.	Thêm Retrofit + OkHttp logging.
2.	Tạo API client gọi:
o	GET http://<IP>:3000/api/models/tree
3.	Log ra url nhận được để chắc chắn call API thành công.
6) Tải và hiển thị model GLB trong AR
1.	Khi đã có modelUrl, dùng Sceneform load GLB từ URL.
2.	Bắt sự kiện user tap lên mặt phẳng:
o	tạo Anchor
o	tạo Node/TransformableNode
o	gán renderable (model) vào node
3.	Chạy app và đặt model được vào AR.
7) Thêm tương tác cơ bản
1.	Bật thao tác:
o	kéo/di chuyển (nếu bạn cho phép)
o	xoay
o	phóng to/thu nhỏ
2.	Thêm UI tối thiểu (nút chọn model, reset scene, v.v. – tuỳ bạn).
