const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors'); 

const app = express();

// 👇 1. CẤU HÌNH ĐỊA CHỈ SERVER CỦA BẠN Ở ĐÂY 👇
const SERVER_IP = "136.111.208.187";
const PORT = 3000; // Chạy cổng 3000 (Nhớ mở firewall)

app.use(cors()); 
app.use(express.static('public')); // Chứa file giao diện index.html
app.use('/uploads', express.static('uploads')); // Chứa file model

// Tạo thư mục uploads nếu chưa có
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)){
    fs.mkdirSync(uploadDir);
}

// Cấu hình lưu file (Tự đổi tên để tránh trùng)
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/');
    },
    filename: function (req, file, cb) {
        const timePrefix = Date.now();
        const safeName = file.originalname.replace(/[^a-zA-Z0-9_.-]/g, '_');
        cb(null, `${timePrefix}-${safeName}`);
    }
});
const upload = multer({ storage: storage });

// --- API 1: UPLOAD FILE ---
app.post('/upload', upload.array('files'), (req, res) => {
    if (!req.files || req.files.length === 0) {
        return res.status(400).json({ status: 'error', message: 'Thiếu file' });
    }
    return res.status(200).json({ status: 'success', message: 'Upload thành công!' });
});

// --- API 2: LẤY DANH SÁCH (Sửa lỗi quan trọng ở đây) ---
app.get('/api/models', (req, res) => {
    try {
        const files = fs.readdirSync(uploadDir)
            .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
            .map(file => {
                const filePath = path.join(uploadDir, file);
                const stats = fs.statSync(filePath);
                return {
                    name: file,
                    size: (stats.size / 1024 / 1024).toFixed(2) + ' MB',
                    date: new Date(stats.mtime).toLocaleString('vi-VN'),
                    timestamp: stats.mtimeMs,
                    // 👇 QUAN TRỌNG: Ép cứng IP để link luôn đúng
                    url: `http://${SERVER_IP}:${PORT}/uploads/${file}`
                };
            })
            .sort((a, b) => b.timestamp - a.timestamp); // Mới nhất lên đầu

        res.json({ models: files });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Lỗi đọc thư mục hoặc chưa có file nào' });
    }
});

// --- API 3: XÓA FILE ---
app.delete('/api/files/:filename', (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(uploadDir, filename);

    if (filename.includes('..') || filename.includes('/')) {
        return res.status(400).json({ error: 'Tên file không hợp lệ' });
    }

    if (fs.existsSync(filePath)) {
        try {
            fs.unlinkSync(filePath);
            res.json({ success: true, message: `Đã xóa ${filename}` });
        } catch (e) {
            res.status(500).json({ error: 'Lỗi khi xóa file' });
        }
    } else {
        res.status(404).json({ error: 'File không tồn tại' });
    }
});

// --- API 4: ANDROID TẢI FILE MỚI NHẤT ---
app.get('/api/get-model', (req, res) => {
    const glbFiles = fs.readdirSync(uploadDir)
        .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
        .map(file => ({
            name: file,
            time: fs.statSync(path.join(uploadDir, file)).mtimeMs
        }))
        .sort((a, b) => b.time - a.time);

    const latestFile = glbFiles.length > 0 ? glbFiles[0].name : null;
    
    if (latestFile) {
        res.download(path.join(uploadDir, latestFile), latestFile);
    } else {
        res.status(404).send("Chưa có file nào.");
    }
});

// Khởi động Server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`------------------------------------------------`);
    console.log(`✅ Server ĐÃ CHẠY THÀNH CÔNG!`);
    console.log(`👉 Truy cập Web tại: http://${SERVER_IP}:${PORT}`);
    console.log(`------------------------------------------------`);
});
