package framework;

import org.json.JSONObject;

public class Domain {
	private final String pfamID;
	private final String uniprotID;
	private final int sequenceStart;
	private final int sequenceEnd;

	public Domain(JSONObject obj) {
		super();
		this.pfamID = obj.getString("pfamID");
		this.uniprotID = obj.getString("uniprotID");
		this.sequenceStart = obj.getInt("start");
		this.sequenceEnd = obj.getInt("end");
	}

	public Domain(String pfamID, String uniprotID, int sequenceStart, int sequenceEnd) {
		super();
		this.pfamID = pfamID;
		this.uniprotID = uniprotID;
		this.sequenceStart = sequenceStart;
		this.sequenceEnd = sequenceEnd;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Domain other = (Domain) obj;
		if (pfamID == null) {
			if (other.pfamID != null)
				return false;
		} else if (!pfamID.equals(other.pfamID))
			return false;
		if (sequenceEnd != other.sequenceEnd)
			return false;
		if (sequenceStart != other.sequenceStart)
			return false;
		if (uniprotID == null) {
			if (other.uniprotID != null)
				return false;
		} else if (!uniprotID.equals(other.uniprotID))
			return false;
		return true;
	}

	public String getPfamID() {
		return pfamID;
	}

	public int getSequenceEnd() {
		return sequenceEnd;
	}

	public int getSequenceStart() {
		return sequenceStart;
	}

	public String getUniprotID() {
		return uniprotID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pfamID == null) ? 0 : pfamID.hashCode());
		result = prime * result + sequenceEnd;
		result = prime * result + sequenceStart;
		result = prime * result + ((uniprotID == null) ? 0 : uniprotID.hashCode());
		return result;
	}

	public JSONObject toJsonObject() {
		JSONObject obj = new JSONObject();
		obj.put("pfamID", pfamID);
		obj.put("uniprotID", uniprotID);
		obj.put("start", sequenceStart);
		obj.put("end", sequenceEnd);
		return obj;
	}

	@Override
	public String toString() {
		return "Domain [pfamID=" + pfamID + ", uniprotID=" + uniprotID + ", sequenceStart=" + sequenceStart
				+ ", sequenceEnd=" + sequenceEnd + "]";
	}
}
