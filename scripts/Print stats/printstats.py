import os
from collections import defaultdict
stats = ""
for filename in os.listdir():
	mutationToDomains = {}
	mutationToScore = {}
	interactions = defaultdict(lambda :defaultdict(list))
	if not filename.endswith(".txt") or filename == "stats.txt":
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
			score = (float(values[14]) + 1 - float(values[15]))/2
			mutationToScore[mutation] = score
			mutationToDomains[mutation] = domains
			interactions[(mutProt,intProt)][mutation] = clVals
	stats+="###Evaluating file: "+filename+"\n\n"
	stats+="Number of individual mutations: "+str(len(mutationToDomains))+"\n"
	stats+="Number of PPIs: "+str(len(interactions))+"\n\n"
	stats+="MutP\tIntP\t#Mut\t%PP2\t%SIFT\t%P||s\t%P&&S\tMutD\n"
	for (mutProt,intProt),mutations in sorted(interactions.items()):
		affectedDomains = set()
		polyphen2Avg = 0
		siftAvg = 0
		pshcAvg = 0
		pasAvg = 0
		mostDeleterious = []
		for id,clVal in mutations.items():
			polyphen2Avg += int(clVal[2])
			siftAvg += int(clVal[4])
			pshcAvg += int(clVal[8])
			pasAvg += 1 if (clVal[2] == "1" and clVal[4] == "1") else  0
			for domain in mutationToDomains[id]:
				affectedDomains.add(domain)
			mostDeleterious.append(id)
		polyphen2Avg = round(polyphen2Avg / len(mutations),2)
		siftAvg = round(siftAvg / len(mutations),2)
		pshcAvg = round(pshcAvg / len(mutations),2)
		pasAvg = round(pasAvg / len(mutations),2)
		print("#########")
		for k in sorted(mostDeleterious,key=lambda x : mutationToScore[x],reverse=True):
			print(k + " "+str(mutationToScore[k])) # TODO use this to append it to stats
		stats+=mutProt+"\t"+intProt+"\t"+str(len(mutations))+"\t"+str(polyphen2Avg)+"\t"+str(siftAvg)+"\t"+str(pshcAvg)+"\t"+str(pasAvg)+"\t"+str(affectedDomains)+"\n"
	stats+="\n"
print(stats)
with open("stats.txt","w") as file:
	file.write(stats)