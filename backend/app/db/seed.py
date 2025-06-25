"""Populate the database with some initial data."""

from app.db.session import SessionLocal
from app import crud, schemas


ARTICLES = [
    {"title": "Mindfulness Basics", "url": "https://example.com/mindfulness"},
    {"title": "Dealing with Stress", "url": "https://example.com/stress"},
]

AUDIO_TRACKS = [
    {
        "title": "Relaxing Piano",
        "url": "https://music.youtube.com/watch?v=9E6b3swbnWg",
    },
    {
        "title": "Nature Sounds",
        "url": "https://music.youtube.com/watch?v=eKFTSSKCzWA",
    },
]

QUOTES = [
    {"text": "Keep going no matter what.", "author": "Unknown"},
    {"text": "Every day is a fresh start.", "author": "Unknown"},
]


def seed() -> None:
    db = SessionLocal()
    try:
        for data in ARTICLES:
            crud.article.create(db, obj_in=schemas.ArticleCreate(**data))

        for data in AUDIO_TRACKS:
            crud.audio_track.create(db, obj_in=schemas.AudioTrackCreate(**data))

        for data in QUOTES:
            crud.motivational_quote.create(db, obj_in=schemas.MotivationalQuoteCreate(**data))
    finally:
        db.close()


if __name__ == "__main__":
    seed()
