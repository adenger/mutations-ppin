package framework;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Mutation implements Comparable<Mutation> {
	private final String dbSNP;
	private final String geneID;
	private final String transcriptID; // ensembl
	private final String proteinAllele;
	private final long proteinPosition; // in uniprot protein
	private final double polyphenScore;
	private final String polyphenPrediction;
	private final double siftScore;
	private final String siftPrediction;
	private final Consequence consequence;
	private final Set<String> affectedPfamDomains;
	private final List<Consequence> consequenceOrder = Arrays.asList(new Consequence[] { Consequence.SYNOMYMOUS,
			Consequence.MISSENSE, Consequence.FRAMESHIFT, Consequence.NONSENSE });
	private String uniprotID;
	private Long transcriptLength;

	public Mutation(JSONObject json) {
		super();
		this.dbSNP = json.getString("id");
		this.geneID = json.getString("gene");
		this.transcriptID = json.getString("transcript");
		this.proteinAllele = json.getString("protein_allele");
		this.proteinPosition = json.getLong("protein_pos");
		this.polyphenScore = json.getDouble("polyphen_score");
		this.polyphenPrediction = json.getString("polyphen_pred");
		this.siftScore = json.getDouble("sift_score");
		this.siftPrediction = json.getString("sift_pred");
		this.consequence = Consequence.getConsequence(json.getString("consequence"));
		this.uniprotID = json.getString("uniprot");
		this.transcriptLength = json.getLong("transcript_length");
		JSONArray domainsJsonArray = json.getJSONArray("pfam_domains");
		Set<String> domains = new HashSet<String>();
		for (Object object : domainsJsonArray) {
			String pfamID = (String) object;
			domains.add(pfamID);
		}
		this.affectedPfamDomains = domains;
	}

	public Mutation(String dbSNP, String geneID, String transcriptID, String proteinAllele, long proteinPosition,
			double polyphenScore, String polyphenPrediction, double siftScore, String siftPrediction,
			Consequence consequence, Set<String> affectedPfamDomains) {
		super();
		this.dbSNP = dbSNP;
		this.geneID = geneID;
		this.transcriptID = transcriptID;
		this.proteinAllele = proteinAllele;
		this.proteinPosition = proteinPosition;
		this.polyphenScore = polyphenScore;
		this.polyphenPrediction = polyphenPrediction;
		this.siftScore = siftScore;
		this.siftPrediction = siftPrediction;
		this.consequence = consequence;
		this.affectedPfamDomains = affectedPfamDomains;
	}

	@Override
	public int compareTo(Mutation other) {
		// greater = better
		int compare = 0;
		// compare length
		if (transcriptLength != null && other.transcriptLength != null) {
			compare = Long.compare(transcriptLength, other.transcriptLength);
			if (compare != 0)
				return compare;
		}
		// compare number of affected domains
		compare = Integer.compare(affectedPfamDomains.size(), other.affectedPfamDomains.size());
		if (compare != 0) {
			return compare;
		}
		// compare consequence rating
		if (consequence != null && other.consequence != null) {
			compare = Integer.compare(consequenceOrder.indexOf(consequence),
					consequenceOrder.indexOf(other.consequence));
			if (compare != 0)
				return compare;
		}
		// // compare polyphen
		compare = Double.compare(polyphenScore, other.polyphenScore);
		if (compare != 0) {
			return compare;
		}
		compare = Double.compare(1.0d - siftScore, 1.0d - other.siftScore);
		return compare;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mutation other = (Mutation) obj;
		if (affectedPfamDomains == null) {
			if (other.affectedPfamDomains != null)
				return false;
		} else if (!affectedPfamDomains.equals(other.affectedPfamDomains))
			return false;
		if (consequence != other.consequence)
			return false;
		if (dbSNP == null) {
			if (other.dbSNP != null)
				return false;
		} else if (!dbSNP.equals(other.dbSNP))
			return false;
		if (geneID == null) {
			if (other.geneID != null)
				return false;
		} else if (!geneID.equals(other.geneID))
			return false;
		if (polyphenPrediction == null) {
			if (other.polyphenPrediction != null)
				return false;
		} else if (!polyphenPrediction.equals(other.polyphenPrediction))
			return false;
		if (Double.doubleToLongBits(polyphenScore) != Double.doubleToLongBits(other.polyphenScore))
			return false;
		if (proteinAllele == null) {
			if (other.proteinAllele != null)
				return false;
		} else if (!proteinAllele.equals(other.proteinAllele))
			return false;
		if (proteinPosition != other.proteinPosition)
			return false;
		if (siftPrediction == null) {
			if (other.siftPrediction != null)
				return false;
		} else if (!siftPrediction.equals(other.siftPrediction))
			return false;
		if (Double.doubleToLongBits(siftScore) != Double.doubleToLongBits(other.siftScore))
			return false;
		if (transcriptID == null) {
			if (other.transcriptID != null)
				return false;
		} else if (!transcriptID.equals(other.transcriptID))
			return false;
		return true;
	}

	public Set<String> getAffectedPfamDomains() {
		return affectedPfamDomains;
	}

	public Consequence getConsequence() {
		return consequence;
	}

	public String getDbSNP() {
		return dbSNP;
	}

	public String getGeneID() {
		return geneID;
	}

	public String getPolyphenPrediction() {
		return polyphenPrediction;
	}

	public double getPolyphenScore() {
		return polyphenScore;
	}

	public String getProteinAllele() {
		return proteinAllele;
	}

	public long getProteinPosition() {
		return proteinPosition;
	}

	public String getSiftPrediction() {
		return siftPrediction;
	}

	public double getSiftScore() {
		return siftScore;
	}

	public String getTranscriptID() {
		return transcriptID;
	}

	public Long getTranscriptLength() {
		return transcriptLength;
	}

	public String getUniprotID() {
		return uniprotID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((affectedPfamDomains == null) ? 0 : affectedPfamDomains.hashCode());
		result = prime * result + ((consequence == null) ? 0 : consequence.hashCode());
		result = prime * result + ((dbSNP == null) ? 0 : dbSNP.hashCode());
		result = prime * result + ((geneID == null) ? 0 : geneID.hashCode());
		result = prime * result + ((polyphenPrediction == null) ? 0 : polyphenPrediction.hashCode());
		long temp;
		temp = Double.doubleToLongBits(polyphenScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((proteinAllele == null) ? 0 : proteinAllele.hashCode());
		result = prime * result + (int) (proteinPosition ^ (proteinPosition >>> 32));
		result = prime * result + ((siftPrediction == null) ? 0 : siftPrediction.hashCode());
		temp = Double.doubleToLongBits(siftScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((transcriptID == null) ? 0 : transcriptID.hashCode());
		return result;
	}

	public void setTranscriptLength(Long transcriptLength) {
		this.transcriptLength = transcriptLength;
	}

	public void setUniprotID(String uniprotID) {
		this.uniprotID = uniprotID;
	}

	public JSONObject toJsonObject() {
		JSONObject obj = new JSONObject();
		obj.put("id", dbSNP);
		obj.put("gene", geneID);
		obj.put("transcript", transcriptID);
		obj.put("protein_allele", proteinAllele);
		obj.put("protein_pos", proteinPosition);
		obj.put("polyphen_score", polyphenScore);
		obj.put("polyphen_pred", polyphenPrediction);
		obj.put("sift_score", siftScore);
		obj.put("sift_pred", siftPrediction);
		obj.put("consequence", consequence.toSequenceOntology());
		JSONArray domains = new JSONArray();
		for (String domain : affectedPfamDomains) {
			domains.put(domain);
		}
		obj.put("pfam_domains", domains);
		obj.put("uniprot", uniprotID);
		obj.put("transcript_length", transcriptLength);
		return obj;
	}

	@Override
	public String toString() {
		return "Mutation [dbSNP=" + dbSNP + ", geneID=" + geneID + ", transcriptID=" + transcriptID + ", proteinAllele="
				+ proteinAllele + ", proteinPosition=" + proteinPosition + ", polyphenScore=" + polyphenScore
				+ ", polyphenPrediction=" + polyphenPrediction + ", siftScore=" + siftScore + ", siftPrediction="
				+ siftPrediction + ", consequence=" + consequence + ", affectedPfamDomains=" + affectedPfamDomains
				+ ", uniprotID=" + uniprotID + ", transcriptLength=" + transcriptLength + "]";
	}

}
