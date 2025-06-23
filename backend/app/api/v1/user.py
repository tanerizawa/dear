from fastapi import APIRouter, Depends

from app import models, schemas
from app.dependencies import get_current_user

router = APIRouter()

@router.get("/users/me", response_model=schemas.UserPublic)
def read_users_me(current_user: models.User = Depends(get_current_user)):
    return current_user
