#!/usr/bin/env python
from dotenv import load_dotenv
load_dotenv()
import os
import uvicorn
from fastapi import FastAPI, Request, Depends, Response
from fastapi.responses import RedirectResponse, HTMLResponse, JSONResponse
import logging

from elasticsearch import AsyncElasticsearch
es = AsyncElasticsearch()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

title = "FastAPI-Elasticsearch"

app = FastAPI(
    title=title,
    description=title,
    debug=os.getenv("DEBUG", "0"),
    version=os.getenv("VERSION", "0.1")
)



@app.on_event("shutdown")
async def app_shutdown():
    await es.close()

@app.get("/iris/sample/{size}")
async def read_item(size:int):
    result = await es.search(index="iris", size=size)

    return {"items": result["hits"]["hits"]}

@app.get("/", include_in_schema=False)
async def index_page():
    return RedirectResponse(url='/docs')


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("API_PORT", 8000)),
        proxy_headers=True,
        lifespan="on",
        root_path=os.getenv("ROOT_PATH", ""),
        log_level=os.getenv("LOG_LEVEL", "debug")
    )
