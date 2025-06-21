from fastapi import FastAPI
from app.api.api import api_router

app = FastAPI(title="Dear Diary API")

app.include_router(api_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {"message": "Welcome to Dear Diary API"}
