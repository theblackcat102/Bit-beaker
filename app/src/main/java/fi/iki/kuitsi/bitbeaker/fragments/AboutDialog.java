package fi.iki.kuitsi.bitbeaker.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.WikiActivity;

public class AboutDialog extends DialogFragment {

	private static final String ISSUE_TRACKER_URL = "https://bitbucket.org/bitbeaker-dev-team/bitbeaker/issues";
	private static final String CONTRIBUTORS_URL = "https://www.openhub.net/p/bitbeaker/contributors";
	private static final String REPO_URL = "https://bitbucket.org/bitbeaker-dev-team/bitbeaker";
	private static final String LICENSE_NAME = "Apache License, Version 2.0";
	private static final String README_URL = "https://bitbucket.org/bitbeaker-dev-team/bitbeaker/src/tip/README.md";

	@BindView(R.id.license) TextView license;
	@BindView(R.id.contributors) TextView contributors;
	@BindView(R.id.issues) TextView issues;
	private Unbinder unbinder;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View aboutView = View.inflate(getActivity(), R.layout.about_dialog, null);
		unbinder = ButterKnife.bind(this, aboutView);

		license.setText(Html.fromHtml(String.format(getString(R.string.about_license),
				LICENSE_NAME, Helper.getHtmlLink(README_URL))));
		license.setMovementMethod(LinkMovementMethod.getInstance());

		contributors.setText(Html.fromHtml(String.format(getString(R.string.about_contributors),
				Helper.getHtmlLink(CONTRIBUTORS_URL))));
		contributors.setMovementMethod(LinkMovementMethod.getInstance());

		issues.setText(Html.fromHtml(String.format(getString(R.string.about_issues),
				Helper.getHtmlLink(ISSUE_TRACKER_URL), Helper.getHtmlLink(REPO_URL))));
		issues.setMovementMethod(LinkMovementMethod.getInstance());

		return new MaterialDialog.Builder(getActivity())
				.title(getString(R.string.app_name) + "\n" + String.format(getString(
						R.string.bitbeaker_version), BuildConfig.VERSION_NAME))
				.customView(aboutView, true)
				.iconRes(R.drawable.ic_launcher)
				.positiveText(R.string.dialog_close)
				.negativeText(R.string.changelog)
				.neutralText(R.string.privacypolicy)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onNegative(MaterialDialog dialog) {
						startActivity(WikiActivity.createIntent(getActivity(), Bitbeaker.REPO_OWNER,
								Bitbeaker.REPO_SLUG, "Changelog"));
					}
					@Override
					public void onNeutral(MaterialDialog dialog) {
						startActivity(WikiActivity.createIntent(getActivity(), Bitbeaker.REPO_OWNER,
								Bitbeaker.REPO_SLUG, "PrivacyPolicy"));
					}
				})
				.build();
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();
		super.onDestroyView();
	}
}
