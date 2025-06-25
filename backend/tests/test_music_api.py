from ytmusicapi import YTMusic


def test_music_endpoint_returns_list(client, monkeypatch):
    def fake_search(self, query, filter="songs", limit=20):
        return [{"title": "Song", "videoId": "abc123"}]

    monkeypatch.setattr(YTMusic, "search", fake_search)

    client_app, _ = client
    resp = client_app.get("/api/v1/music?mood=test")
    assert resp.status_code == 200
    data = resp.json()
    assert isinstance(data, list)
    assert data[0]["url"].endswith("abc123")
