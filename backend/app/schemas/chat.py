from pydantic import BaseModel, ConfigDict
from typing import Optional
from datetime import datetime

# Skema dasar untuk field yang sama di semua varian
class ChatMessageBase(BaseModel):
    content: str
    sender_type: str  # Anda bisa menggunakan Enum di sini jika mau
    ai_technique: Optional[str] = None


# Skema untuk membuat pesan baru (digunakan oleh CRUD)
class ChatMessageCreate(ChatMessageBase):
    pass


# Skema untuk memperbarui pesan (saat ini tidak digunakan, tapi ada untuk kelengkapan)
class ChatMessageUpdate(BaseModel):
    pass


# Skema untuk properti yang ada di database tetapi tidak selalu dikirim ke klien
class ChatMessageInDBBase(ChatMessageBase):
    id: int
    owner_id: int
    created_at: datetime

    # Perbaikan untuk warning 'orm_mode'
    model_config = ConfigDict(from_attributes=True)


# Skema utama yang akan dikembalikan oleh API (model respons)
class ChatMessage(ChatMessageInDBBase):
    pass


# Skema yang ada sebelumnya untuk menerima permintaan dari klien
class ChatRequest(BaseModel):
    message: str
