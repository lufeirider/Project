def WriteFile(data):
	f = open("cms4.txt","a+")
	f.write(data)

sUpName = "NULL"
flag = 0
dPath = {}
with open('cms3.txt') as file:
	for line in file:
		line = line.replace("\n", "").split(':')
		if line[1] == sUpName:
			dPath[line[1]].append(line[0])
			if "/conn.asp" in line[0]:
				flag = 1
		else:
			if flag == 1:
				for i in dPath[sUpName]:
					WriteFile(i + ":" + sUpName + "\n")
					flag = 0
			sUpName = line[1]
			dPath.setdefault(line[1],[])
			dPath[line[1]].append(line[0])