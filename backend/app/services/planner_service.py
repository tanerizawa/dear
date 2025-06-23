
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
        chat_history: List[Dict[str, str]],
        latest_journal: str,
    ) -> ConversationPlan:
        """Determine which counseling technique Dear should apply next."""

        self.log.info("planning_conversation", user_message=user_message)

        available_techniques = ", ".join(
            f"'{t.value}'" for t in CommunicationTechnique if t != CommunicationTechnique.UNKNOWN
        )

        history_str = "\n".join(f"{m['role']}: {m['content']}" for m in chat_history)

        prompt = (
            "You are Dear's planning counselor. Choose the best next counseling "
            "technique for Dear to use when replying.\n"
            f"Strategies: [{available_techniques}]\n\n"
            f"Latest journal entry: {latest_journal or 'None'}\n"
            f"Chat history:\n{history_str}\n"
            f"User message: {user_message}\n\n"
            "Respond ONLY with a JSON object like {\"technique\": \"<name>\"}."
        )

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
