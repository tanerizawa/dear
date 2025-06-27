# backend/app/api/v1/music.py (Versi Final dengan Logika VideoID)

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

# --- Blok Inisialisasi (Sudah Benar dan Dipertahankan) ---
OAUTH_PATH = Path(__file__).resolve().parent.parent.parent.parent / "oauth.json"
if not OAUTH_PATH.exists():
    raise FileNotFoundError(
        f"Authentication file not found at {OAUTH_PATH}. "
        "Please run 'ytmusicapi oauth' in your terminal in the 'backend' directory."
    )

if not settings.OAUTH_CLIENT_ID or not settings.OAUTH_CLIENT_SECRET:
    raise ValueError(
        "OAUTH_CLIENT_ID and OAUTH_CLIENT_SECRET must be set in your .env file."
    )

try:
    creds = OAuthCredentials(
        client_id=settings.OAUTH_CLIENT_ID,
        client_secret=settings.OAUTH_CLIENT_SECRET,
    )
    ytmusic = YTMusic(
        str(OAUTH_PATH),
        oauth_credentials=creds,
        language='en',
        location='ID'
    )
    log.info("YTMusic berhasil diinisialisasi dengan otentikasi penuh.")
except Exception as e:
    log.error("Gagal menginisialisasi YTMusic. Periksa kredensial Anda.", error=str(e))
    raise e
# --- Akhir Blok Inisialisasi ---


# --- PERBAIKAN LOGIKA FUNDAMENTAL DI SINI ---
def _process_search_results(search_results: list) -> list[schemas.AudioTrack]:
    """
    Helper function to process search results and return a list of AudioTrack.
    Tugas fungsi ini HANYA untuk memformat hasil pencarian, BUKAN untuk mendapatkan URL stream.
    """
    musics: list[schemas.AudioTrack] = []
    if not search_results:
        return musics

    for idx, track in enumerate(search_results, start=1):
        video_id = track.get("videoId")
        title = track.get("title")

        # Kita hanya butuh videoId dan title. URL akan dibuat di sisi klien.
        if video_id and title:
            # Mengirim videoId sebagai 'url' untuk sementara, klien akan menanganinya.
            # Atau klien bisa langsung menggunakan field 'videoId' jika modelnya diubah.
            # Untuk kompatibilitas, kita akan tetap mengisi field 'url' dengan videoId.
            musics.append(schemas.AudioTrack(id=idx, title=title, url=video_id))

    return musics
# --- AKHIR PERBAIKAN ---


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
        db=db, owner_id=current_user.id, limit=5, order_by="created_at desc"
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
