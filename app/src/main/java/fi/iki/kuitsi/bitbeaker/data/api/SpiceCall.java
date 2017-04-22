package fi.iki.kuitsi.bitbeaker.data.api;

public interface SpiceCall<T> {
	T loadDataFromNetwork() throws Exception;
}
