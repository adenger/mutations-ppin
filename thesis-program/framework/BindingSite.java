package framework;

public class BindingSite implements Comparable<BindingSite> {
	private final int start, end;

	public BindingSite(int start, int end) {
		super();
		if (start <= end) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
	}

	@Override
	public int compareTo(BindingSite o) {
		int result = Integer.compare(this.start, o.start);
		if (result == 0) {
			return Integer.compare(this.end, o.end);
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BindingSite other = (BindingSite) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		return result;
	}

	public boolean isInRange(int number) {
		return start <= number && number <= end;
	}

	@Override
	public String toString() {
		return "BindingSite [start=" + start + ", end=" + end + "]";
	}

}
