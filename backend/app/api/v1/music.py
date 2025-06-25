from fastapi import APIRouter, Query
from ytmusicapi import YTMusic
from app import schemas

router = APIRouter()

@router.get("/", response_model=list[schemas.AudioTrack])
def search_music(mood: str = Query(..., min_length=1)):
    results = YTMusic().search(mood, filter="songs")
    tracks = []
    for idx, item in enumerate(results):
        title = item.get("title")
        video_id = item.get("videoId")
        if not title or not video_id:
            continue
        url = f"https://music.youtube.com/watch?v={video_id}"
        tracks.append(schemas.AudioTrack(id=idx + 1, title=title, url=url))
    return tracks
