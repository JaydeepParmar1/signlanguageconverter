const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs'); // For checking and creating directories
const { exec } = require('child_process');

const app = express();
const port = 3000;

// Ensure the 'uploads' folder exists
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir);
}

// Set up Multer for handling file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);  // Save files to the 'uploads' directory
  },
  filename: (req, file, cb) => {
    cb(null, Date.now() + path.extname(file.originalname)); // Unique file name
  }
});

const upload = multer({ storage: storage });

// Endpoint to receive the photo
app.post('/upload', upload.single('image'), (req, res) => {
  if (!req.file) {
    return res.status(400).send('No file uploaded.');
  }

  const filePath = req.file.path; // File path for processing

  // Pass the image to the model (example using Python)
  exec(`python3 text.py ${filePath}`, (error, stdout, stderr) => {
    if (error) {
      console.error(`exec error: ${error.message}`);
      return res.status(500).send(`Error processing image: ${error.message}`);
    }
    if (stderr) {
      console.error(`stderr: ${stderr}`);
      return res.status(500).send(`Error processing image: ${stderr}`);
    }

    console.log(`stdout: ${stdout}`);
    res.send({result: stdout});
  });
})

// Serve static files (e.g., images) from the 'uploads' folder
app.use('/uploads', express.static(uploadDir));

// Start the server
app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});
