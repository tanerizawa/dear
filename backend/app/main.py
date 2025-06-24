from app.api.api import api_router
from fastapi import FastAPI

import os
from app.models import *  # noqa
from alembic import command
from alembic.config import Config

app = FastAPI(title="Dear Diary API")


@app.on_event("startup")
def on_startup() -> None:
    """Run database migrations on startup."""
    alembic_cfg = Config(os.path.join(os.path.dirname(__file__), "..", "alembic.ini"))
    command.upgrade(alembic_cfg, "head")

app.include_router(api_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {"message": "Welcome to Dear Diary API"}
