#coding=utf-8
from RedisQueue import RedisQueue
redis = RedisQueue('0','testno1080')

with open("testcn1080.txt") as file:
	for i in file.readlines():
		i = i.replace("\n","") + ":1080"
		print(i)
		redis.put(i)