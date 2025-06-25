# backend/app/crud/__init__.py

# CRUD Base
from .base import CRUDBase

# User & Profile
from .crud_user import CRUDUser, user
from .crud_user_profile import user_profile

# Journal, Chat, Article
from .crud_journal import CRUDJournal, journal
from .crud_chat import chat_message
from .crud_article import article

# Audio, Quotes
from .crud_audio import audio_track
from .crud_motivational_quote import motivational_quote

__all__ = [
    "CRUDBase",
    "CRUDUser",
    "CRUDJournal",
    "user",
    "journal",
    "chat_message",
    "article",
    "audio_track",
    "motivational_quote",
    "user_profile",
]
