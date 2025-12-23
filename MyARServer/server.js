const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors'); 

const app = express();

// --- CẤU HÌNH QUAN TRỌNG ---
const PORT = 3000; // Chạy cổng 3000 (Ổn định nhất trên VPS)
const SERVER_IP = "136.111.208.187"; // IP Server của bạn

app.use(cors()); 
app.use(express.json()); 
app.use(express.static('public')); // Chứa giao diện Web (index.html)
app.use('/uploads', express.static('uploads')); // Chứa file model (.glb)

// Tạo thư mục uploads
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir);

// Cấu hình lưu file
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => {
        const timePrefix = Date.now();
        const safeName = file.originalname.replace(/[^a-zA-Z0-9_.-]/g, '_');
        cb(null, `${timePrefix}-${safeName}`);
    }
});
const upload = multer({ storage: storage });

// ============================================
// 1. API CHO WEB (Upload, Danh sách, Xóa)
// ============================================

// Upload file
app.post('/upload', upload.array('files'), (req, res) => {
    if (!req.files || req.files.length === 0) return res.status(400).json({ message: 'Thiếu file' });
    res.json({ message: 'Upload thành công!' });
});

// Lấy danh sách hiển thị lên Web
app.get('/api/files', (req, res) => {
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
                    // Link tải trực tiếp (Dùng IP cứng để không bị lỗi localhost)
                    url: `http://${SERVER_IP}:${PORT}/uploads/${file}`
                };
            })
            .sort((a, b) => b.timestamp - a.timestamp); // Mới nhất lên đầu

        res.json(files);
    } catch (error) {
        res.status(500).json({ error: 'Lỗi server' });
    }
});

// Xóa file
app.delete('/api/files/:filename', (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(uploadDir, filename);
    
    if (filename.includes('..') || filename.includes('/')) return res.status(400).json({ error: 'Lỗi tên file' });

    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
        res.json({ success: true, message: 'Đã xóa file' });
    } else {
        res.status(404).json({ error: 'File không tồn tại' });
    }
});

// ============================================
// 2. API CHO ANDROID (Quan trọng)
// ============================================
app.get('/api/get-model', (req, res) => {
    // Log để bạn biết khi nào Android kết nối
    console.log(`📲 Android đang gọi tải file từ IP: ${req.ip}`);

    const files = fs.readdirSync(uploadDir)
        .filter(f => f.endsWith('.glb') || f.endsWith('.gltf'))
        .map(f => ({ name: f, time: fs.statSync(path.join(uploadDir, f)).mtimeMs }))
        .sort((a, b) => b.time - a.time);

    if (files.length > 0) {
        const latestFile = files[0].name;
        console.log(`✅ Đang gửi file cho Android: ${latestFile}`);
        res.download(path.join(uploadDir, latestFile), latestFile);
    } else {
        console.log("⚠️ Server trống, không có file để gửi.");
        res.status(404).send("Server chưa có file nào.");
    }
});

// ============================================
// 3. KHỞI ĐỘNG SERVER
// ============================================
app.listen(PORT, '0.0.0.0', () => {
    console.log("---------------------------------------------------");
    console.log("✅ SERVER ĐÃ CHẠY THÀNH CÔNG!");
    console.log(`👉 Link Web Quản Lý:  http://${SERVER_IP}:${PORT}`);
    console.log(`👉 Link cho Android:  http://${SERVER_IP}:${PORT}/api/get-model`);
    console.log("---------------------------------------------------");
});
