const express = require('express');
const app = express();
const path = require('path');
const ip = require('ip'); // Thư viện lấy IP tự động

const PORT = 3000;

// Cấu hình để thư mục 'public' chứa file 3D cho bên ngoài truy cập
app.use('/models', express.static(path.join(__dirname, 'public')));

// API để App Android gọi vào lấy link
app.get('/api/get-model', (req, res) => {
    // Tự động lấy IP của máy tính (Laptop)
    const myIp = ip.address(); 
    
    // Tạo đường dẫn đầy đủ đến file 3D
    const modelUrl = `http://${myIp}:${PORT}/models/my_model.glb`;
    
    console.log("--> Có điện thoại vừa gọi lấy model!");
    console.log("--> Trả về link: " + modelUrl);
    
    // Trả về dữ liệu dạng JSON cho Android
    res.json({
        name: "Mô hình 3D Demo",
        url: modelUrl
    });
});

// Khởi động server
app.listen(PORT, () => {
    console.log("---------------------------------------------------");
    console.log(`Server ĐÃ CHẠY THÀNH CÔNG!`);
    console.log(`Địa chỉ server: http://${ip.address()}:${PORT}`);
    console.log("---------------------------------------------------");
});