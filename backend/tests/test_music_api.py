from ytmusicapi import YTMusic
from app import crud
from app.services.music_keyword_service import MusicKeywordService
from app.models.journal import Journal


def test_music_endpoint_returns_list(client, monkeypatch):
    def fake_search(self, query, filter="songs", limit=20):
        return [{"title": "Song", "videoId": "abc123"}]

    def fake_get_song(self, videoId):
        return {
            "streamingData": {
                "adaptiveFormats": [
                    {"url": "https://audio.example/abc123.m4a", "mimeType": "audio/mp4"}
                ]
            }
        }

    monkeypatch.setattr(YTMusic, "search", fake_search)
    monkeypatch.setattr(YTMusic, "get_song", fake_get_song)

    client_app, _ = client
    resp = client_app.get("/api/v1/music?mood=test")
    assert resp.status_code == 200
    data = resp.json()
    assert isinstance(data, list)
    assert data[0]["url"] == "https://audio.example/abc123.m4a"


def test_music_recommend_uses_keyword_and_journals(client, monkeypatch):
    captured = {}

    def fake_get_multi_by_owner(db, owner_id: int, skip: int = 0, limit: int = 100):
        captured["limit"] = limit
        return [Journal(content=f"j{i}") for i in range(3)]

    async def fake_generate_keyword(self, journals):
        captured["journals"] = journals
        return "lofi"

    def fake_search(self, query, filter="songs", limit=20):
        captured["query"] = query
        return [{"title": "Song", "videoId": "xyz"}]

    def fake_get_song(self, videoId):
        return {
            "streamingData": {
                "adaptiveFormats": [
                    {"url": "https://audio.example/xyz.m4a", "mimeType": "audio/mp4"}
                ]
            }
        }

    monkeypatch.setattr(crud.journal, "get_multi_by_owner", fake_get_multi_by_owner)
    monkeypatch.setattr(MusicKeywordService, "generate_keyword", fake_generate_keyword)
    monkeypatch.setattr(YTMusic, "search", fake_search)
    monkeypatch.setattr(YTMusic, "get_song", fake_get_song)

    client_app, _ = client
    resp = client_app.get("/api/v1/music/recommend")

    assert resp.status_code == 200
    data = resp.json()
    assert data[0]["url"] == "https://audio.example/xyz.m4a"
    assert captured["limit"] == 5
    assert len(captured["journals"]) == 3
    assert captured["query"] == "lofi"


def test_music_recommend_returns_empty_list_when_no_results(client, monkeypatch):
    def fake_get_multi_by_owner(db, owner_id: int, skip: int = 0, limit: int = 100):
        return []

    async def fake_generate_keyword(self, journals):
        return "lofi"

    def fake_search(self, query, filter="songs", limit=20):
        return []

    monkeypatch.setattr(crud.journal, "get_multi_by_owner", fake_get_multi_by_owner)
    monkeypatch.setattr(MusicKeywordService, "generate_keyword", fake_generate_keyword)
    monkeypatch.setattr(YTMusic, "search", fake_search)

    client_app, _ = client
    resp = client_app.get("/api/v1/music/recommend")

    assert resp.status_code == 200
    assert resp.json() == []

