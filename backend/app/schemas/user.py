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

class UserLogin(BaseModel):
    email: EmailStr
    password: str
