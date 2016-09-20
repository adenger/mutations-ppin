from collections import defaultdict
geneName = "BRCA2_HUMAN"
domainToMutations = defaultdict(set)
with open("stats.txt" ,"r")as file:
	for line in file:
		if not line.startswith("PF"):
			continue
		values = line.strip().split("\t")
		domainToMutations[values[0]].add(values[1])
domainToSMLF = defaultdict(set)
with open("brca2_results.txt","r") as file:
	for line in file:
		if not line.startswith("rs"):
			continue
		values = line.strip().split("\t")
		domains = values[13].split(":")
		allele = values[20]
		position=values[21]
		for domain in domains:
			mutations = domainToMutations[domain]
			for mutation in mutations:
				domainToSMLF[domain].add(geneName+"\t"+allele[0]+position+allele[2]+"\n")
for domain in domainToSMLF:
	smlfStrings = domainToSMLF[domain]
	with open(domain+".smlf","w") as file:
		for smlfString in smlfStrings:
			file.write(smlfString)