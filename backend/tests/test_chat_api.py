import pytest
from app.schemas.plan import CommunicationTechnique, ConversationPlan
from app.services.planner_service import PlannerService
from app.services.generator_service import GeneratorService


def test_chat_flow_success(client):
    client_app, session_local = client

    class DummyPlanner:
        async def get_plan(self, *args, **kwargs):
            return ConversationPlan(technique=CommunicationTechnique.INFORMATION)

    class DummyGenerator:
        async def generate_response(self, plan, history, emotion):
            return "hello from ai"

    from app.main import app

    app.dependency_overrides[PlannerService] = lambda: DummyPlanner()
    app.dependency_overrides[GeneratorService] = lambda: DummyGenerator()

    response = client_app.post("/api/v1/chat/", json={"message": "Hi"})
    assert response.status_code == 200
    data = response.json()
    assert data["content"] == "hello from ai"
    assert data["sender_type"] == "ai"
    assert data["ai_technique"] == "information"

    from app.models.chat import ChatMessage

    db = session_local()
    try:
        msgs = db.query(ChatMessage).all()
        assert len(msgs) == 2
    finally:
        db.close()

    app.dependency_overrides.pop(PlannerService, None)
    app.dependency_overrides.pop(GeneratorService, None)


def test_get_latest_journal_returns_newest_entry(client):
    """Ensure get_latest_journal fetches the most recent journal content."""
    client_app, session_local = client

    db = session_local()
    from app import crud, models, schemas

    try:
        user = db.query(models.User).first()
        crud.journal.create_with_owner(
            db,
            obj_in=schemas.JournalCreate(title="old", content="first", mood="ok"),
            owner_id=user.id,
        )
        crud.journal.create_with_owner(
            db,
            obj_in=schemas.JournalCreate(title="new", content="second", mood="ok"),
            owner_id=user.id,
        )
        from app.api.v1.chat import get_latest_journal

        latest = get_latest_journal(db, user)
        assert latest == "second"
    finally:
        db.close()


def test_flag_and_delete_message(client):
    client_app, session_local = client

    class DummyPlanner:
        async def get_plan(self, *args, **kwargs):
            return ConversationPlan(technique=CommunicationTechnique.INFORMATION)

    class DummyGenerator:
        async def generate_response(self, plan, history, emotion):
            return "hi ai"

    from app.main import app

    app.dependency_overrides[PlannerService] = lambda: DummyPlanner()
    app.dependency_overrides[GeneratorService] = lambda: DummyGenerator()

    post_resp = client_app.post("/api/v1/chat/", json={"message": "Hello"})
    msg_id = post_resp.json()["id"]

    flag_resp = client_app.patch(f"/api/v1/chat/{msg_id}/flag", json={"flag": True})
    assert flag_resp.status_code == 200
    assert flag_resp.json()["is_flagged"] is True

    delete_resp = client_app.delete(f"/api/v1/chat/{msg_id}")
    assert delete_resp.status_code == 200

    from app.models.chat import ChatMessage

    db = session_local()
    try:
        assert db.query(ChatMessage).filter(ChatMessage.id == msg_id).first() is None
    finally:
        db.close()

    app.dependency_overrides.pop(PlannerService, None)
    app.dependency_overrides.pop(GeneratorService, None)


def test_flag_missing_message_returns_404(client):
    client_app, _ = client

    response = client_app.patch("/api/v1/chat/9999/flag", json={"flag": True})
    assert response.status_code == 404
