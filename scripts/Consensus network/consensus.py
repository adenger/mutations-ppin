import gzip
import os.path
import re
import urllib.request
from zipfile import ZipFile
biogridPPI = {"Two-hybrid","Reconstituted Complex","Co-crystal Structure","Protein-peptide","PCA","FRET","Far Western","Biochemical Activity"}
pattern = re.compile("[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}")
def downloadDBs():
	if not os.path.isfile('intact.zip'):
		print("retrieving intact...")
		urllib.request.urlretrieve('ftp://ftp.ebi.ac.uk/pub/databases/intact/current/psimitab/intact.zip','intact.zip')
	if not os.path.isfile('biogrid_id.txt'):
		print("retrieving biogrid id conversion...")
		urllib.request.urlretrieve('http://www.genenames.org/cgi-bin/download?col=gd_app_sym&col=md_prot_id&status=Approved&status_opt=2&where=&order_by=gd_app_sym_sort&format=text&limit=&hgnc_dbtag=on&submit=submit','biogrid_id.txt')
	if not os.path.isfile('biogrid.zip'):
		print("retrieving biogrid...")
		urllib.request.urlretrieve('http://thebiogrid.org/downloads/archives/Latest%20Release/BIOGRID-ORGANISM-LATEST.tab2.zip','biogrid.zip')
def readIntact():
	print ("reading intact...")
	intactSet = set()
	with ZipFile('intact.zip').open('intact.txt','r') as intactFile:
		for byteLine in intactFile:
			line = byteLine.decode("utf-8")
			if line.startswith('#'):
				continue
			values = line.split("\t")
			taxStringA = values[9]
			taxStringB = values[10]
			if taxStringA == "-" or taxStringB == "-":
				continue
			taxidA = values[9].split("|")[0].split(":")[1].split("(")[0]
			taxidB = values[10].split("|")[0].split(":")[1].split("(")[0]
			if taxidA != "9606" or taxidB != "9606":
				continue
			uniprotA = values[0].split(":")[1];
			uniprotB = values[1].split(":")[1];
			if not pattern.match(uniprotA) or not pattern.match(uniprotB):
				continue
			intactSet.add((uniprotA,uniprotB))
			intactSet.add((uniprotB,uniprotA))
	return intactSet
	
def readBiogridConversion():
	print ("reading biogrid conversion file...")
	hgncToUniprot = {}
	with open('biogrid_id.txt') as biogridIDFile:
		for line in biogridIDFile:
			values = line.split("\t")
			if len(values) != 2:
				continue
			hgnc = values[0].strip()
			uniprot = values[1].strip()
			if len(hgnc) == 0 or len(uniprot) == 0:
				continue
			hgncToUniprot[hgnc] = uniprot
	return hgncToUniprot
	
def readBiogrid():
	print ("reading biogrid...")
	hgncToUniprot = readBiogridConversion()
	biogridSet = set()
	with ZipFile('biogrid.zip') as biogridZip:
		for filename in biogridZip.namelist():
			if not filename.startswith('BIOGRID-ORGANISM-Homo_sapiens'):
				continue
			with biogridZip.open(filename) as biogridFile:
				for byteLine in biogridFile:
					line = byteLine.decode("utf-8")
					if line.startswith("#"):
						continue
					values = line.split("\t")
					experimentType = values[11]
					if experimentType not in biogridPPI:
						continue
					hgncA = values[7]
					hgncB = values[8]
					if not hgncA in hgncToUniprot or not hgncB in hgncToUniprot:
						continue
					uniprotA = hgncToUniprot[hgncA]
					uniprotB = hgncToUniprot[hgncB]
					if not pattern.match(uniprotA) or not pattern.match(uniprotB):
						continue
					biogridSet.add((uniprotA,uniprotB))
					biogridSet.add((uniprotB,uniprotA))
	return biogridSet

def getUniqueIDs(network):
	uniqueIDs = set()
	for(a,b) in network:
		uniqueIDs.add(a)
		uniqueIDs.add(b)
	return uniqueIDs
	
def getUniqueEdges(network):
	newNetwork = set()
	for (a,b) in network:
		if (a,b) in newNetwork or (b,a) in newNetwork:
			continue
		newNetwork.add((a,b))
	return newNetwork

def writeLog(intact,biogrid):
	print("writing log...")
	intactUniqueIDs = getUniqueIDs(intact)
	biogridUniqueIDs = getUniqueIDs(biogrid)
	with open('log.txt',"w") as logFile:
		logFile.write("Intact nodes: " + str(len(intactUniqueIDs))+"\n")
		logFile.write("Biogrid nodes: " + str(len(biogridUniqueIDs))+"\n")
		logFile.write("Intersection Nodes: "+str(len(intactUniqueIDs & biogridUniqueIDs))+"\n")
		logFile.write("Symmetric difference Nodes: "+str(len(intactUniqueIDs ^ biogridUniqueIDs))+"\n")
		logFile.write("Union Nodes: "+str(len(intactUniqueIDs | biogridUniqueIDs))+"\n")
		logFile.write("Intact Edges: "+str(len(intact)//2)+"\n")
		logFile.write("Biogrid Edges: "+str(len(biogrid)//2)+"\n")
		logFile.write("Intersection Edges: "+str(len(intact & biogrid)//2)+"\n")
		logFile.write("Symmetric difference Edges: "+str(len(intact ^ biogrid)//2)+"\n")
		logFile.write("Union Edges: "+str(len(intact | biogrid)//2)+"\n")

def writeFile(network,name):
	print("writing output file...")
	with gzip.open(name,"wb") as outputFile:
		for (a,b) in network:
			outputFile.write((a + "\t" + b + "\n").encode())

def main():
	downloadDBs()
	intact = readIntact()
	biogrid = readBiogrid()
	writeLog(intact,biogrid)
	writeFile(getUniqueEdges(intact | biogrid),"consensus_network.txt.gz")
	print("done.")
main()