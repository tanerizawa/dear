from fastapi import APIRouter, Depends

from app import models, schemas
from app.dependencies import get_current_user

router = APIRouter()

@router.post("/", response_model=schemas.ChatResponse)
def create_chat_message(
    *,
    message_in: schemas.ChatMessage,
    current_user: models.User = Depends(get_current_user),
):
    """Simple chat endpoint that echoes back the provided message."""
    reply = f"You said: {message_in.message}" 
    return schemas.ChatResponse(reply=reply)

