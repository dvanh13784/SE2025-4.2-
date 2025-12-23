const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors');

const app = express();
const PORT = 3000; // Server chạy cổng 3000
const SERVER_IP = "136.111.208.187";

// Middleware
app.use(cors());
app.use(express.static('public'));
app.use('/uploads', express.static('uploads'));

// Tạo thư mục uploads nếu chưa có
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

// --- CẤU HÌNH UPLOAD ---
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

// --- API 2: LẤY DANH SÁCH MODEL (Sửa logic lấy URL) ---
app.get('/api/models', (req, res) => {
    try {
        const files = fs.readdirSync(uploadDir)
            .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
            .map(file => {
                const filePath = path.join(uploadDir, file);
                const stats = fs.statSync(filePath);
                
                // Tự động nhận diện host để tạo link đúng dù chạy IP nào
                const hostUrl = `${req.protocol}://${req.get('host')}`; 
                
                return {
                    name: file,
                    size: (stats.size / 1024 / 1024).toFixed(2) + ' MB',
                    date: new Date(stats.mtime).toLocaleString('vi-VN'),
                    timestamp: stats.mtimeMs,
                    url: `${hostUrl}/uploads/${file}`
                };
            })
            .sort((a, b) => b.timestamp - a.timestamp); // File mới nhất lên đầu

        res.json({ models: files });
    } catch (error) {
        console.error("Lỗi đọc thư mục:", error);
        res.status(500).json({ error: 'Lỗi đọc thư mục' });
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

// --- API 4: ANDROID LẤY FILE MỚI NHẤT ---
app.get('/api/get-model', (req, res) => {
    console.log("👉 Android đang yêu cầu model mới nhất...");
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

// --- KHỞI ĐỘNG SERVER ---
app.listen(PORT, '0.0.0.0', () => {
    console.log(`------------------------------------------------`);
    console.log(`✅ Server đang chạy ổn định!`);
    console.log(`👉 Truy cập Web tại đây: http://${SERVER_IP}:${PORT}`); // Thêm cổng vào log
    console.log(`------------------------------------------------`);
});
