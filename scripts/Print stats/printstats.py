import os
from collections import defaultdict
#take protein connections: sort mutations that delete it by number of classfiier values that predicted it 
interactions = defaultdict(lambda :defaultdict(list)) # uniprot pairs to list of mutation pairs 
for filename in os.listdir():
	if not filename.endswith(".txt"):
		continue
	with open(filename,"r") as file:
		for line in file:
			if not line.startswith("rs"):
				continue
			values = line.strip().split("\t")
			mutation = values[0]
			mutProt = values[1]
			intProt = values[2]
			clVals = []
			for i in range(3,12):
				clVals.append(values[i])	
			interactions[(mutProt,intProt)][mutation] =clVals
	for (mutProt,intProt),mutations in interactions.items():
		print(mutProt+"\t"+intProt+"\t"+str(len(mutations)))