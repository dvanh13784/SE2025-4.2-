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

const MODEL_EXTENSIONS = new Set(['.glb', '.gltf']);

// Utility: get list of model files (sorted by newest)
const getModelFiles = (hostUrl = null) => {
    return fs.readdirSync(uploadDir)
        .filter(file => MODEL_EXTENSIONS.has(path.extname(file).toLowerCase()))
        .map(file => {
            const filePath = path.join(uploadDir, file);
            const stats = fs.statSync(filePath);
            const info = {
                name: file,
                size: (stats.size / 1024 / 1024).toFixed(2) + ' MB',
                date: new Date(stats.mtime).toLocaleString('vi-VN'),
                timestamp: stats.mtimeMs
            };

            if (hostUrl) info.url = `${hostUrl}/uploads/${file}`;
            return info;
        })
        .sort((a, b) => b.timestamp - a.timestamp);
};

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
        res.json(getModelFiles(hostUrl));
    } catch (error) {
        res.status(500).json({ error: 'Lỗi server' });
    }
});

// --- API 2b: ALIAS LẤY DANH SÁCH MODEL ---
app.get('/api/models', (req, res) => {
    try {
        const hostUrl = `http://${req.headers.host}`;
        res.json(getModelFiles(hostUrl));
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
    const files = getModelFiles();

    if (files.length > 0) res.download(path.join(uploadDir, files[0].name), files[0].name);
    else res.status(404).send("Empty");
});

// Chạy Server (Sửa thông báo log)
app.listen(PORT, '0.0.0.0', () => {
    console.log('✅ Server đã chạy thành công!');
});
