from utils import create_and_add_mapping, populate
import elasticsearch
from pprint import pprint

es = elasticsearch.Elasticsearch()
index_name = "my_index"

if es.indices.exists(index_name):
    es.indices.delete(index_name)


create_and_add_mapping(es, index_name)
populate(es, index_name)

results = es.search(index=index_name,
                    body={"query": {"match_all": {}}})
pprint(results)

results = es.search(index=index_name,
                    body={
                        "query": {
                            "term": {"name": {"boost": 3.0, "value": "joe"}}}
                    })
pprint(results)

results = es.search(index=index_name,
                    body={"query": {
                        "bool": {
                            "filter": {
                                "bool": {
                                    "should": [
                                        {"term": {"position": 1}},
                                        {"term": {"position": 2}}]}
                            }}}})
pprint(results)

es.indices.delete(index_name)
