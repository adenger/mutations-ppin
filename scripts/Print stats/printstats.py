import os

#take protein connections: sort mutations that delete it by number of classfiier values that predicted it 
for filename in os.listdir():
	if not filename.endswith(".txt"):
		continue
	with open(filename,"r") as file:
		for line in file:
			if not line.startswith("rs"):
				continue
			values = line.strip().split("\t")
			print(values)