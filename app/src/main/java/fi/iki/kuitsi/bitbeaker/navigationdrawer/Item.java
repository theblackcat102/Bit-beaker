package fi.iki.kuitsi.bitbeaker.navigationdrawer;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a Navigation Drawer item.
 */
public interface Item {

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({CLICK_ACTION_NOTHING,
			CLICK_ACTION_SOURCE, CLICK_ACTION_COMMITS, CLICK_ACTION_PULL_REQUESTS,
			CLICK_ACTION_ISSUES, CLICK_ACTION_WIKI, CLICK_ACTION_FOLLOWERS,
	})
	@interface ClickAction { }

	int CLICK_ACTION_NOTHING = -1;

	int CLICK_ACTION_SOURCE = 10;
	int CLICK_ACTION_COMMITS = 11;
	int CLICK_ACTION_PULL_REQUESTS = 12;
	int CLICK_ACTION_ISSUES = 13;
	int CLICK_ACTION_WIKI = 14;
	int CLICK_ACTION_FOLLOWERS = 15;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({STATE_HIDDEN, STATE_VISIBLE, STATE_LOADING})
	@interface State { }

	int STATE_HIDDEN = -1;
	int STATE_VISIBLE = 0;
	int STATE_LOADING = 1;

	@NonNull
	View getView(LayoutInflater inflater, ViewGroup parent);

	@ClickAction int getAction();
}
