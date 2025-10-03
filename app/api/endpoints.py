from fastapi import APIRouter, Depends, Query
from typing import Optional
from app.api.deps import get_db, require_api_key
from app.services.chat import (
    create_session, add_message, get_messages, rename_session,
    mark_favorite, delete_session,
)

router = APIRouter(prefix="/api", dependencies=[Depends(require_api_key)])

# Session endpoints
@router.post("/sessions", summary="Create a new chat session")
async def create_chat_session(user_id: str, db=Depends(get_db)):
    return await create_session(user_id, db)

@router.patch("/sessions/{session_id}/rename", summary="Rename a chat session")
async def rename_chat_session(session_id: int, name: str, db=Depends(get_db)):
    return await rename_session(session_id, name, db)

@router.patch("/sessions/{session_id}/favorite", summary="Mark/unmark as favorite")
async def favorite_session(session_id: int, favorite: bool, db=Depends(get_db)):
    return await mark_favorite(session_id, favorite, db)

@router.delete("/sessions/{session_id}", summary="Delete a chat session")
async def delete_chat_session(session_id: int, db=Depends(get_db)):
    return await delete_session(session_id, db)

# Message endpoints
@router.post("/sessions/{session_id}/messages", summary="Add a message to session")
async def add_session_message(session_id: int, sender: str, content: str, context: Optional[str] = None, db=Depends(get_db)):
    return await add_message(session_id, sender, content, context, db)

@router.get("/sessions/{session_id}/messages", summary="Get chat messages")
async def get_session_messages(
    session_id: int,
    skip: int = Query(0, ge=0), 
    limit: int = Query(20, le=100),
    db=Depends(get_db),
):
    return await get_messages(session_id, skip, limit, db)