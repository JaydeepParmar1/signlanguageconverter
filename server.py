from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import os
from pathlib import Path
from text import process_image

app = FastAPI()


@app.get("/")
async def xyz():
    return {"hard coding is good":"but not cool!"}


@app.post("/upload")
async def abc(file: UploadFile = File(...)):

    print(f"Received file: {file.size}")


    save_dir = Path("uploaded_image")
    save_dir.mkdir(parents=True, exist_ok=True)

    file_path = save_dir / file.filename

    try:

        with open(file_path, "wb") as f:
            file_content = await file.read()
            f.write(file_content)

        output = process_image(str(file_path))
        return {"result": output}

    except Exception as e:
        return {"error": f"Failed to save file: {str(e)}"}
@app.get("/test")
async def random(name:str):
    return {"this is cool ":f"{name}"}