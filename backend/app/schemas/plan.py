
from enum import Enum
from pydantic import BaseModel, Field

class CommunicationTechnique(str, Enum):
    PROBING = "probing"
    CLARIFYING = "clarifying"
    PARAPHRASING = "paraphrasing"
    REFLECTING = "reflecting"
    OPEN_ENDED_QUESTIONS = "open_ended_questions"
    CLOSED_ENDED_QUESTIONS = "closed_ended_questions"
    SUMMARIZING = "summarizing"
    CONFRONTATION = "confrontation"
    REASSURANCE_ENCOURAGEMENT = "reassurance_encouragement"
    # Tambahkan 'UNKNOWN' sebagai fallback
    UNKNOWN = "unknown"

class ConversationPlan(BaseModel):
    technique: CommunicationTechnique = Field(..., description="The communication technique chosen by the planner AI.")