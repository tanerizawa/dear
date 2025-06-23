from app.api.api import api_router
from fastapi import FastAPI

from app.db.session import engine
from app.models import *  # noqa
from app.db.base_class import Base
from app.db.migrations import migrate

app = FastAPI(title="Dear Diary API")


@app.on_event("startup")
def on_startup() -> None:
    """Create database tables on startup."""
    Base.metadata.create_all(bind=engine)
    migrate()

app.include_router(api_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {"message": "Welcome to Dear Diary API"}
