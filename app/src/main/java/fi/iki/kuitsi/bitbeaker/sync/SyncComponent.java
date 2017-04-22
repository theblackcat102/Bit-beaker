package fi.iki.kuitsi.bitbeaker.sync;

import dagger.Subcomponent;

@Subcomponent
public interface SyncComponent {
	SyncHelper syncHelper();
}
