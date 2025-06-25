# backend/app/models/__init__.py

# === User-related Models ===
from .user import User
from .user_profile import UserProfile

# === Content-related Models ===
from .journal import Journal
from .article import Article
from .audio import AudioTrack
from .motivational_quote import MotivationalQuote

# === Interaction-related Models ===
from .chat import ChatMessage


__all__ = [
    "User",
    "UserProfile",
    "Journal",
    "Article",
    "AudioTrack",
    "MotivationalQuote",
    "ChatMessage",
]
