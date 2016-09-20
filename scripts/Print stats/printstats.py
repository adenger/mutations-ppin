import os
from collections import defaultdict
def readFile(filename,mutationToScore,mutationToDomains,interactions):
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
def getDeleteriousPercentages(mutations):
	polyphen2Avg = 0
	siftAvg = 0
	pshcAvg = 0
	pasAvg = 0
	for id,clVal in mutations.items():
		polyphen2Avg += int(clVal[2])
		siftAvg += int(clVal[4])
		pshcAvg += int(clVal[8])
		pasAvg += 1 if (clVal[2] == "1" and clVal[4] == "1") else  0
	polyphen2Avg = round(polyphen2Avg / len(mutations),2)
	siftAvg = round(siftAvg / len(mutations),2)
	pshcAvg = round(pshcAvg / len(mutations),2)
	pasAvg = round(pasAvg / len(mutations),2)
	return [polyphen2Avg,siftAvg,pshcAvg,pasAvg]
def getDomainString(mutations,mutationToDomains):
	affectedDomains = set()
	for id,clVal in mutations.items():	
		for domain in mutationToDomains[id]:
			affectedDomains.add(domain)
	domainString = ""
	for domain in sorted(affectedDomains):
		domainString += domain+","
	return domainString[:-1]
def processPPI(mutProt,intProt,mutations,mutationToDomains):
	percentages = getDeleteriousPercentages(mutations)
	polyphen2Avg = percentages[0]
	siftAvg = percentages[1]
	pshcAvg = percentages[2]
	pasAvg = percentages[3]
	domainString = getDomainString(mutations,mutationToDomains)
	return mutProt+"\t"+intProt+"\t"+str(len(mutations))+"\t"+str(polyphen2Avg)+"\t"+str(siftAvg)+"\t"+str(pshcAvg)+"\t"+str(pasAvg)+"\t"+domainString+"\t"+"\n"
def writeFile(stats):
	print(stats)
	with open("stats.txt","w") as file:
		file.write(stats)
def processMutations(mutationToScore,mutationToDomains):
	domainToMutations = defaultdict(set)
	mutationString = ""
	for id,score in sorted (mutationToScore.items(), key=lambda x: x[1],reverse=True):
		if score < 1:
			break	
		for domain in mutationToDomains[id]:
			domainToMutations[domain].add(id+"\t"+str(score))
	for domain,strings in sorted(domainToMutations.items()):
		for string in sorted(strings):
			mutationString += domain +"\t"+ string+ "\n"
	return mutationString
def main():
	stats = ""
	for filename in os.listdir():
		mutationToDomains = defaultdict(set)
		mutationToScore = {}
		interactions = defaultdict(lambda :defaultdict(list))
		if not filename.endswith(".txt") or filename == "stats.txt":
			continue
		readFile(filename,mutationToScore,mutationToDomains,interactions)
		stats+="###Evaluating file: "+filename+"\n\n"
		stats+="Number of individual mutations: "+str(len(mutationToDomains))+"\n"
		stats+="Number of PPIs: "+str(len(interactions))+"\n\n"
		stats+="MutP\tIntP\t#Mut\t%PP2\t%SIFT\t%P||s\t%P&&S\tMutD\n"
		for (mutProt,intProt),mutations in sorted(interactions.items()):
			stats += processPPI(mutProt,intProt,mutations,mutationToDomains)
		stats+="\nMutations with highest score:\n\nDomain\tMutation\tScore\n"
		stats += processMutations(mutationToScore,mutationToDomains)+ "\n"
	writeFile(stats)
main()