#!/bin/bash

# Skrip untuk membuat ulang struktur backend FastAPI
# agar selaras dengan arsitektur berlapis modern.
# Jalankan skrip ini dari dalam direktori root backend.

echo "Memulai restrukturisasi backend..."
BASE_PATH="app"

# --- 1. Buat Semua Struktur Direktori ---
echo "Membuat direktori baru untuk 'app'..."
mkdir -p "$BASE_PATH/api/v1"
mkdir -p "$BASE_PATH/core"
mkdir -p "$BASE_PATH/crud"
mkdir -p "$BASE_PATH/db"
mkdir -p "$BASE_PATH/models"
mkdir -p "$BASE_PATH/schemas"
mkdir -p "$BASE_PATH/services"

# --- 2. Isi File-file Python ---

# ==================================
# PAKET: core
# ==================================
echo "Menulis file di paket 'core'..."
cat << 'EOF' > "$BASE_PATH/core/config.py"
import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str = os.environ.get("DATABASE_URL", "sqlite:///./test.db")
    SECRET_KEY: str = os.environ.get("SECRET_KEY", "supersecretkey")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    class Config:
        env_file = ".env"

settings = Settings()
EOF

cat << 'EOF' > "$BASE_PATH/core/security.py"
from datetime import datetime, timedelta
from typing import Any, Union
from passlib.context import CryptContext
from jose import jwt
from .config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def create_access_token(subject: Union[str, Any], expires_delta: timedelta = None) -> str:
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)

    to_encode = {"exp": expire, "sub": str(subject)}
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt
EOF

# ==================================
# PAKET: db & models
# ==================================
echo "Menulis file di paket 'db' dan 'models'..."
cat << 'EOF' > "$BASE_PATH/db/session.py"
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

engine = create_engine(settings.DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
EOF

cat << 'EOF' > "$BASE_PATH/db/base_class.py"
from sqlalchemy.ext.declarative import as_declarative, declared_attr

@as_declarative()
class Base:
    id: Any
    __name__: str

    # Generate __tablename__ automatically
    @declared_attr
    def __tablename__(cls) -> str:
        return cls.__name__.lower() + "s"
EOF

cat << 'EOF' > "$BASE_PATH/models/user.py"
from sqlalchemy import Column, Integer, String, Boolean
from app.db.base_class import Base

class User(Base):
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean(), default=True)
EOF

cat << 'EOF' > "$BASE_PATH/models/journal.py"
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Float
from sqlalchemy.orm import relationship
from app.db.base_class import Base
import datetime

class Journal(Base):
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    content = Column(Text)
    mood = Column(String)
    sentiment_score = Column(Float)
    sentiment_label = Column(String)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship("User", back_populates="journals")
User.journals = relationship("Journal", order_by=Journal.id, back_populates="owner")
EOF

# ==================================
# PAKET: schemas
# ==================================
echo "Menulis file di paket 'schemas'..."
cat << 'EOF' > "$BASE_PATH/schemas/user.py"
from pydantic import BaseModel, EmailStr

class UserBase(BaseModel):
    email: EmailStr
    username: str

class UserCreate(UserBase):
    password: str

class UserUpdate(BaseModel):
    email: EmailStr | None = None
    username: str | None = None
    password: str | None = None

class UserInDB(UserBase):
    id: int
    is_active: bool
    class Config:
        orm_mode = True

class UserPublic(UserBase):
    id: int
EOF

cat << 'EOF' > "$BASE_PATH/schemas/journal.py"
from pydantic import BaseModel
from datetime import datetime

class JournalBase(BaseModel):
    title: str
    content: str
    mood: str

class JournalCreate(JournalBase):
    pass

class JournalUpdate(JournalBase):
    pass

class JournalInDB(JournalBase):
    id: int
    owner_id: int
    created_at: datetime
    sentiment_score: float | None = None
    sentiment_label: str | None = None
    class Config:
        orm_mode = True
EOF

cat << 'EOF' > "$BASE_PATH/schemas/token.py"
from pydantic import BaseModel

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenPayload(BaseModel):
    sub: int | None = None
EOF

cat << 'EOF' > "$BASE_PATH/schemas/chat.py"
from pydantic import BaseModel

class ChatMessage(BaseModel):
    message: str

class ChatResponse(BaseModel):
    reply: str
EOF


# ==================================
# PAKET: crud
# ==================================
echo "Menulis file di paket 'crud'..."
cat << 'EOF' > "$BASE_PATH/crud/base.py"
from typing import Any, Dict, Generic, List, Optional, Type, TypeVar, Union
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.db.base_class import Base

ModelType = TypeVar("ModelType", bound=Base)
CreateSchemaType = TypeVar("CreateSchemaType", bound=BaseModel)
UpdateSchemaType = TypeVar("UpdateSchemaType", bound=BaseModel)

class CRUDBase(Generic[ModelType, CreateSchemaType, UpdateSchemaType]):
    def __init__(self, model: Type[ModelType]):
        self.model = model

    def get(self, db: Session, id: Any) -> Optional[ModelType]:
        return db.query(self.model).filter(self.model.id == id).first()

    def get_multi(self, db: Session, *, skip: int = 0, limit: int = 100) -> List[ModelType]:
        return db.query(self.model).offset(skip).limit(limit).all()

    def create(self, db: Session, *, obj_in: CreateSchemaType) -> ModelType:
        obj_in_data = obj_in.dict()
        db_obj = self.model(**obj_in_data)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
EOF

cat << 'EOF' > "$BASE_PATH/crud/crud_user.py"
from sqlalchemy.orm import Session
from .base import CRUDBase
from app.models.user import User
from app.schemas.user import UserCreate, UserUpdate
from app.core.security import get_password_hash

class CRUDUser(CRUDBase[User, UserCreate, UserUpdate]):
    def get_by_email(self, db: Session, *, email: str) -> User | None:
        return db.query(User).filter(User.email == email).first()

    def create(self, db: Session, *, obj_in: UserCreate) -> User:
        db_obj = User(
            email=obj_in.email,
            username=obj_in.username,
            hashed_password=get_password_hash(obj_in.password),
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

user = CRUDUser(User)
EOF

cat << 'EOF' > "$BASE_PATH/crud/crud_journal.py"
from sqlalchemy.orm import Session
from .base import CRUDBase
from app.models.journal import Journal
from app.schemas.journal import JournalCreate, JournalUpdate

class CRUDJournal(CRUDBase[Journal, JournalCreate, JournalUpdate]):
    def create_with_owner(self, db: Session, *, obj_in: JournalCreate, owner_id: int) -> Journal:
        db_obj = Journal(**obj_in.dict(), owner_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def get_multi_by_owner(self, db: Session, *, owner_id: int, skip: int = 0, limit: int = 100) -> list[Journal]:
        return db.query(self.model).filter(Journal.owner_id == owner_id).offset(skip).limit(limit).all()

journal = CRUDJournal(Journal)
EOF

# ==================================
# PAKET: api & dependencies
# ==================================
echo "Menulis file di paket 'api'..."
cat << 'EOF' > "$BASE_PATH/dependencies.py"
from typing import Generator
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import jwt
from pydantic import ValidationError
from sqlalchemy.orm import Session

from app import crud, models, schemas
from app.core.config import settings
from app.db.session import SessionLocal

reusable_oauth2 = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/login")

def get_db() -> Generator:
    try:
        db = SessionLocal()
        yield db
    finally:
        db.close()

def get_current_user(db: Session = Depends(get_db), token: str = Depends(reusable_oauth2)) -> models.User:
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        token_data = schemas.TokenPayload(**payload)
    except (jwt.JWTError, ValidationError):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Could not validate credentials",
        )
    user = crud.user.get(db, id=token_data.sub)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user
EOF

cat << 'EOF' > "$BASE_PATH/api/v1/auth.py"
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app import crud, schemas
from app.core.security import create_access_token
from app.dependencies import get_db

router = APIRouter()

@router.post("/register", response_model=schemas.UserPublic)
def register(
    *,
    db: Session = Depends(get_db),
    user_in: schemas.UserCreate,
):
    user = crud.user.get_by_email(db, email=user_in.email)
    if user:
        raise HTTPException(status_code=400, detail="Email already registered")
    user = crud.user.create(db, obj_in=user_in)
    return user

@router.post("/login", response_model=schemas.Token)
def login(
    db: Session = Depends(get_db),
    form_data: OAuth2PasswordRequestForm = Depends(),
):
    user = crud.user.get_by_email(db, email=form_data.username)
    if not user or not crud.user.pwd_context.verify(form_data.password, user.hashed_password):
        raise HTTPException(status_code=400, detail="Incorrect email or password")

    access_token = create_access_token(user.id)
    return {
        "access_token": access_token,
        "token_type": "bearer",
    }
EOF

cat << 'EOF' > "$BASE_PATH/api/v1/journal.py"
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app import crud, models, schemas
from app.dependencies import get_db, get_current_user

router = APIRouter()

@router.post("/", response_model=schemas.JournalInDB)
def create_journal(
    *,
    db: Session = Depends(get_db),
    journal_in: schemas.JournalCreate,
    current_user: models.User = Depends(get_current_user),
):
    journal = crud.journal.create_with_owner(db=db, obj_in=journal_in, owner_id=current_user.id)
    return journal

@router.get("/", response_model=list[schemas.JournalInDB])
def read_journals(
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
    current_user: models.User = Depends(get_current_user),
):
    journals = crud.journal.get_multi_by_owner(db, owner_id=current_user.id, skip=skip, limit=limit)
    return journals
EOF

cat << 'EOF' > "$BASE_PATH/api/api.py"
from fastapi import APIRouter
from app.api.v1 import auth, journal

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(journal.router, prefix="/journals", tags=["journals"])
EOF

# ==================================
# MAIN APPLICATION
# ==================================
echo "Menulis file aplikasi utama..."
cat << 'EOF' > "$BASE_PATH/main.py"
from fastapi import FastAPI
from app.api.api import api_router

app = FastAPI(title="Dear Diary API")

app.include_router(api_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {"message": "Welcome to Dear Diary API"}
EOF


echo "--------------------------------------"
echo "Restrukturisasi Backend Selesai!"
echo "Jalankan 'uvicorn app.main:app --reload' dari direktori 'backend/' untuk memulai server."
echo "--------------------------------------"
