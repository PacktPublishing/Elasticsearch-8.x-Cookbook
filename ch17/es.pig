REGISTER elasticsearch-hadoop-7.12.1/dist/elasticsearch-hadoop-pig-7.12.1.jar;
REGISTER commons-httpclient-3.1.jar;

SET pig.noSplitCombination TRUE;


DEFINE EsStorage org.elasticsearch.hadoop.pig.EsStorage();


-- launch the Map/Reduce job with 5 reducers

SET default_parallel 5;


--load the GB.txt file

geonames= LOAD 'GB.txt' using PigStorage('\t') AS
(geonameid:int,name:chararray,asciiname:chararray,
alternatenames:chararray,latitude:double,longitude:double,
feature_class:chararray,feature_code:chararray,
country_code:chararray,cc2:chararray,admin1_code:chararray,
admin2_code:chararray,admin3_code:chararray,
admin4_code:chararray,population:int,elevation:int,
dem:chararray,timezone:chararray,modification_date:chararray);


STORE geonames INTO 'geoname' USING EsStorage();