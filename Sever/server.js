const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors'); // Nếu chưa cài thì chạy: npm install cors

const app = express();
const PORT = 3000;

app.use(cors()); // Cho phép các nguồn khác gọi vào thoải mái

// --- TỰ ĐỘNG TẠO THƯ MỤC UPLOADS NẾU CHƯA CÓ ---
// (Tránh lỗi crash nếu bạn quên tạo folder)
const uploadDir = 'uploads';
if (!fs.existsSync(uploadDir)){
    fs.mkdirSync(uploadDir);
}

// 1. Cấu hình nơi lưu file Upload
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/') 
    },
    filename: function (req, file, cb) {
        // Luôn lưu thành tên cố định để Android dễ tải
        cb(null, 'model.glb'); 
    }
});

const upload = multer({ storage: storage });

// 2. Cho phép truy cập vào thư mục public (nơi chứa file html giao diện)
app.use(express.static('public'));

// --- API UPLOAD (ĐÃ SỬA) ---
// Quan trọng: Đổi 'modelFile' thành 'file' để khớp với giao diện
app.post('/upload', upload.single('file'), (req, res) => {
    
    if (!req.file) {
        return res.status(400).json({ status: 'error', message: 'Không có file nào được gửi!' });
    }

    console.log("✅ Đã nhận file mới từ Web: model.glb");
    
    // Trả về JSON để giao diện hiện thông báo đẹp, không bị chuyển trang
    res.status(200).json({ status: 'success', message: 'Upload thành công!' });
});

// --- API DOWNLOAD CHO ANDROID ---
app.get('/api/get-model', (req, res) => {
    const filePath = path.join(__dirname, 'uploads', 'model.glb');
    
    if (fs.existsSync(filePath)) {
        console.log("📲 Android đang tải model về...");
        res.download(filePath); 
    } else {
        res.status(404).send("Chưa có file nào được upload!");
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server đang chạy tại: http://localhost:${PORT}`);
});