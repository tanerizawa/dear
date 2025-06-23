# File: backend/app/api/v1/chat.py

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app import models, schemas, crud, dependencies
from app.services.planner_service import PlannerService
from app.services.generator_service import GeneratorService

router = APIRouter()

def build_context(db: Session, user: models.User) -> str:
    """Membangun string konteks dari data pengguna."""
    # Ambil 1 jurnal terakhir
    latest_journal = crud.journal.get_multi_by_owner(db, owner_id=user.id, limit=1)
    journal_context = f"Latest journal entry: '{latest_journal[0].content}'" if latest_journal else "No recent journal entries."

    # Tambahkan data profil jika ada
    # profile_context = f"User profile (MBTI): {user.profile.mbti}" if user.profile else ""

    return f"{journal_context}"

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
    user_message_obj = schemas.chat.ChatMessageCreate(content=chat_in.message, sender_type='user')
    crud.chat_message.create_with_owner(db, obj_in=user_message_obj, owner_id=current_user.id)

    # 2. Ambil riwayat chat & bangun konteks
    history_db = crud.chat_message.get_multi_by_owner(db, owner_id=current_user.id, limit=6)
    history_formatted = [{"role": msg.sender_type, "content": msg.content} for msg in reversed(history_db)]
    context_str = build_context(db, user=current_user)

    # 3. Panggil Planner untuk mendapatkan rencana
    conversation_plan = await planner.plan_conversation_strategy(context_str, chat_in.message)

    # 4. Panggil Generator untuk mendapatkan respons final
    final_response = await generator.generate_response(conversation_plan, history_formatted, chat_in.message)

    # 5. Simpan respons AI ke database
    ai_message_obj = schemas.chat.ChatMessageCreate(
        content=final_response,
        sender_type='ai',
        ai_technique=conversation_plan.technique.value
    )
    ai_message_db = crud.chat_message.create_with_owner(db, obj_in=ai_message_obj, owner_id=current_user.id)

    return ai_message_db
