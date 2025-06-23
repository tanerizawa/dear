from fastapi import APIRouter
from app.api.v1 import auth, journal, chat, user

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(journal.router, prefix="/journals", tags=["journals"])
api_router.include_router(chat.router, prefix="/chat", tags=["chat"])
api_router.include_router(user.router, tags=["users"])

