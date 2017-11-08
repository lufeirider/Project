def WriteFile(data):
	f = open("cms3.txt","a+")
	f.write(data)
	
with open("cms2.txt") as lines:
	for line in lines:
		line = line.replace("\n","").split(":")
		if line[0][-1] != "/":
			WriteFile(line[0] + ":" + line[1] + "\n")