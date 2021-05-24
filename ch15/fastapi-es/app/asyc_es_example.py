import asyncio
from utils import create_and_add_mapping, populate
from pprint import pprint
from elasticsearch import AsyncElasticsearch
es = AsyncElasticsearch()

async def main():
    index_name = "my_index"

    if await es.indices.exists(index_name):
        await es.indices.delete(index_name)


    await create_and_add_mapping(es, index_name)
    await populate(es, index_name)

    results = await es.search(index=index_name,
                        body={"query": {"match_all": {}}})
    pprint(results)

    results = await es.search(index=index_name,
                        body={
                            "query": {
                                "term": {"name": {"boost": 3.0, "value": "joe"}}}
                        })
    pprint(results)

    results = await es.search(index=index_name,
                        body={"query": {
                            "bool": {
                                "filter": {
                                    "bool": {
                                        "should": [
                                            {"term": {"position": 1}},
                                            {"term": {"position": 2}}]}
                                }}}})
    pprint(results)

    await es.indices.delete(index_name)

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
    loop.run_until_complete(es.close())
