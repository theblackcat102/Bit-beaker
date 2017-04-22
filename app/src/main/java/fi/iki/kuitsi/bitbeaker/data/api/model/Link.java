package fi.iki.kuitsi.bitbeaker.data.api.model;

import android.support.annotation.NonNull;

final class Link implements CharSequence {
	final String href;

	Link(String href) {
		this.href = href;
	}

	@Override
	public int length() {
		return href.length();
	}

	@Override
	public char charAt(int index) {
		return href.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return href.subSequence(start, end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Link link = (Link) o;
		return href.equals(link.href);
	}

	@Override
	public int hashCode() {
		return href.hashCode();
	}

	@NonNull
	@Override
	public String toString() {
		return href;
	}
}
