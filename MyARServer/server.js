const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3000
const SERVER_IP = "136.111.208.187";

app.use(cors());
app.use(express.json());
const UPLOAD_DIR = './uploads';

// Tạo thư mục nếu chưa có
if (!fs.existsSync(UPLOAD_DIR)) fs.mkdirSync(UPLOAD_DIR);

app.use('/uploads', express.static(UPLOAD_DIR));
app.use(express.static('public'));

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, UPLOAD_DIR),
    filename: (req, file, cb) => {
        const originalName = Buffer.from(file.originalname, 'latin1').toString('utf8');
        const safeName = originalName.replace(/\s+/g, '_');
        cb(null, safeName);
    }
});
const upload = multer({ storage: storage });

// --- API ---
app.get('/api/files', (req, res) => {
    fs.readdir(UPLOAD_DIR, (err, files) => {
        if (err) return res.status(500).json({ error: 'Lỗi đọc thư mục' });
        const fileInfos = files.map(file => {
            const stats = fs.statSync(path.join(UPLOAD_DIR, file));
            return {
                name: file,
                size: (stats.size / 1024 / 1024).toFixed(2) + ' MB',
                date: stats.mtime.toLocaleString('vi-VN'),
                // Sửa lại url trả về cho đúng IP và Port
                url: `http://${SERVER_IP}:${PORT}/uploads/${file}`
            };
        });
        res.json(fileInfos);
    });
});

app.post('/api/upload', upload.single('modelFile'), (req, res) => {
    if (!req.file) return res.status(400).json({ error: 'Chưa chọn file!' });
    res.json({ message: 'Upload thành công!', file: req.file });
});

app.delete('/api/files/:filename', (req, res) => {
    const filePath = path.join(UPLOAD_DIR, req.params.filename);
    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
        res.json({ success: true, message: `Đã xóa` });
    } else {
        res.status(404).json({ error: 'File không tồn tại' });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    // SỬA QUAN TRỌNG: Thêm port vào log
    console.log(`🚀 Server quản lý đang chạy tại: http://${SERVER_IP}:${PORT}/`);
});
