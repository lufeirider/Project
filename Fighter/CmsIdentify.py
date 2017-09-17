import urllib.request
import re
import sys
from urllib.parse import urlparse

url = "http://www.hut.edu.cn/"
data = urllib.request.urlopen(url).read()
data = data.decode('gbk','ignore')

resultDict = {}
infoDict = {}

regx = re.compile(r"""src=\W(\S*?(?:\.jpg|png|gif))""",re.I)
picList = re.findall(regx,data)

for index in range(len(picList)):
	picList[index] = picList[index].lower()
	
picList = list(set(picList))
picExistList = picList
print(picList)


regx = re.compile(r"""src=\W(\S*?\.js)""",re.I)
jsList = re.findall(regx,data)

for index in range(len(jsList)):
	jsList[index] = jsList[index].lower()
jsList = list(set(jsList))
print(jsList)

regx = re.compile(r"""href=\W(\S*?\.css)""",re.I)
cssList = re.findall(regx,data)

for index in range(len(cssList)):
	cssList[index] = cssList[index].lower()
cssList = list(set(cssList))
print(cssList)


regx = re.compile(r"""a.*?href=\W(\S*?")""",re.I)
pathList = re.findall(regx,data)

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
print(pathList)

# sys.exit()

print('\n\n\n\n')
f = open("log.txt","r")  

lines = f.readlines()#读取全部内容  
for line in lines: 
	line = line.replace('\n','').lower()
	
	for pic in picList:
		if(line.find(pic)>-1):
			if((infoDict.get(line.split(':')[1])==None) or (pic not in infoDict.get(line.split(':')[1]))):
				key = line.split(':')[1]
				value = pic
				infoDict.setdefault(key,[]).append(value)
				if(line.split(':')[1] in resultDict):
					resultDict[line.split(':')[1]] = resultDict[line.split(':')[1]] + 1
				else:
					resultDict[line.split(':')[1]] = 1

	for js in jsList:
		if(line.find(js)>-1):
			if((infoDict.get(line.split(':')[1])==None) or (js not in infoDict.get(line.split(':')[1]))):
				key = line.split(':')[1]
				value = js
				infoDict.setdefault(key,[]).append(value)
				if(line.split(':')[1] in resultDict):
					resultDict[line.split(':')[1]] = resultDict[line.split(':')[1]] + 1
				else:
					resultDict[line.split(':')[1]] = 1
				
	for css in cssList:
		if(line.find(css)>-1):
			if((infoDict.get(line.split(':')[1])==None) or (css not in infoDict.get(line.split(':')[1]))):
				key = line.split(':')[1]
				value = css
				infoDict.setdefault(key,[]).append(value)
				if(line.split(':')[1] in resultDict):
					resultDict[line.split(':')[1]] = resultDict[line.split(':')[1]] + 1
				else:
					resultDict[line.split(':')[1]] = 1
					
	for path in pathList:
		if(line.find(path)>-1):
			if((infoDict.get(line.split(':')[1])==None) or (path not in infoDict.get(line.split(':')[1]))):
				key = line.split(':')[1]
				value = path
				infoDict.setdefault(key,[]).append(value)
				if(line.split(':')[1] in resultDict):
					resultDict[line.split(':')[1]] = resultDict[line.split(':')[1]] + 1
				else:
					resultDict[line.split(':')[1]] = 1
				

resultDict = sorted(resultDict.items(), key=lambda e:e[1], reverse=True)
print('\n\n\n\n')
print(resultDict)
print('\n\n\n\n')
print(resultDict[0])
print('infoDict[resultDict[0][0]]\n\n\n\n')
print(infoDict[resultDict[0][0]])



