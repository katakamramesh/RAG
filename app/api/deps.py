from app.core.security import api_key_auth
from app.db.session import get_async_session

def require_api_key():
    return api_key_auth

def get_db():
    return get_async_session()