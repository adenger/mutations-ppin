import os
from collections import defaultdict
mutationToDomains = {}
interactions = defaultdict(lambda :defaultdict(list))
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
			domains = values[13].strip().split(":")
			mutationToDomains[mutation] = domains
			interactions[(mutProt,intProt)][mutation] = clVals
	for (mutProt,intProt),mutations in interactions.items():
		affectedDomains = set()
		for id,clVal in mutations.items():
			for domain in mutationToDomains[id]:
				affectedDomains.add(domain)
		print(mutProt+"\t"+intProt+"\t"+str(len(mutations))+"\t"+str(affectedDomains))