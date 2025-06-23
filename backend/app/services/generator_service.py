import httpx
import structlog
from typing import List, Dict
from fastapi import Depends, HTTPException
from app.core.config import Settings, settings
from app.schemas.plan import ConversationPlan

class GeneratorService:
    def __init__(self, settings: Settings = Depends(lambda: settings)):
        self.settings = settings
        self.api_base_url = "https://openrouter.ai/api/v1"
        self.log = structlog.get_logger(__name__)

        # Brief instructions for applying each communication technique
        self.TOOLBOX = {
            "social_greeting": "start with a warm, friendly greeting to set a comfortable tone.",
            "probing": "ask a short clarifying question to gently explore the user's message.",
            "validation": "acknowledge that the user's feelings or viewpoint make sense.",
            "empathetic": "show empathy so the user feels heard and understood.",
            "reflection": "mirror back the main feeling or idea you heard.",
            "summarizing": "briefly recap the key points shared by the user.",
            "clarifying": "confirm your understanding of what the user said.",
            "information": "Jawab pertanyaan pengguna secara langsung, singkat, dan jujur berdasarkan riwayat percakapan kita.",
            "unknown": "ask a simple open question like 'Could you tell me more?'"
        }

    async def _call_openrouter(self, model: str, messages: List[Dict[str, str]]) -> Dict:
        headers = {"Authorization": f"Bearer {self.settings.OPENROUTER_API_KEY}"}
        json_data = {"model": model, "messages": messages}
        async with httpx.AsyncClient() as client:
            response = await client.post(f"{self.api_base_url}/chat/completions", headers=headers, json=json_data, timeout=20.0)
            response.raise_for_status()
            return response.json()

    async def generate_response(self, plan: ConversationPlan, history: List[Dict], user_message: str) -> str:
        self.log.info("generating_response", technique=plan.technique.value)

        technique_instruction = self.TOOLBOX.get(plan.technique.value, self.TOOLBOX["unknown"])

        prompt = (
            "Kamu adalah \"Dear\", teman bicara virtual yang suportif dan responsif secara emosional. "
            "Jawablah dalam Bahasa Indonesia yang hangat, santai, dan penuh empati. "
            "Gunakan gaya percakapan sehari-hari yang cocok untuk konteks psikologis pengguna. "
            "Jika pengguna terlihat sedih, cemas, atau lelah, berikan respons yang menenangkan dan tidak menggurui. "
            "Hindari bahasa yang terlalu formal atau terjemahan kaku. Gunakan nada yang manusiawi dan kadang bercanda ringan.\n\n"
            "Tugasmu hanya memberikan balasan singkat berdasarkan pesan terakhir pengguna dengan teknik komunikasi yang ditetapkan.\n\n"
            "**INSTRUKSI PENTING:**\n"
            "1. Gunakan HANYA teknik yang diberikan.\n"
            "2. JANGAN memberi nasihat.\n"
            "3. JANGAN menilai atau menganalisis.\n"
            "4. Buatlah singkat dan natural.\n"
            "5. Jawab hanya berdasarkan informasi yang diberikan pengguna.\n"
            "6. Jangan menebak atau menambahkan detail yang tidak disebutkan.\n"
            "7. Keep replies 1-2 sentences long.\n\n"
            f"**Teknik:** {plan.technique.value}\n"
            f"**Cara menerapkan:** {technique_instruction}"
        )

        messages = [{"role": "system", "content": prompt}]
        messages.extend(history)
        messages.append({"role": "user", "content": user_message})

        try:
            data = await self._call_openrouter(self.settings.GENERATOR_MODEL_NAME, messages)
            return data["choices"][0]["message"]["content"].strip()
        except Exception as e:
            self.log.error("generator_service_error", error=str(e))
            return "I'm here to listen. Could you tell me a little more about that?"
