from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app import models, schemas, crud, dependencies
from app.models.chat import SenderType
from app.services.planner_service import PlannerService
from app.services.generator_service import GeneratorService

router = APIRouter()

def get_latest_journal(db: Session, user: models.User) -> str:
    """Return the latest journal content or empty string."""
    latest_journal = crud.journal.get_multi_by_owner(db, owner_id=user.id, limit=1)
    return latest_journal[0].content if latest_journal else ""

@router.post("/", response_model=schemas.chat.ChatMessage)
async def handle_chat_message(
        *,
        db: Session = Depends(dependencies.get_db),
        chat_in: schemas.chat.ChatRequest,
        current_user: models.User = Depends(dependencies.get_current_user),
        planner: PlannerService = Depends(),
        generator: GeneratorService = Depends(),
):
    """
    Orkestrasi alur chat Planner/Generator.
    """
    # 1. Simpan pesan dari pengguna
    user_message_obj = schemas.chat.ChatMessageCreate(
        content=chat_in.message,
        sender_type=SenderType.USER
    )
    crud.chat_message.create_with_owner(db, obj_in=user_message_obj, owner_id=current_user.id)

    # 2. Ambil riwayat chat & bangun konteks
    # Load a bit more history to give the Planner and Generator
    # better conversational context
    history_db = crud.chat_message.get_multi_by_owner(
        db, owner_id=current_user.id, limit=10
    )

    # List of {role, content} dicts for the GeneratorService
    history_formatted = [
        {"role": msg.sender_type.value, "content": msg.content}
        for msg in reversed(history_db)
    ]

    # List of just message strings for the PlannerService
    chat_history = [msg.content for msg in reversed(history_db)]

    latest_journal = get_latest_journal(db, user=current_user)

    # 3. Panggil Planner untuk mendapatkan rencana
    conversation_plan = await planner.get_plan(
        chat_in.message, chat_history, latest_journal
    )

    # 4. Panggil Generator untuk mendapatkan respons final
    final_response = await generator.generate_response(conversation_plan, history_formatted, chat_in.message)

    # 5. Simpan respons AI ke database
    ai_message_obj = schemas.chat.ChatMessageCreate(
        content=final_response,
        sender_type=SenderType.AI,
        ai_technique=conversation_plan.technique.value
    )
    ai_message_db = crud.chat_message.create_with_owner(db, obj_in=ai_message_obj, owner_id=current_user.id)

    return ai_message_db
