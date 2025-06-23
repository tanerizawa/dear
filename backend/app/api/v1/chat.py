from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app import models, schemas, crud, dependencies
from app.models.chat import SenderType
from app.services.planner_service import PlannerService
from app.services.generator_service import GeneratorService
from app.services.emotion_service import EmotionService

router = APIRouter()


def get_latest_journal(db: Session, user: models.User) -> str:
    """Return the latest journal content or empty string."""
    # CRUDJournal.get_multi_by_owner now returns entries ordered by
    # ``created_at`` descending so limiting to one gives the latest journal.
    journals = crud.journal.get_multi_by_owner(db, owner_id=user.id, limit=1)
    return journals[0].content if journals else ""


@router.post("/", response_model=schemas.chat.ChatMessage)
async def handle_chat_message(
    *,
    db: Session = Depends(dependencies.get_db),
    chat_in: schemas.chat.ChatRequest,
    current_user: models.User = Depends(dependencies.get_current_user),
    planner: PlannerService = Depends(),
    generator: GeneratorService = Depends(),
    emotion_service: EmotionService = Depends(),
):
    """
    Orkestrasi alur chat Planner/Generator.
    """
    # 1. Analisis emosi dan simpan pesan dari pengguna
    emotion_label = emotion_service.detect_emotion(chat_in.message)
    user_message_obj = schemas.chat.ChatMessageCreate(
        content=chat_in.message,
        sender_type=SenderType.USER,
        emotion=emotion_label,
    )
    crud.chat_message.create_with_owner(
        db, obj_in=user_message_obj, owner_id=current_user.id
    )

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
        chat_in.message,
        chat_history,
        latest_journal,
        emotion_label,
    )

    # 4. Panggil Generator untuk mendapatkan respons final
    # history_formatted already contains the latest user message so we don't
    # need to send it separately to the GeneratorService
    final_response = await generator.generate_response(
        conversation_plan,
        history_formatted,
        emotion_label,
    )

    # 5. Simpan respons AI ke database
    ai_message_obj = schemas.chat.ChatMessageCreate(
        content=final_response,
        sender_type=SenderType.AI,
        ai_technique=conversation_plan.technique.value,
    )
    ai_message_db = crud.chat_message.create_with_owner(
        db, obj_in=ai_message_obj, owner_id=current_user.id
    )

    return ai_message_db


@router.patch("/{id}/flag", response_model=schemas.chat.ChatMessage)
def update_chat_flag(
    *,
    db: Session = Depends(dependencies.get_db),
    id: int,
    flag_in: schemas.chat.ChatFlagUpdate,
    current_user: models.User = Depends(dependencies.get_current_user),
):
    message = crud.chat_message.set_flag(
        db, id=id, owner_id=current_user.id, flag=flag_in.flag
    )
    if not message:
        raise HTTPException(status_code=404, detail="Chat message not found")
    return message


@router.delete("/{id}", response_model=schemas.chat.ChatMessage)
def delete_chat_message(
    *,
    db: Session = Depends(dependencies.get_db),
    id: int,
    current_user: models.User = Depends(dependencies.get_current_user),
):
    message = crud.chat_message.remove(db, id=id, owner_id=current_user.id)
    if not message:
        raise HTTPException(status_code=404, detail="Chat message not found")
    return message
