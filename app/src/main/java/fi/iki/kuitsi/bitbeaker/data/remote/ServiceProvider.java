package fi.iki.kuitsi.bitbeaker.data.remote;

interface ServiceProvider<S> {
	S getService();
}
