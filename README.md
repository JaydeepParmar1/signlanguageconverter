
---

## **README for Sign Language Generator**
# Sign Language Generator 

Sign Language Generator is an **offline** app that converts text into sign language symbols (images or videos).  
It can be used as both a **learning tool** and a **communication aid** for deaf and mute communities.

---

## ✨ Features
- **Text-to-Sign Conversion**:
  - If a full sentence has a matching sign video, it plays it directly.
  - If not, the sentence is broken into words.
  - If a word is missing, it is broken into alphabets.
- **Offline Dataset**:
  - No internet required — all sign images/videos are stored locally.
- **Bidirectional Communication**:
  - Future support for sign-to-text conversion using hand tracking.
- **Python Backend + Kotlin Frontend**:
  - Python handles text-to-sign processing.
  - Kotlin app handles the UI and plays the generated signs.
- **Custom Word Mapping**:
  - Certain words (e.g., "she") are replaced with synonyms (e.g., "he") for dataset consistency.

---

## 📱 Tech Stack
- **Kotlin (Jetpack Compose)** for the mobile app UI
- **Python** for text processing
- **MediaPipe** (planned) for hand tracking in future updates
- **Local storage** for offline access
- **Custom dictionary mapping** for handling similar words

---

