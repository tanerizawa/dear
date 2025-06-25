from ytmusicapi import YTMusic


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
