try:
	file = open(input("path 2 requestTimes.txt: ").rstrip(), "r")
	lines = file.readlines()
	ts = tj = tscount = tjcount = 0
	for line in lines:
		line.rstrip("\n")
		temp = line.split(",")
		if len(temp) > 0:
			tj += int(temp[0])
			tjcount += 1
		if len(temp) > 1:
			ts += int(temp[1])
			tscount += 1
		

	file.close()
	print("Average TS: " + str(ts / tscount))
	print("Average TJ: " + str(tj / tjcount))
except IOError:
	print("Invalid path")
