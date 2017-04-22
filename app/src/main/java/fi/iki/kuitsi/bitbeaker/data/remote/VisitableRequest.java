package fi.iki.kuitsi.bitbeaker.data.remote;

public interface VisitableRequest<S> {
	void accept(RequestComponent<S> component);
}
