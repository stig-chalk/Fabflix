try:
	file = open(input("path 2 requestTimes.txt: "), "r")
	lines = file.readlines()
	ts = tj = requests = 0
	for line in lines:
		line.rstrip("\n")
		requests += 1
		temp = line.split(",")
		tj += int(temp[0])
		ts += int(temp[1])
	file.close()
	print("Average TS: " + str(ts / requests))
	print("Average TJ: " + str(tj / requests))
except IOError:
	print("Invalid path")
