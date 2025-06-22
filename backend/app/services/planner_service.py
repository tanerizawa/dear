
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

    async def plan_conversation_strategy(self, context: str, user_message: str) -> ConversationPlan:
        self.log.info("planning_conversation", user_message=user_message)

        available_techniques = ", ".join(f"'{t.value}'" for t in CommunicationTechnique if t != CommunicationTechnique.UNKNOWN)

        prompt = f"""You are the 'director' persona. Your task is to choose one optimal communication technique from a given list to guide an empathetic AI assistant's response. Analyze the user's message within the provided context.

        Context: {context}
        User Message: "{user_message}"

        Based on the above, which of the following techniques is most appropriate for the next response?
        Available techniques: [{available_techniques}]

        Respond with a JSON object containing only one key: "technique". The value must be one of the available techniques.
        """

        messages = [{"role": "system", "content": prompt}]

        try:
            data = await self._call_openrouter(self.settings.PLANNER_MODEL_NAME, messages)
            content = data["choices"][0]["message"]["content"]
            plan_data = json.loads(content)

            # Validasi manual sederhana
            if 'technique' not in plan_data or plan_data['technique'] not in [t.value for t in CommunicationTechnique]:
                raise ValueError("Invalid technique returned by AI")

            return ConversationPlan(**plan_data)
        except Exception as e:
            self.log.error("planner_service_error", error=str(e))
            # Fallback jika planner gagal
            return ConversationPlan(technique=CommunicationTechnique.UNKNOWN)