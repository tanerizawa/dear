from .base import CRUDBase
from app.models.chat import ChatMessage
from app.schemas.chat import ChatMessageCreate, ChatMessageUpdate

class CRUDChatMessage(CRUDBase[ChatMessage, ChatMessageCreate, ChatMessageUpdate]):
    def create_with_owner(self, db, *, obj_in: ChatMessageCreate, owner_id: int) -> ChatMessage:
        db_obj = ChatMessage(
            **obj_in.model_dump(),
            owner_id=owner_id
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def get_multi_by_owner(self, db, *, owner_id: int, skip: int = 0, limit: int = 100):
        return (
            db.query(self.model)
            .filter(ChatMessage.owner_id == owner_id)
            .order_by(ChatMessage.created_at.desc())
            .offset(skip)
            .limit(limit)
            .all()
        )

chat_message = CRUDChatMessage(ChatMessage)
