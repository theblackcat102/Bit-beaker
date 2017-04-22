package fi.iki.kuitsi.bitbeaker.provider;

import android.content.ContentValues;
import android.net.Uri;

interface QueryHandler {
	void startInsert(int token, Object cookie, Uri uri, ContentValues initialValues);
	void startDelete(int token, Object cookie, Uri uri, String selection, String[] selectionArgs);
}
