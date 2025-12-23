const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const cors = require('cors'); 

const app = express();


const PORT = 3000; 


const SERVER_IP = '136.111.208.187';
const BASE_URL = `http://${SERVER_IP}`; // Đường dẫn gốc: http://136.111.208.187

app.use(cors()); 

// Tạo thư mục uploads nếu chưa có
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)){
    fs.mkdirSync(uploadDir);
}

// Lưu file với tên gốc kèm timestamp
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
        // Sửa đường dẫn trả về theo IP server
        url: `${BASE_URL}/uploads/${file.filename}`
    }));

    console.log(`✅ Đã lưu ${uploadedFiles.length} file`);
    return res.status(200).json({ status: 'success', message: 'Upload thành công!', files: uploadedFiles });
});

// --- LẤY DANH SÁCH MODEL ---
app.get('/api/models', (req, res) => {
    const glbFiles = fs.readdirSync(uploadDir)
        .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
        .map(file => ({
            name: file,
            // Sửa đường dẫn trả về theo IP server
            url: `${BASE_URL}/uploads/${file}`
        }));

    res.json({ models: glbFiles });
});

// --- API CHO ANDROID TẢI FILE MỚI NHẤT ---
app.get('/api/get-model', (req, res) => {
    console.log("--------------------------------");
    console.log("📞 Có thiết bị đang gọi API download...");
    console.log("👉 IP thiết bị:", req.ip);

    const glbFiles = fs.readdirSync(uploadDir)
        .filter(file => file.endsWith('.glb') || file.endsWith('.gltf'))
        .map(file => ({
            name: file,
            time: fs.statSync(path.join(uploadDir, file)).mtimeMs
        }))
        .sort((a, b) => b.time - a.time);

    const latestFile = glbFiles.length > 0 ? glbFiles[0].name : null;
    const filePath = latestFile ? path.join(uploadDir, latestFile) : null;

    if (filePath && fs.existsSync(filePath)) {
        console.log(`✅ Tìm thấy file ${latestFile}, đang gửi đi...`);
        res.download(filePath, latestFile, (err) => {
            if (err) console.log("❌ Lỗi khi gửi file:", err);
            else console.log("🚀 Đã gửi xong!");
        });
    } else {
        console.log("⚠️ Không tìm thấy file!");
        res.status(404).send("Chưa có file nào!");
    }
});

// Lắng nghe trên cổng 80
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server quản lý đang chạy tại: http://${SERVER_IP}:${PORT}`);
});
