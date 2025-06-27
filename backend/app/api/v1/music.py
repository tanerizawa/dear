# backend/app/api/v1/music.py (Versi Final dengan Lokalisasi untuk Hasil Konsisten)

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from pathlib import Path
from ytmusicapi import YTMusic, OAuthCredentials
import structlog
import re

from app import crud, models, schemas, dependencies
from app.core.config import settings
from app.services.music_keyword_service import MusicKeywordService

router = APIRouter()
log = structlog.get_logger(__name__)

# --- Blok Inisialisasi ---
OAUTH_PATH = Path(__file__).resolve().parent.parent.parent.parent / "oauth.json"
AUTH_ARG = str(OAUTH_PATH) if OAUTH_PATH.exists() else None

if not OAUTH_PATH.exists():
    log.warning(
        "Authentication file not found at %s. Running in unauthenticated mode.",
        OAUTH_PATH,
    )

creds = None
if settings.OAUTH_CLIENT_ID and settings.OAUTH_CLIENT_SECRET:
    creds = OAuthCredentials(
        client_id=settings.OAUTH_CLIENT_ID,
        client_secret=settings.OAUTH_CLIENT_SECRET,
    )

# --- PERBAIKAN FINAL DI SINI ---
# Mengubah 'language' ke 'en' (English) yang didukung oleh library.
# 'location' tetap 'ID' untuk memastikan hasil pencarian relevan dengan Indonesia.
if creds:
    ytmusic = YTMusic(
        AUTH_ARG,
        oauth_credentials=creds,
        language='en',
        location='ID'
    )
else:
    ytmusic = YTMusic(
        AUTH_ARG,
        language='en',
        location='ID'
    )
# --- Akhir Perbaikan ---

def _process_search_results(search_results: list) -> list[schemas.AudioTrack]:
    """Helper function to process search results and return a list of AudioTrack."""
    musics: list[schemas.AudioTrack] = []
    for idx, track in enumerate(search_results, start=1):
        video_id = track.get("videoId")
        title = track.get("title")
        if not video_id or not title:
            continue

        try:
            song = ytmusic.get_song(video_id)
            formats = song.get("streamingData", {}).get("adaptiveFormats", [])
            audio_format = next(
                (f for f in formats if f.get("mimeType", "").startswith("audio/")),
                None,
            )
            streaming_url = audio_format.get("url") if audio_format else None
        except Exception as e:
            log.error("ytmusic_error", video_id=video_id, error=str(e))
            continue

        if streaming_url:
            musics.append(schemas.AudioTrack(id=idx, title=title, url=streaming_url))
    return musics


@router.get("/", response_model=list[schemas.AudioTrack])
def search_music(
    mood: str = Query(..., min_length=1),
    current_user: models.User = Depends(dependencies.get_current_user)
):
    if not mood:
        raise HTTPException(status_code=400, detail="Mood parameter is required")

    try:
        search_results = ytmusic.search(query=mood, filter="songs", limit=20)
        musics = _process_search_results(search_results)
    except Exception as e:
        log.error("Pencarian musik manual gagal", query=mood, error=str(e))
        raise HTTPException(status_code=503, detail="Layanan pencarian musik sedang bermasalah.")

    if not musics:
        raise HTTPException(status_code=404, detail="No music found for the given mood")

    return musics


@router.get("/recommend", response_model=list[schemas.AudioTrack])
async def recommend_music(
    *,
    db: Session = Depends(dependencies.get_db),
    current_user: models.User = Depends(dependencies.get_current_user),
    keyword_service: MusicKeywordService = Depends(),
):
    journals = crud.journal.get_multi_by_owner(
        db=db, owner_id=current_user.id, limit=5
    )

    musics = []

    if journals:
        try:
            raw_ai_keyword = await keyword_service.generate_keyword(journals)
            cleaned_keyword = re.sub(r'[^a-zA-Z0-9\s]', '', raw_ai_keyword).strip()

            if cleaned_keyword:
                log.info("Mencoba rekomendasi dengan kata kunci AI", keyword=cleaned_keyword)
                search_results = ytmusic.search(query=cleaned_keyword, filter="songs", limit=20)
                musics = _process_search_results(search_results)

            if not musics:
                mood = journals[0].mood
                fallback_map = {
                    "Sangat Negatif": "lagu sedih", "Negatif": "lagu galau",
                    "Netral": "lofi hip hop instrumental", "Positif": "lagu semangat",
                    "Sangat Positif": "lagu ceria playlist"
                }
                fallback_keyword = fallback_map.get(mood, "musik instrumental santai")
                log.info("Pencarian AI gagal, mencoba fallback", fallback_keyword=fallback_keyword)
                search_results_fallback = ytmusic.search(query=fallback_keyword, filter="songs", limit=20)
                musics = _process_search_results(search_results_fallback)
        except Exception as e:
            log.error("Gagal mendapatkan rekomendasi cerdas", error=str(e))

    if not musics:
        try:
            log.warning("Semua rekomendasi gagal, memberikan daftar default.")
            default_search_results = ytmusic.search(query="top hits indonesia", filter="songs", limit=20)
            musics = _process_search_results(default_search_results)
        except Exception as e:
            log.error("Gagal mendapatkan rekomendasi default", error=str(e))
            musics = []

    return musics
