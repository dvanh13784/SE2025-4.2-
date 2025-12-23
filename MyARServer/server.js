const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const cors = require('cors'); // Thêm cái này để tránh lỗi nếu gọi từ nơi khác

const app = express();
const PORT = 3000;
const UPLOAD_DIR = './uploads';

// Cấu hình cơ bản
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(UPLOAD_DIR)); // Cho phép truy cập file
app.use(express.static('public')); // Cho phép truy cập giao diện quản lý

// Tạo thư mục nếu chưa có
if (!fs.existsSync(UPLOAD_DIR)) fs.mkdirSync(UPLOAD_DIR);

// 1. Cấu hình Upload (Giữ nguyên tên gốc của file)
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, UPLOAD_DIR),
    filename: (req, file, cb) => {
        // Xử lý tên file để tránh lỗi ký tự đặc biệt (tiếng Việt, dấu cách)
        const originalName = Buffer.from(file.originalname, 'latin1').toString('utf8');
        const safeName = originalName.replace(/\s+/g, '_'); // Đổi khoảng trắng thành _
        cb(null, safeName);
    }
});
const upload = multer({ storage: storage });

// --- CÁC API QUẢN LÝ ---

// API 1: Lấy danh sách tất cả các file
app.get('/api/files', (req, res) => {
    fs.readdir(UPLOAD_DIR, (err, files) => {
        if (err) return res.status(500).json({ error: 'Lỗi đọc thư mục' });

        const fileInfos = files.map(file => {
            const stats = fs.statSync(path.join(UPLOAD_DIR, file));
            return {
                name: file,
                size: (stats.size / 1024 / 1024).toFixed(2) + ' MB', // Chuyển sang MB
                date: stats.mtime.toLocaleString('vi-VN'), // Ngày giờ việt nam
                url: `http://${req.headers.host}/uploads/${file}`
            };
        });
        res.json(fileInfos); // Trả về danh sách JSON
    });
});

// API 2: Upload file mới
app.post('/api/upload', upload.single('modelFile'), (req, res) => {
    if (!req.file) return res.status(400).json({ error: 'Chưa chọn file!' });
    res.json({ message: 'Upload thành công!', file: req.file });
});

// API 3: Xóa file
app.delete('/api/files/:filename', (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(UPLOAD_DIR, filename);

    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath); // Lệnh xóa file
        res.json({ success: true, message: `Đã xóa ${filename}` });
    } else {
        res.status(404).json({ error: 'File không tồn tại' });
    }
});

// Chạy Server
app.listen(PORT, () => {
    console.log(`🚀 AR Server Management running at http://localhost:${PORT}`);
});
