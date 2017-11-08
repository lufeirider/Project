import urllib.request
import re
import sys
from urllib.parse import urlparse
import io
import sys

# sys.stdout = io.TextIOWrapper(sys.stdout.buffer,encoding='gbk')   
#http://www.hygcsj.com/
url = sys.argv[1]
data = urllib.request.urlopen(url).read()
data = data.decode('gbk','ignore')


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
#print(pathList)



with open('cms2.txt','r',encoding="gbk") as file:
	for line in file:
		line = line.replace('\n','').lower()
						
		for path in pathList:
			if(line.find(path) > -1):
				if((dInfo.get(line.split(':')[1]) == None) or (path not in dInfo.get(line.split(':')[1]))):
					key = line.split(':')[1]
					value = path
					dInfo.setdefault(key,[]).append(value)
					if(line.split(':')[1] in dResult):
						dResult[line.split(':')[1]] = dResult[line.split(':')[1]] + 1
					else:
						dResult[line.split(':')[1]] = 1
				

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