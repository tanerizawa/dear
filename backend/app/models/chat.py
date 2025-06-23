from enum import Enum
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Enum as SQLAlchemyEnum
from sqlalchemy.orm import relationship
from app.db.base_class import Base
import datetime

class SenderType(str, Enum):
    USER = "user"
    AI = "ai"

class ChatMessage(Base):
    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, index=True)
    content = Column(String, nullable=False)
    sender_type = Column(SQLAlchemyEnum(SenderType), nullable=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship("User")

    # Kolom penting untuk menyimpan hasil dari Planner
    ai_technique = Column(String, nullable=True)
    # Optional emotion label for the message
    emotion = Column(String, nullable=True)
