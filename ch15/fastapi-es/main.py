#!/usr/bin/env python
import os
from fastapi.params import Query
import uvicorn
from fastapi import FastAPI
from fastapi.responses import RedirectResponse
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
async def get_iris_samples(size: int = Query(..., title="Number of records to return", gt=10)):
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
