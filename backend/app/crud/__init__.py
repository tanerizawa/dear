from .crud_user import CRUDUser, user
from .crud_journal import CRUDJournal, journal
from .base import CRUDBase

__all__ = [
    "CRUDBase",
    "CRUDUser",
    "CRUDJournal",
    "user",
    "journal",
]
