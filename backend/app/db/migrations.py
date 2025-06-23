import sqlite3
from app.core.config import settings


def migrate():
    """Simple migration to add is_flagged column if it doesn't exist."""
    conn = sqlite3.connect(settings.DATABASE_URL.replace('sqlite:///', ''))
    cursor = conn.cursor()
    cursor.execute("PRAGMA table_info(chat_messages)")
    columns = [row[1] for row in cursor.fetchall()]
    if 'is_flagged' not in columns:
        cursor.execute("ALTER TABLE chat_messages ADD COLUMN is_flagged BOOLEAN NOT NULL DEFAULT 0")
        conn.commit()
    conn.close()

