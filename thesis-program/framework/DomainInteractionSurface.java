package framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DomainInteractionSurface {
	private final String symbolA, symbolB;
	private Map<String, Set<BindingSite>> domainInterfaceConnections = new HashMap<String, Set<BindingSite>>();

	public DomainInteractionSurface(String symbolA, String symbolB) {
		super();
		this.symbolA = symbolA;
		this.symbolB = symbolB;
	}

	public void add3didInterfaceLine(String line) {
		Set<BindingSite> domainInterface = new TreeSet<BindingSite>();
		if (line.isEmpty() || !line.startsWith("#=IF")) {
			return;
		}
		String siteName = line.split(":")[0].trim();
		String sites = line.split(":")[1].trim();
		String[] values = sites.split("\\s+");
		for (String site : values) {
			String[] siteValues = site.split("\\-");
			int start = Integer.parseInt(siteValues[0]);
			int end = Integer.parseInt(siteValues[1]);
			BindingSite bindingSite = new BindingSite(start, end);
			domainInterface.add(bindingSite);
		}
		domainInterfaceConnections.put(siteName, domainInterface);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainInteractionSurface other = (DomainInteractionSurface) obj;
		if (domainInterfaceConnections == null) {
			if (other.domainInterfaceConnections != null)
				return false;
		} else if (!domainInterfaceConnections.equals(other.domainInterfaceConnections))
			return false;
		if (symbolA == null) {
			if (other.symbolA != null)
				return false;
		} else if (!symbolA.equals(other.symbolA))
			return false;
		if (symbolB == null) {
			if (other.symbolB != null)
				return false;
		} else if (!symbolB.equals(other.symbolB))
			return false;
		return true;
	}

	public String getSymbolA() {
		return symbolA;
	}

	public String getSymbolB() {
		return symbolB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainInterfaceConnections == null) ? 0 : domainInterfaceConnections.hashCode());
		result = prime * result + ((symbolA == null) ? 0 : symbolA.hashCode());
		result = prime * result + ((symbolB == null) ? 0 : symbolB.hashCode());
		return result;
	}

	public boolean isInBindingSite(String name, int position) {
		Set<BindingSite> sites = domainInterfaceConnections.get(name);
		if (sites == null)
			return false;
		for (BindingSite bindingSite : sites) {
			if (bindingSite.isInRange(position))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "DomainInteractionSurface [symbolA=" + symbolA + ", symbolB=" + symbolB + ", domainInterfaceConnections="
				+ domainInterfaceConnections + "]";
	}
}
