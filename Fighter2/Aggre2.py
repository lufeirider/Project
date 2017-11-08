import re
def WriteFile(data):
	f = open("cms1.txt","a+")
	f.write(data)

sUpName = "NULL"
sUpPath = "NULL"
sTemp = ""
flag = 0
dPath = {}
with open('cms.txt') as file:
	for line in file:
		line = line.replace("\n", "").split(':')
		regx = re.compile(r"""(/.*/)(.*)""",re.I)
		#print(re.findall(regx,line[0]))

		lUrl = re.findall(regx,line[0])
		if len(lUrl) > 0:
			sPath = lUrl[0][0]
			sFile = lUrl[0][1]
			
			if sUpPath == sPath:
				sTemp = sTemp + sFile
			else:
				WriteFile(sTemp + ":" + sUpName + "\n")
				sTemp = line[0]
			sUpName = line[1]
			sUpPath = sPath