from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app import crud, models, schemas
from app.dependencies import get_db, get_current_user

router = APIRouter()

@router.post("/", response_model=schemas.JournalInDB)
def create_journal(
    *,
    db: Session = Depends(get_db),
    journal_in: schemas.JournalCreate,
    current_user: models.User = Depends(get_current_user),
):
    journal = crud.journal.create_with_owner(db=db, obj_in=journal_in, owner_id=current_user.id)
    return journal

@router.get("/", response_model=list[schemas.JournalInDB])
def read_journals(
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
    current_user: models.User = Depends(get_current_user),
):
    journals = crud.journal.get_multi_by_owner(db, owner_id=current_user.id, skip=skip, limit=limit)
    return journals
