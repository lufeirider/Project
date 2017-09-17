#encoding:UTF-8
import urllib.request
import bs4
from bs4 import BeautifulSoup
import re
from urllib.parse import urlparse
def WriteFile(path,str):
	f = open(path,"a+")
	f.write(str+"\n")

for i in range(1,134):
	i = str(i) + ".htm"
	if(i == "1.htm"):
		i = ""
	url = "http://www.mycodes.net/5/" + i
	print(url+"\n")
	
	##################################################
	#获取列表页中a标签的href内容
	response = urllib.request.urlopen(url).read()
	html = response.decode('gbk','ignore')
	soup = BeautifulSoup(html,"html.parser")
	table = soup.find_all("table",width=re.compile("97%"), border="0")

	for a in table:
		name = a.find("a",target="_blank").string
		print(name)
		print(a.find("a",target="_blank")["href"])
		siteUrl = a.find("a",target="_blank")["href"]
		
		##################################################
		#获取详情页中下载地址
		response = urllib.request.urlopen(siteUrl).read()
		html = response.decode('gbk','ignore')
		#print(html)
		siteSoup = BeautifulSoup(html,"html.parser")
		jumpTd = siteSoup.find("td",class_="b4")
		jumpA = jumpTd.find("a")
		
		jumpUrl = jumpA['href']
		#print(jumpUrl)
		
		##################################################
		try:
			#获取302跳转地址
			buff = urllib.request.urlopen(jumpUrl)
			print(buff.geturl())
			
			#获取下载文件名
			parseResult = urlparse(buff.geturl())  
			path = parseResult.path
			regx = re.compile(r"""/.*?/(.*?(?:\.zip|rar))""",re.I)
			regxResult = re.findall(regx,path)
			print(regxResult[0])
			
			#写入文件
			WriteFile('url.txt',buff.geturl())
			WriteFile('match.txt',regxResult[0]+":"+name)
		except:
			pass