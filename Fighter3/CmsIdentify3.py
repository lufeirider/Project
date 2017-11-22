import urllib.request
import re
import sys
from urllib.parse import urlparse
import io
import sys
from datetime import datetime
from elasticsearch import Elasticsearch

sys.stdout = io.TextIOWrapper(sys.stdout.buffer,encoding='gbk')   
#http://www.hygcsj.com/
url = sys.argv[1]
data = urllib.request.urlopen(url).read()
data = data.decode('gbk','ignore')

es = Elasticsearch("127.0.0.1:9200")
def GetData(key):
	aResult = []
	res = es.search(index="cms",body={'query':{'match':{'url':key}},'size':500},scroll="1m")
	while True:
		if res['hits']['hits']:
			aResult = aResult + res['hits']['hits']
			scrollId= res['_scroll_id']
			res = es.scroll(scroll_id=scrollId, scroll= "1m")
			aResult = aResult + res['hits']['hits']
		else:
			return aResult

regx = re.compile(r"""src=["|'](.*?)["|']""",re.I)
srcList = re.findall(regx,data)


regx = re.compile(r"""href=["|'](.*?)["|']""",re.I)
hrefList = re.findall(regx,data)

#filter same
pathList = list(set(srcList).union(set(hrefList)))

#filter http
pathList = list(filter(lambda i:"http:" not in i,pathList))

dResult = {}
dInfo = {}

for index in range(len(pathList)):
	pathList[index] = pathList[index].lower()
	parseResult = urlparse(pathList[index])  
	path = parseResult.path
	if(path[0:1]=='/'):
		path = path[1:]
	pathList[index] = path

while '' in pathList:
	pathList.remove('')

pathList = list(set(pathList))

for path in pathList:
	res = es.search(index="cms",body={'query':{'match_phrase':{'url':path}},'aggs':{'name_list':{'terms':{'field':'name.keyword','size':500}}}})
	aResult = res["aggregations"]["name_list"]["buckets"]
	for i in aResult:
		#print(i["key"])
		if(((dInfo.get(i["key"])) == None) or (path not in dInfo.get(i["key"]))):
			key = i["key"]
			value = path
			dInfo.setdefault(key,[]).append(value)
			if(i["key"] in dResult):
				dResult[i["key"]] = dResult[i["key"]] + 1
			else:
				dResult[i["key"]] = 1	
					
dResult = sorted(dResult.items(), key = lambda e:e[1], reverse = True)

if len(dResult) == 0:
	print("NULL")
	exit()


sUpNum = ""
iTypeNum = 0
for i in dResult:
	if i[1]!= sUpNum:
		iTypeNum = iTypeNum + 1
		sUpNum = i[1]
	if iTypeNum < 3:
		print("{[" + i[0] + "," + str(i[1]) + "]" + str(dInfo[i[0]]) + "}")
