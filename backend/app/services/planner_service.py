import httpx
import json
import structlog
from typing import List, Dict
from fastapi import Depends, HTTPException
from app.core.config import Settings, settings
from app.schemas.plan import CommunicationTechnique, ConversationPlan

class PlannerService:
    def __init__(self, settings: Settings = Depends(lambda: settings)):
        self.settings = settings
        self.api_base_url = "https://openrouter.ai/api/v1"
        self.log = structlog.get_logger(__name__)

    async def _call_openrouter(self, model: str, messages: List[Dict[str, str]]) -> Dict:
        headers = {
            "Authorization": f"Bearer {self.settings.OPENROUTER_API_KEY}",
            "HTTP-Referer": self.settings.APP_SITE_URL,
            "X-Title": self.settings.APP_NAME,
        }
        json_data = {"model": model, "messages": messages, "response_format": {"type": "json_object"}}

        async with httpx.AsyncClient() as client:
            response = await client.post(f"{self.api_base_url}/chat/completions", headers=headers, json=json_data, timeout=20.0)
            response.raise_for_status()
            return response.json()


    async def get_plan(
        self,
        user_message: str,
        chat_history: List[str],
        latest_journal: str,
    ) -> ConversationPlan:
        """Determine which counseling technique Dear should apply next."""

        self.log.info("planning_conversation", user_message=user_message)

        available_techniques = ", ".join(
            f"'{t.value}'" for t in CommunicationTechnique if t != CommunicationTechnique.UNKNOWN
        )

        history_str = "\n".join(chat_history)

        prompt = f"""
Anda adalah konselor perencana untuk Dear. Analisis riwayat percakapan dan pesan pengguna, kemudian pilih SATU teknik komunikasi yang paling sesuai.

Kategorikan pesan pengguna ke dalam salah satu dari empat tipe berikut:
1. Salam atau basa-basi.
2. Curhat atau ungkapan perasaan.
3. Pertanyaan informasi.
4. Tidak jelas atau di luar konteks.

Gunakan toolbox teknik di bawah ini dan pilih satu strategi saja:
{available_techniques}
- information: Jawab pertanyaan pengguna secara langsung, singkat, dan jujur.

Entri jurnal terbaru: {latest_journal or 'Tidak ada'}
Riwayat chat:
{history_str}
Pesan pengguna: {user_message}

Balas HANYA dengan objek JSON sederhana seperti {{"technique": "<name>"}}.
"""

        messages = [{"role": "system", "content": prompt}]

        try:
            data = await self._call_openrouter(self.settings.PLANNER_MODEL_NAME, messages)
            content = data["choices"][0]["message"]["content"]
            plan_data = json.loads(content)

            if (
                "technique" not in plan_data
                or plan_data["technique"] not in [t.value for t in CommunicationTechnique]
            ):
                raise ValueError("Invalid technique returned by AI")

            return ConversationPlan(**plan_data)
        except Exception as e:
            self.log.error("planner_service_error", error=str(e))
            return ConversationPlan(technique=CommunicationTechnique.UNKNOWN)
