def WriteFile(data):
	f = open("cms2.txt","a+")
	f.write(data)
	
list = []
with open("cms1.txt") as lines:
	for line in lines:
		if line not in list:
			list.append(line)
			WriteFile(line)
			