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
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

// Lưu file với tên gốc kèm timestamp để tránh đè lên nhau
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

app.use(express.static('public'));
app.use('/uploads', express.static('uploads'));

// --- XỬ LÝ UPLOAD ---
app.post('/upload', upload.array('files'), (req, res) => {
    console.log("--------------------------------");
    console.log("📥 Đang nhận yêu cầu upload...");

    if (!req.files || req.files.length === 0) {
        console.log("❌ Lỗi: Không thấy file đâu cả!");
        return res.status(400).json({ status: 'error', message: 'Thiếu file' });
    }

    const uploadedFiles = req.files.map(file => ({
        name: file.filename,
        url: `${req.protocol}://${req.get('host')}/uploads/${file.filename}`
    }));

    console.log(`✅ Đã lưu ${uploadedFiles.length} file`);
    return res.status(200).json({ status: 'success', message: 'Upload thành công!', files: uploadedFiles });
});

// --- LẤY DANH SÁCH MODEL ---
app.get('/api/models', (req, res) => {
    const uploadsPath = path.join(__dirname, 'uploads');
    const files = fs.readdirSync(uploadsPath)
        .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
        .map(file => {
            const stats = fs.statSync(path.join(uploadsPath, file));
            return {
                name: file,
                url: `${req.protocol}://${req.get('host')}/uploads/${file}`,
                size: stats.size,
                uploadedAt: stats.mtime.toISOString()
            };
        })
        .sort((a, b) => new Date(b.uploadedAt) - new Date(a.uploadedAt));

    res.json({ models: files });
});

// --- XOÁ MODEL CỤ THỂ ---
app.delete('/api/models/:name', (req, res) => {
    const uploadsPath = path.join(__dirname, 'uploads');
    const safeName = path.basename(req.params.name);
    const filePath = path.join(uploadsPath, safeName);

    if (!fs.existsSync(filePath)) {
        return res.status(404).json({ status: 'error', message: 'Không tìm thấy file' });
    }

    fs.unlinkSync(filePath);
    return res.json({ status: 'success', message: 'Đã xoá file' });
});

// --- CHO ANDROID TẢI FILE MỚI NHẤT (LEGACY) ---
app.get('/api/get-model', (req, res) => {
    console.log("--------------------------------");
    console.log("📞 Có thiết bị đang gọi API download...");
    console.log("👉 IP của thiết bị:", req.ip);

    const uploadsPath = path.join(__dirname, 'uploads');
    const glbFiles = fs.readdirSync(uploadsPath)
        .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
        .map(file => ({
            name: file,
            time: fs.statSync(path.join(uploadsPath, file)).mtimeMs
        }))
        .sort((a, b) => b.time - a.time);

    const latestFile = glbFiles.length > 0 ? glbFiles[0].name : null;
    const filePath = latestFile ? path.join(uploadsPath, latestFile) : null;

    if (filePath && fs.existsSync(filePath)) {
        console.log(`✅ Tìm thấy file ${latestFile}, đang gửi đi...`);

        res.download(filePath, latestFile, (err) => {
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
