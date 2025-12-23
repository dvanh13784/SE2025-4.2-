const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors'); 

const app = express();
const PORT = 3000; 

app.use(cors()); 
app.use(express.json()); 
app.use(express.static('public')); 
app.use('/uploads', express.static('uploads'));

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

// --- API 1: UPLOAD ---
app.post('/upload', upload.array('files'), (req, res) => {
    if (!req.files || req.files.length === 0) return res.status(400).json({ message: 'Thiếu file' });
    res.json({ message: 'Upload thành công!' });
});

// --- API 2: LẤY DANH SÁCH FILE ---
app.get('/api/files', (req, res) => {
    try {
        const hostUrl = `http://${req.headers.host}`;
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
                    url: `${hostUrl}/uploads/${file}`
                };
            })
            .sort((a, b) => b.timestamp - a.timestamp); 

        res.json(files);
    } catch (error) {
        res.status(500).json({ error: 'Lỗi server' });
    }
});

// --- API 3: XÓA FILE ---
app.delete('/api/files/:filename', (req, res) => {
    const filename = req.params.filename;
    const filePath = path.join(uploadDir, filename);

    if (filename.includes('..') || filename.includes('/')) return res.status(400).json({ error: 'Tên file lỗi' });

    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath); 
        res.json({ success: true, message: 'Đã xóa file' });
    } else {
        res.status(404).json({ error: 'File không tồn tại' });
    }
});

// --- API 4: ANDROID DOWNLOAD ---
app.get('/api/get-model', (req, res) => {
    const files = fs.readdirSync(uploadDir)
        .filter(f => f.endsWith('.glb') || f.endsWith('.gltf'))
        .map(f => ({ name: f, time: fs.statSync(path.join(uploadDir, f)).mtimeMs }))
        .sort((a, b) => b.time - a.time);

    if (files.length > 0) res.download(path.join(uploadDir, files[0].name), files[0].name);
    else res.status(404).send("Empty");
});

// Chạy Server (Sửa thông báo log)
app.listen(PORT, '0.0.0.0', () => {
    console.log('✅ Server đã chạy thành công!');
});
