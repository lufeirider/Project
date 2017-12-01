#encoding=utf8
#author: walker摘自《Python Cookbook（2rd）》
#date: 2015-06-11
#function: 网络端口的转发和重定向（适用于python2/python3）
import sys, socket, time, threading
import random
LOGGING = True
loglock = threading.Lock()
ip_list = []
ip = ['ip','port']
#打印日志到标准输出
def log(s, *a):
	if LOGGING:	
		loglock.acquire()
		try:
			print('%s:%s' % (time.ctime(), (s % a)))
			sys.stdout.flush()
		finally:
			loglock.release()
class PipeThread(threading.Thread):
	pipes = []	 #静态成员变量，存储通讯的线程编号
	pipeslock = threading.Lock()
	def __init__(self, source, sink):
		#Thread.__init__(self) #python2.2之前版本适用
		super(PipeThread, self).__init__()
		self.source = source
		self.sink = sink
		log('Creating new pipe thread %s (%s -> %s)',
				self, source.getpeername(), sink.getpeername())
		self.pipeslock.acquire()
		try:
			self.pipes.append(self)
		finally:
			self.pipeslock.release()
		self.pipeslock.acquire()
		try:
			pipes_now = len(self.pipes)
		finally:
			self.pipeslock.release()
		log('%s pipes now active', pipes_now)
	def run(self):
		while True:
			try:
				data = self.source.recv(1024)
				if not data:
					break
				self.sink.send(data)
			except:
				break
		log('%s terminating', self)
		self.pipeslock.acquire()
		try:
			self.pipes.remove(self)
		finally:
			self.pipeslock.release()
		self.pipeslock.acquire()
		try:
			pipes_left = len(self.pipes)
		finally:
			self.pipeslock.release()
		log('%s pipes still active', pipes_left)
class Pinhole(threading.Thread):
	def __init__(self, port, newhost, newport):
		#Thread.__init__(self) #python2.2之前版本适用
		super(Pinhole, self).__init__()
		log('Redirecting: localhost: %s->%s:%s', port, newhost, newport)
		self.newhost = newhost
		self.newport = newport
		self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.bind(('', port))
		self.sock.listen(5) #参数为timeout，单位为秒
	def run(self):
		while True:
			newsock, address = self.sock.accept()
			log('Creating new session for %s:%s', *address)
			
			proxyip = ip_list[random.randint(0,len(ip_list)-1)]
			self.newhost = proxyip[0]
			self.newport = proxyip[1]
			print("############"+self.newhost)
			
			fwd = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
			fwd.connect((self.newhost, self.newport))
			PipeThread(newsock, fwd).start() #正向传送
			PipeThread(fwd, newsock).start() #逆向传送
if __name__ == '__main__':
	with open("ips.txt") as ips:
		lines = ips.readlines()
	for line in lines:
		ip[0],ip[1] = line.strip().split(":")
		ip[1] = eval(ip[1])
		nip = tuple(ip)
		ip_list.append(nip)
	
	
	print('Starting Pinhole port fowarder/redirector')
	port = 17629
	
	# try:
		# port = int(sys.argv[1])
		# newhost = sys.argv[2]
		# try:
			# newport = int(sys.argv[3])
		# except IndexError:
			# newport = port
	# except (ValueError, IndexError):
		# print('Usage: %s port newhost [newport]' % sys.argv[0])
		# sys.exit(1)
	#sys.stdout = open('pinhole.log', 'w') #将日志写入文件
	Pinhole(port, "", "").start()