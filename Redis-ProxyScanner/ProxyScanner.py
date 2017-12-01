import threading
import threadpool
from sockshandler import SocksiPyHandler
import urllib2
import socks 
import socket
import re
import os
import requests

from RedisQueue import RedisQueue
rRead = RedisQueue('0','no1080')
rWrite = RedisQueue('0','yes1080')

lProxy = []
nCounter = 0

pool = threadpool.ThreadPool(100) 

def check(host):
	print(host)
	try:
		ip,port = host.strip().split(":")
		proxies = dict(http='socks5://'+host,https='socks5://'+host)
		timeout = 1.0
		resp = requests.get('http://2017.ip138.com/ic.asp', proxies=proxies ,timeout=timeout)							 
		r = resp.text.encode('UTF-8')
		ip = re.findall('\[(\d*?\.\d*?\.\d*?\.\d*?)\]',r)[0]
		if ip:
			rWrite.put(host)
			print "############" + host + "############"
	except:
		pass
	

while True:
	while not rRead.empty():
		sProxy = rRead.pop()
		lProxy.append(sProxy)
		nCounter = nCounter + 1
		if nCounter%100 == 0:
			lRequests = threadpool.makeRequests(check,lProxy) 
			[pool.putRequest(req) for req in lRequests]
			pool.wait()
			lProxy = []
	if len(lProxy) != 0:
		lRequests = threadpool.makeRequests(check,lProxy) 
		[pool.putRequest(req) for req in lRequests]
		pool.wait()
		lProxy = []