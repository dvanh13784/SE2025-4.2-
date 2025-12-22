/**
 * server.js - MyARServer
 * Chạy local:
 *   npm i
 *   node server.js
 *
 * Chạy trên server thật (khuyến nghị set base url):
 *   PUBLIC_BASE_URL=http://136.111.208.187 node server.js
 *
 * Nếu chạy sau Nginx reverse proxy (port 80 -> 3000), base url có thể:
 *   PUBLIC_BASE_URL=http://136.111.208.187 node server.js
 */

const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const cors = require("cors");

const app = express();
app.set("trust proxy", true); // quan trọng nếu chạy sau reverse proxy

const PORT = process.env.PORT || 3000;

// Nếu bạn chạy server thật, nên set biến này để URL trả về luôn đúng
// Ví dụ: PUBLIC_BASE_URL=http://136.111.208.187
const PUBLIC_BASE_URL = process.env.PUBLIC_BASE_URL
  ? process.env.PUBLIC_BASE_URL.replace(/\/$/, "")
  : null;

app.use(cors());

// --------------------
// Tạo thư mục uploads nếu chưa có
// --------------------
const uploadDir = path.join(__dirname, "uploads");
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// --------------------
// Multer storage: lưu tên file có timestamp để tránh đè
// --------------------
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    const timePrefix = Date.now();
    const safeName = file.originalname.replace(/[^a-zA-Z0-9_.-]/g, "_");
    cb(null, `${timePrefix}-${safeName}`);
  },
});

const upload = multer({ storage });

// --------------------
// Static files
// --------------------
app.use(express.static(path.join(__dirname, "public")));
app.use("/uploads", express.static(uploadDir));

function makeBaseUrl(req) {
  // Ưu tiên biến môi trường để không bị sai host/port khi deploy
  if (PUBLIC_BASE_URL) return PUBLIC_BASE_URL;

  // Nếu có reverse proxy, ưu tiên x-forwarded-*
  const proto = req.headers["x-forwarded-proto"] || req.protocol;
  const host = req.headers["x-forwarded-host"] || req.get("host");
  return `${proto}://${host}`;
}

// --------------------
// UPLOAD
// --------------------
app.post("/upload", upload.array("files"), (req, res) => {
  console.log("--------------------------------");
  console.log("📥 Đang nhận yêu cầu upload...");

  if (!req.files || req.files.length === 0) {
    console.log("❌ Lỗi: Không thấy file!");
    return res.status(400).json({ status: "error", message: "Thiếu file" });
  }

  const baseUrl = makeBaseUrl(req);

  const uploadedFiles = req.files.map((file) => ({
    name: file.filename,
    url: `${baseUrl}/uploads/${file.filename}`,
  }));

  console.log(`✅ Đã lưu ${uploadedFiles.length} file`);
  return res.status(200).json({
    status: "success",
    message: "Upload thành công!",
    files: uploadedFiles,
  });
});

// --------------------
// API: Danh sách models
// --------------------
app.get("/api/models", (req, res) => {
  const baseUrl = makeBaseUrl(req);

  let files = [];
  try {
    files = fs
      .readdirSync(uploadDir)
      .filter((file) => file.endsWith(".glb") || file.endsWith(".gltf"))
      .map((file) => ({
        name: file,
        url: `${baseUrl}/uploads/${file}`,
      }));
  } catch (e) {
    console.log("❌ Lỗi đọc thư mục uploads:", e);
    return res.status(500).json({ models: [], error: "Cannot read uploads" });
  }

  res.json({ models: files });
});

// --------------------
// API: Android tải model mới nhất
// --------------------
app.get("/api/get-model", (req, res) => {
  console.log("--------------------------------");
  console.log("📞 Có thiết bị đang gọi API download...");
  console.log("👉 IP của thiết bị:", req.ip);

  let glbFiles = [];
  try {
    glbFiles = fs
      .readdirSync(uploadDir)
      .filter((file) => file.endsWith(".glb") || file.endsWith(".gltf"))
      .map((file) => ({
        name: file,
        time: fs.statSync(path.join(uploadDir, file)).mtimeMs,
      }))
      .sort((a, b) => b.time - a.time);
  } catch (e) {
    console.log("❌ Lỗi đọc uploads:", e);
    return res.status(500).send("Server không đọc được thư mục uploads");
  }

  const latestFile = glbFiles.length > 0 ? glbFiles[0].name : null;
  const filePath = latestFile ? path.join(uploadDir, latestFile) : null;

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
    console.log("⚠️ Không tìm thấy file trong uploads!");
    res.status(404).send("Chưa có file nào được upload!");
  }
});

// --------------------
// Start server
// --------------------
app.listen(PORT, "0.0.0.0", () => {
  console.log("================================");
  console.log(`🚀 Server listening on 0.0.0.0:${PORT}`);
  console.log(`👉 Local test (trên server):  http://127.0.0.1:${PORT}/`);
  console.log(`👉 API models (trên server):  http://127.0.0.1:${PORT}/api/models`);
  if (PUBLIC_BASE_URL) {
    console.log(`👉 Public base url (theo ENV): ${PUBLIC_BASE_URL}`);
    console.log(`👉 Public entry:              ${PUBLIC_BASE_URL}/`);
    console.log(`👉 Public models:             ${PUBLIC_BASE_URL}/api/models`);
  } else {
    console.log("ℹ️ Gợi ý: set PUBLIC_BASE_URL để URL trả về luôn đúng khi deploy.");
    console.log("   Ví dụ: PUBLIC_BASE_URL=http://136.111.208.187");
  }
  console.log("================================");
});
