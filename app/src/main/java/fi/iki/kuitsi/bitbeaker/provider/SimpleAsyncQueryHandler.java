package fi.iki.kuitsi.bitbeaker.provider;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

class SimpleAsyncQueryHandler extends AsyncQueryHandler implements QueryHandler {
	public SimpleAsyncQueryHandler(ContentResolver cr) {
		super(cr);
	}
}
