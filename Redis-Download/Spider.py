#coding=utf-8
from bs4 import BeautifulSoup
import urllib2
from Queue import Queue
from RedisQueue import RedisQueue
queue = Queue()
redis = RedisQueue('0','jandan')
 
def user_agent(url):
	req_header = {'User-Agent':'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0'}
	req_timeout = 20
	req = urllib2.Request(url,None,req_header)
	page = urllib2.urlopen(req,None,req_timeout)
	html = page
	return html
 
def next_page():
	base_url = 'http://jandan.net/drawings'
	html = user_agent(base_url).read()
	soup = BeautifulSoup(html,"html.parser")
	img_url = soup.find_all("img")
	for i in img_url:
		if "http" in i.get('src'):
			yield(i.get('src'))
			redis.put(i.get('src'))

for i in next_page():
	print(i)