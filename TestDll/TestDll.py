# coding=utf8
# -*- coding: UTF-8 -*-
import re
import shutil

f = open("dll.txt", "r")  
while True:  
	line = f.readline()  
	if line:  
		line=line.strip()
		shutil.copyfile("test.dll", "dll/"+line)
		print line
		copyDllFile = open("dll/"+line,"r+b")
		copyDllFile.seek(176156)				#移动指针
		copyDllFile.write(line)
		#copyDllFile.write(b'\x68\x65\x6C\x6C\x77\x6F\x72\x6C\x64')  也可以用这种方法修改十六进制
		copyDllFile.close()
	else:  
		break
f.close()
