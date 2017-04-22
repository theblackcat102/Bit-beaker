package fi.iki.kuitsi.bitbeaker.data.api.model;

public final class Links {

	final Link self;

	Links(Link self) {
		this.self = self;
	}

	@Override
	public String toString() {
		return "{self=" + self + '}';
	}

	public String self() {
		return self.toString();
	}
}
