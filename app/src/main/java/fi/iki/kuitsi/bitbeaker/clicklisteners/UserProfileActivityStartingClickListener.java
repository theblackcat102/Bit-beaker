package fi.iki.kuitsi.bitbeaker.clicklisteners;

import fi.iki.kuitsi.bitbeaker.activities.UserProfileActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * This class is an OnClickListener that launches the UserProfileActivity for
 * the username given in the constructor. This class can thus be used for
 * making a View clickable by using its
 * {@link android.view.View#setOnClickListener(android.view.View.OnClickListener)}
 * method, the click leading the user to the profile of the given user.
 */
public class UserProfileActivityStartingClickListener implements OnClickListener {

	private final String username;

	public UserProfileActivityStartingClickListener(String user) {
		username = user;
	}

	@Override
	public void onClick(View v) {
		Bundle b = new Bundle();
		b.putString("user", username);
		Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
		intent.putExtras(b);
		v.getContext().startActivity(intent);
	}

}
