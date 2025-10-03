from pydantic import BaseSettings
from typing import List

class Settings(BaseSettings):
    # Environment Variables
    DATABASE_URL: str
    API_KEY: str
    RATE_LIMIT: str = "10/minute"
    CORS_ALLOW_ORIGINS: List[str] = ["*"]

    class Config:
        env_file = ".env"

settings = Settings()