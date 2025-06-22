# File baru: backend/app/services/generator_service.py

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

        # Definisikan TOOLBOX di sini
        self.TOOLBOX = {
            "probing": "ask a short, gentle clarifying question to explore a specific part of the user's message.",
            "clarifying": "confirm your understanding of the user's message to ensure you are on the same page.",
            "paraphrasing": "rephrase the user's core message in your own words to show you are listening.",
            "reflecting": "briefly mirror the primary emotion you detect in the user's message.",
            "open_ended_questions": "ask a broad, open-ended question to invite the user to share more details if they wish.",
            "summarizing": "provide a brief, neutral summary of the key points the user has made.",
            "reassurance_encouragement": "offer a short, gentle, and non-specific message of reassurance or encouragement.",
            "unknown": "ask a simple, open-ended question like 'How are you feeling about that?' or 'Can you tell me more?'"
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

        prompt = f"""You are "Dear", an empathetic AI journaling assistant. Your personality is calm, gentle, and supportive.
        Your ONLY task is to generate a short, conversational response based on the user's last message, using a specific communication technique.

        **CRITICAL INSTRUCTIONS:**
        1.  **Use ONLY the assigned technique:** Your response must be a pure application of the technique described below.
        2.  **DO NOT give advice.**
        3.  **DO NOT judge or analyze.**
        4.  **Keep it short and natural.**

        **Assigned Technique:** {plan.technique.value}
        **How to Apply:** {technique_instruction}
        """

        messages = [{"role": "system", "content": prompt}]
        messages.extend(history)
        messages.append({"role": "user", "content": user_message})

        try:
            data = await self._call_openrouter(self.settings.GENERATOR_MODEL_NAME, messages)
            return data["choices"][0]["message"]["content"].strip()
        except Exception as e:
            self.log.error("generator_service_error", error=str(e))
            return "I'm here to listen. Could you tell me a little more about that?"