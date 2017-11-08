import re

def WriteFile(data):
	f = open("cms1.txt","a+")
	f.write(data)
	f.close()

with open("cms.txt") as lines:
	for line in lines:
		line = line.replace("\n","").split(":")
		regx = re.compile("\d")
		line[0] = re.sub(regx,"*",line[0])
		print(line[0]+":"+line[1]+"\n")
		WriteFile(line[0]+":"+line[1]+"\n")