import redis  
import sys
sys.dont_write_bytecode = True
class RedisQueue(object):  
	"""Simple Queue with Redis Backend"""  
	def __init__(self, db , name , **redis_kwargs):  
		"""The default connection parameters are: host='localhost', port=6379, db=0"""  
		self.__db= redis.Redis(host='192.168.33.136', port=6379, db=db)
		self.pipe = self.__db.pipeline()
		self.key = '%s' %(name)  
  
	def qsize(self):  
		"""Return the approximate size of the queue."""  
		return self.__db.llen(self.key)  
  
	def empty(self):  
		"""Return True if the queue is empty, False otherwise."""  
		return self.qsize() == 0  
  
	def put(self, item):  
		"""Put item into the queue."""  
		self.__db.rpush(self.key, item)  
  
	def pop(self, block=True, timeout=None):  
		"""Remove and return an item from the queue.  
 
		If optional args block is true and timeout is None (the default), block 
		if necessary until an item is available."""  
		if block:  
			item = self.__db.blpop(self.key, timeout=timeout)  
		else:  
			item = self.__db.lpop(self.key)  
  
		if item:  
			item = item[1]  
		return item
		
	def get(self, block=True, timeout=None):  
		"""Remove and return an item from the queue.  
 
		If optional args block is true and timeout is None (the default), block 
		if necessary until an item is available."""  

		item = self.__db.lindex(self.key, 0)  
		return item

	def get_nowait(self):  
		"""Equivalent to get(False)."""  
		return self.get(False) 