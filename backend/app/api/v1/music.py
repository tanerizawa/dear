# app/api/v1/endpoints/music.py

from fastapi import APIRouter, Depends, HTTPException, Query
from ytmusicapi import YTMusic
from ..deps import get_current_active_user
from app.models.user import User
import yt_dlp
from app import schemas

router = APIRouter()
ytmusic = YTMusic()

@router.get("/", response_model=list[schemas.AudioTrack])
def search_music(
    mood: str = Query(..., min_length=1),
    current_user: User = Depends(get_current_active_user)
):
    if not mood:
        raise HTTPException(status_code=400, detail="Mood parameter is required")

    search_results = ytmusic.search(query=mood, filter="songs", limit=20)

    ydl_opts = {
        'format': 'bestaudio[ext=m4a]/bestaudio/best',
        'quiet': True,
        'no_warnings': True,
        'force_generic_extractor': True,
    }

    musics = []
    video_id_counter = 1

    for track in search_results:
        try:
            video_id = track.get("videoId")
            title = track.get("title")

            if not video_id or not title:
                continue

            url = f"https://www.youtube.com/watch?v={video_id}"
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=False)
                streaming_url = info.get("url")

            if streaming_url:
                musics.append(
                    schemas.AudioTrack(
                        id=video_id_counter,
                        title=title,
                        url=streaming_url
                    )
                )
                video_id_counter += 1

        except Exception as e:
            print(f"Could not process video {track.get('videoId')}: {e}")
            continue

    if not musics:
        raise HTTPException(status_code=404, detail="No music found for the given mood")

    return musics
