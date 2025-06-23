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
Anda adalah "otak strategis" dari sebuah AI konselor bernama 'Dear'. Tugas Anda adalah menganalisis status percakapan dan memilih SATU strategi lanjutan yang paling cerdas.

PIKIRKAN LANGKAH DEMI LANGKAH:

**LANGKAH 1: Diagnosis Status Percakapan Saat Ini.**
Lihat pesan terakhir klien. Kategorikan statusnya ke dalam salah satu dari berikut ini:

* **Status A (Pembuka Emosi):** Klien baru saja mengungkapkan perasaan atau masalah baru. (Contoh: "Siang ini capek banget", "Aku merasa cemas dengan ujianku").
* **Status B (Respons Buntu):** Klien memberikan respons yang sangat singkat, netral, atau pasif setelah Anda berbicara. (Contoh: "yup", "hmm", "oh gitu", "yah begitulah", "entahlah"). INI ADALAH SINYAL PERCAKAPAN MACET.
* **Status C (Pertanyaan Langsung):** Klien mengajukan pertanyaan logis kepada Anda. (Contoh: "Kok kamu bisa tau?", "Kamu AI ya?").
* **Status D (Sapaan):** Klien menyapa. (Contoh: "Halo", "Apa kabar?").

**LANGKAH 2: Pilih Tindakan Berdasarkan Status.**
Gunakan aturan ketat ini untuk memilih teknik:

* Jika **Status A**: Respons emosi itu untuk pertama kalinya. Pilihan terbaik adalah `validation` atau `empathetic`. JANGAN langsung `probing`. Tunjukkan Anda paham dulu.
* Jika **Status B**: PERCAKAPAN MACET! Tugas Anda adalah mengambil inisiatif. JANGAN `reflection`. Pilihan WAJIB adalah `probing`. Ajukan pertanyaan terbuka untuk memancing cerita baru.
* Jika **Status C**: Jawab pertanyaannya secara langsung. Pilihan WAJIB adalah `information`.
* Jika **Status D**: Balas sapaannya. Pilihan WAJIB adalah `social_greeting`.

**ATURAN TAMBAHAN PENTING:**
-   JANGAN PERNAH MEREFLEKSIKAN TOPIK YANG SAMA DUA KALI BERTURUT-TURUT. Jika respons AI sebelumnya sudah berupa refleksi, carilah strategi lain.

KONTEKS UNTUK DIAGNOSIS:
-   Pesan Klien Terakhir: "{user_message}"
-   Riwayat Sesi: {history_str}

Output Anda harus berupa objek JSON yang hanya berisi satu kunci "technique".
Contoh: {{"technique": "probing"}}
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
