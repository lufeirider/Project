#!/usr/bin/env python
# -*- coding: utf-8 -*-
# unzip-gbk.py
import os
import zipfile
import rarfile
import io
import sys  
reload(sys)  
sys.setdefaultencoding('gbk')  


def WriteFile(dir):
	f = open("path.txt", "a+")
	f.write(dir + "\n")


for file in os.listdir("./ASP/"):
	flagDir = ''
	dirList = []
	if (os.path.splitext(file)[1] == ".rar"):
		print("\n" + file + "\n")
		
		rFileName = "./ASP/" + file
		r = rarfile.RarFile(rFileName, 'r')
		for filePath in r.namelist():
			filePath = '/' + filePath
			#处理目录路径
			if(filePath.split('/')[-1].find('.') < 0 and filePath[-1]!='/'):
				filePath = filePath + '/'
			#添加到路径列表当中去
			if (filePath[-1] == '/'):
				dirList.append(filePath)
		
		if(len(dirList)>0):
			####################################################################
			#去除压缩包文件目录
			
			sameList = dirList[0].split('/')
			
			flag = 1
			for index in range(len(sameList)):
				for filePath in dirList:
					if(filePath.find(sameList[index])<0):
						flag = 0
						break
				if(flag == 0):
					break
			
			
			for index in range(1,index):
				flagDir = flagDir + '/' + sameList[index]
			
			####################################################################
			for filePath in dirList:
				try:
					if(filePath.replace(flagDir,'') != '/'):
						print(filePath.replace(flagDir,'').lower())
						WriteFile(filePath.replace(flagDir,'').lower())
				except:
					pass

	#####################################################################################################
	#zip 压缩包
	if (os.path.splitext(file)[1] == ".zip"):
		print("\n" + file + "\n")
		
		zFileName = "./ASP/" + file
		z = zipfile.ZipFile(zFileName, 'r')
		for filePath in z.namelist():
			filePath = '/' + filePath
			#处理目录路径
			if(filePath.split('/')[-1].find('.') < 0 and filePath[-1]!='/'):
				filePath = filePath + '/'
			#添加到路径列表当中去
			if (filePath[-1] == '/'):
				dirList.append(filePath)
					
		if(len(dirList)>0):			
			####################################################################
			#去除压缩包文件目录
			


			sameList = dirList[0].split('/')
					
			flag = 1
			for index in range(len(sameList)):
				for filePath in dirList:
					if(filePath.find(sameList[index])<0):
						flag = 0
						break
				if(flag == 0):
					break
			
			for index in range(1,index):
				flagDir = flagDir + '/' + sameList[index]

			####################################################################
			for filePath in dirList:
				try:
					if(filePath.replace(flagDir,'') != '/'):
						print(filePath.replace(flagDir,'').lower())
						WriteFile(filePath.replace(flagDir,'').lower())
				except:
					pass
