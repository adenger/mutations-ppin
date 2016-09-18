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
			for i in range(3,13):
				clVals.append(values[i])
			domains = values[13].strip().split(":")
			mutationToDomains[mutation] = domains
			interactions[(mutProt,intProt)][mutation] = clVals
	print("Number of mutations: "+str(len(mutationToDomains)))
	print("Number of PPIs: "+str(len(interactions)))
	print("\nMutP\tIntP\t#Mut\t%PP2\t%SIFT\t%P||s\t%P&&S\tMutD")
	for (mutProt,intProt),mutations in interactions.items():
		affectedDomains = set()
		polyphen2Avg = 0
		siftAvg = 0
		pshcAvg = 0
		pasAvg = 0
		for id,clVal in mutations.items():
			polyphen2Avg += int(clVal[2])
			siftAvg += int(clVal[4])
			pshcAvg += int(clVal[8])
			pasAvg += 1 if (clVal[2] == "1" and clVal[4] == "1") else  0
			for domain in mutationToDomains[id]:
				affectedDomains.add(domain)
		polyphen2Avg = round(polyphen2Avg / len(mutations),2)
		siftAvg = round(siftAvg / len(mutations),2)
		pshcAvg = round(pshcAvg / len(mutations),2)
		pasAvg = round(pasAvg / len(mutations),2)
		print(mutProt+"\t"+intProt+"\t"+str(len(mutations))+"\t"+str(polyphen2Avg)+"\t"+str(siftAvg)+"\t"+str(pshcAvg)+"\t"+str(pasAvg)+"\t"+str(affectedDomains))