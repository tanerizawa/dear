# app/api/v1/endpoints/music.py

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from ytmusicapi import YTMusic

from app import crud, models, schemas, dependencies
from app.services.music_keyword_service import MusicKeywordService

router = APIRouter()
ytmusic = YTMusic()

@router.get("/", response_model=list[schemas.AudioTrack])
def search_music(
    mood: str = Query(..., min_length=1),
    current_user: models.User = Depends(dependencies.get_current_user)
):
    if not mood:
        raise HTTPException(status_code=400, detail="Mood parameter is required")

    search_results = ytmusic.search(query=mood, filter="songs", limit=20)

    musics = []

    for idx, track in enumerate(search_results, start=1):
        video_id = track.get("videoId")
        title = track.get("title")
        if not video_id or not title:
            continue

        try:
            song = ytmusic.get_song(video_id)
            formats = song.get("streamingData", {}).get("adaptiveFormats", [])
            audio_format = next(
                (
                    f for f in formats
                    if f.get("mimeType", "").startswith("audio/")
                ),
                None,
            )
            streaming_url = audio_format.get("url") if audio_format else None
        except Exception as e:
            print(f"Could not process video {video_id}: {e}")
            continue

        if streaming_url:
            musics.append(
                schemas.AudioTrack(id=idx, title=title, url=streaming_url)
            )

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
        db=db,
        owner_id=current_user.id,
        limit=5,
    )
    keyword = await keyword_service.generate_keyword(journals)

    search_results = ytmusic.search(query=keyword, filter="songs", limit=20)

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
            print(f"Could not process video {video_id}: {e}")
            continue

        if streaming_url:
            musics.append(schemas.AudioTrack(id=idx, title=title, url=streaming_url))

    if not musics:
        raise HTTPException(status_code=404, detail="No music found")

    return musics
