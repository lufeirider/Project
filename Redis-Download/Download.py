import urllib2
from RedisQueue import RedisQueue
redis = RedisQueue('0','jandan')

def user_agent(url):
	proxy_handler = urllib2.ProxyHandler({'http': '127.0.0.1:8080'})
	opener = urllib2.build_opener(proxy_handler)
	urllib2.install_opener(opener)
	req_header = {'User-Agent':'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0'}
	req_timeout = 20
	req = urllib2.Request(url,None,req_header)
	page = urllib2.urlopen(req,None,req_timeout)
	html = page
	return html

while True:
	while not redis.empty():
		down_url = redis.get()
		print(down_url)
		try:
			data = user_agent(down_url).read()
			with open('./'+down_url[-11:],'wb')as code:
				code.write(data)
			redis.pop()
		except:
			pass	