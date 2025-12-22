const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors'); 

const app = express();
const PORT = 3000;

app.use(cors()); 

// Tạo thư mục uploads nếu chưa có
const uploadDir = 'uploads';
if (!fs.existsSync(uploadDir)){
    fs.mkdirSync(uploadDir);
}

// Tìm đoạn code khai báo storage (kho lưu trữ)
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/') // Thư mục lưu
    },
    filename: function (req, file, cb) {
        // 👇 BÍ KÍP LÀ Ở ĐÂY 👇
        // Thay vì dùng file.originalname (tên gốc), ta ép nó thành tên cố định
        cb(null, 'model.glb'); 
    }
});

const upload = multer({ storage: storage });

app.use(express.static('public'));
app.use('/uploads', express.static('uploads'));

// --- XỬ LÝ UPLOAD ---
app.post('/upload', upload.single('file'), (req, res) => {
    // Log để kiểm tra
    console.log("--------------------------------");
    console.log("📥 Đang nhận yêu cầu upload...");

    if (!req.file) {
        console.log("❌ Lỗi: Không thấy file đâu cả!");
        return res.status(400).json({ status: 'error', message: 'Thiếu file' });
    }

    console.log("✅ Đã lưu file thành công: model.glb");
    
    // Quan trọng: Trả về JSON chuẩn 200 OK
    return res.status(200).json({ status: 'success', message: 'Upload thành công!' });
});

// --- CHO ANDROID TẢI FILE ---
app.get('/api/get-model', (req, res) => {
    // 1. Log ngay khi có ai đó gọi vào
    console.log("--------------------------------");
    console.log("📞 Có thiết bị đang gọi API download...");
    console.log("👉 IP của thiết bị:", req.ip);

    const filePath = path.join(__dirname, 'uploads', 'model.glb');
    
    // 2. Kiểm tra file
    if (fs.existsSync(filePath)) {
        console.log("✅ Tìm thấy file model.glb, đang gửi đi...");
        
        // Thêm xử lý lỗi nếu gửi thất bại
        res.download(filePath, 'model.glb', (err) => {
            if (err) {
                console.log("❌ Lỗi khi đang gửi file:", err);
            } else {
                console.log("🚀 Đã gửi xong!");
            }
        });
    } else {
        console.log("⚠️ Không tìm thấy file trong thư mục uploads!");
        res.status(404).send("Chưa có file nào được upload!");
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server đang chạy tại: http://localhost:${PORT}`);
});