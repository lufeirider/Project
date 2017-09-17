# 注意用到的库和 python 2 有明显不同
from urllib import request


f = open(r"url.txt","r")  
lines = f.readlines()#读取全部内容  
for line in lines:
	downUrl = line.replace('\n','')
	print(downUrl)
	filename = downUrl.split('/')[-1] 
	print(filename)
	with request.urlopen(downUrl) as reponse:
		with open(filename, 'wb') as outFile:
			outFile.write(reponse.read())
