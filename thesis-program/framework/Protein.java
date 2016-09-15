package framework;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Protein {
	private final String uniprotID;
	private final Set<Domain> domains;

	public Protein(JSONObject obj) {
		this.uniprotID = obj.getString("uniprotID");
		JSONArray domainArray = obj.getJSONArray("domains");
		Set<Domain> domains = new HashSet<Domain>();
		for (Object domainObj : domainArray) {
			JSONObject domainJsonObj = (JSONObject) domainObj;
			Domain domain = new Domain(domainJsonObj);
			domains.add(domain);
		}
		this.domains = domains;
	}

	public Protein(String uniprotID, Set<Domain> domains) {
		super();
		this.uniprotID = uniprotID;
		this.domains = domains;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Protein other = (Protein) obj;
		if (domains == null) {
			if (other.domains != null)
				return false;
		} else if (!domains.equals(other.domains))
			return false;
		if (uniprotID == null) {
			if (other.uniprotID != null)
				return false;
		} else if (!uniprotID.equals(other.uniprotID))
			return false;
		return true;
	}

	public Set<Domain> getDomains() {
		return domains;
	}

	public String getUniprotID() {
		return uniprotID;
	}

	public boolean hasDomain(String pfamID) {
		for (Domain domain : domains) {
			if (domain.getPfamID().equals(pfamID)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domains == null) ? 0 : domains.hashCode());
		result = prime * result + ((uniprotID == null) ? 0 : uniprotID.hashCode());
		return result;
	}

	public JSONObject toJsonObject() {
		JSONObject obj = new JSONObject();
		obj.put("uniprotID", uniprotID);
		JSONArray domainArr = new JSONArray();
		for (Domain domain : domains) {
			JSONObject domainObj = domain.toJsonObject();
			domainArr.put(domainObj);
		}
		obj.put("domains", domainArr);
		return obj;
	}

	@Override
	public String toString() {
		return "Protein [uniprotID=" + uniprotID + ", domains=" + domains + "]";
	}

}
