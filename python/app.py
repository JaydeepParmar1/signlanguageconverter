from flask import Flask, request, jsonify
from flask_cors import CORS
import subprocess
import os
import uuid
from text import process_image
# import cv2
app = Flask(__name__)
CORS(app)

# Create a directory to store temporary files
TEMP_FOLDER = "./temp_images"
os.makedirs(TEMP_FOLDER, exist_ok=True)


@app.route('/upload', methods=['POST'])
def upload_file():
    # Check if an image file is uploaded
    if 'image' not in request.files:
        return jsonify({"error": "No image file uploaded"}), 400

    file = request.files['image']

    # Save the image temporarily
    temp_filename = f"{uuid.uuid4().hex}.jpg"
    temp_filepath = os.path.join(TEMP_FOLDER, temp_filename)
    file.save(temp_filepath)

    try:
        # Call the text.py script using subprocess
        # result = subprocess.run(
        #     ['python', 'text.py', temp_filepath],  # Call text.py with the image path
        #     capture_output=True,                  # Capture the script output
        #     text=True                             # Output as text (string)
        # )



        # Remove the temporary file after processing
        os.remove(temp_filepath)

        # Check if the script executed successfully
        if result.returncode == 0:
            return jsonify({"prediction": result.stdout.strip()}), 200
        else:
            return jsonify({"error": result.stderr.strip()}), 500

    except Exception as e:
        # Handle unexpected server errors
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000, debug=True)
