const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
app.use(cors());

app.use('/models', express.static(path.join(__dirname, 'public/models'), {
  setHeaders: (res, filePath) => {
    if (filePath.endsWith('.glb')) {
      res.setHeader('Content-Type', 'model/gltf-binary');
      res.setHeader('Accept-Ranges', 'bytes');
    }
  }
}));

app.get('/api/models/:id', (req, res) => {
  const { id } = req.params;
  const map = {
    'tree': '/models/tree.glb'
  };
  const rel = map[id];
  if (!rel) return res.status(404).json({ error: 'Not found' });
  const host = req.headers.host;
  const url = `http://${host}${rel}`;
  res.json({ id, url });
});

const PORT = 3000;
app.listen(PORT, () => console.log(`Server chạy tại http://localhost:${PORT}`));
