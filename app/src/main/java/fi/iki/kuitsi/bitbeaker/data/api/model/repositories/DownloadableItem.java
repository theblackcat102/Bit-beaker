package fi.iki.kuitsi.bitbeaker.data.api.model.repositories;

import fi.iki.kuitsi.bitbeaker.data.api.model.Links;

public final class DownloadableItem {
	public String name;
	public int size;
	public String type;
	public Links links;

	@Override
	public String toString() {
		return "DownloadableItem{"
				+ "name=" + name
				+ ",links=" + links
				+ '}';
	}
}
