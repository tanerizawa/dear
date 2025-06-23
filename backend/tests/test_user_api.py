from app.models.user import User


def test_get_current_user(client):
    client_app, _ = client
    response = client_app.get("/api/v1/users/me")
    assert response.status_code == 200
    data = response.json()
    assert data["username"] == "tester"
    assert data["email"] == "tester@example.com"
    assert data["id"] == 1

