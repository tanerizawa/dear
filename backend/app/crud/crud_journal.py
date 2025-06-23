from sqlalchemy.orm import Session
from .base import CRUDBase
from app.models.journal import Journal
from app.schemas.journal import JournalCreate, JournalUpdate

class CRUDJournal(CRUDBase[Journal, JournalCreate, JournalUpdate]):
    def create_with_owner(self, db: Session, *, obj_in: JournalCreate, owner_id: int) -> Journal:
        db_obj = Journal(**obj_in.dict(), owner_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def get_multi_by_owner(self, db: Session, *, owner_id: int, skip: int = 0, limit: int = 100) -> list[Journal]:
        return (
            db.query(self.model)
            .filter(Journal.owner_id == owner_id)
            .order_by(Journal.created_at.desc())
            .offset(skip)
            .limit(limit)
            .all()
        )

journal = CRUDJournal(Journal)
