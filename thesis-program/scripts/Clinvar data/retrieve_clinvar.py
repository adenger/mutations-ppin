import gzip
import os.path
import urllib.request

if not os.path.isfile('variant_summary.txt.gz'):
	print("retrieving clinvar...")
	urllib.request.urlretrieve('ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz','variant_summary.txt.gz')
dbSNP_Set = set()
print("filtering variants...")
with gzip.open('variant_summary.txt.gz','rb') as f:
	for byteline in f:
		line = byteline.decode("utf-8")
		values = line.split("\t")
		assembly = values[12]
		if assembly != "GRCh38":
			continue
		type = values[1]
		if type != "single nucleotide variant":
			continue
		significance = values[5]
		if significance != "Pathogenic":
			continue
		id = values[6]
		if id == "-1":
			continue
		dbSNP = 'rs'+id
		dbSNP_Set.add(dbSNP)
print("wrinting file...")
with gzip.open('clinvar_pathogenic_snv.txt.gz','wb') as f:
	for id in dbSNP_Set:
		line = id + "\n"
		f.write(line.encode())
print("done.")