def WriteFile(data):
	f = open("cms2.txt","a+")
	f.write(data)

sUpName = "NULL"
flag = 0
dPath = {}
with open('cms.txt') as file:
	for line in file:
		line = line.replace("\n", "").split(':')
		if line[1] == sUpName:
			dPath[line[1]].append(line[0])
		else:
			if sUpName in dPath.keys():
				num = 0
				for i in dPath[sUpName]:
					WriteFile(i)
					num = num + 1
					if num % 100 == 0:
						WriteFile(":" + sUpName + "\n")
			sUpName = line[1]
			dPath.setdefault(line[1],[])
			dPath[line[1]].append(line[0])