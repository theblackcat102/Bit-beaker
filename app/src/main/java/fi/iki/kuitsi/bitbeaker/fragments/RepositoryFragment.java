package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.MarkupHelper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.RepositoryActivity;
import fi.iki.kuitsi.bitbeaker.activities.UserProfileActivity;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

public class RepositoryFragment extends Fragment {

	@BindView(R.id.title) TextView title;
	@BindView(R.id.fork_of_label) TextView forkOfLabel;
	@BindView(R.id.fork_parent_description) TextView forkParentDescription;
	@BindView(R.id.repository_description) TextView repositoryDescription;
	@BindView(R.id.repository_readme) TextView readme;
	@BindView(R.id.icon) ImageView icon;

	private ImageLoader imageLoader;
	private Unbinder unbinder;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.repository, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		unbinder = ButterKnife.bind(this, view);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		imageLoader = AppComponentService.obtain(getContext().getApplicationContext()).imageLoader();
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();
		super.onDestroyView();
	}

	public void setRepository(Repository repo) {
		title.setText(repo.getOwner() + "/" + repo.getSlug());
		if (repo.isFork()) {
			setParentProjectInfo(repo.getForkOf());
		}
		repositoryDescription.setMovementMethod(LinkMovementMethod.getInstance());
		repositoryDescription.setText(Html.fromHtml(getRepositoryDescription(repo)));
		imageLoader.loadImage(this, repo.getLogo(), icon);
	}

	public void setReadme(String owner, String slug, String text) {
		//TODO: MarkupHelper.handleMarkup can't handle reStructuredText and Textile
		Spanned spanned = MarkupHelper.handleMarkup(text, owner, slug, false);
		readme.setText(spanned);
		readme.setMovementMethod(LinkMovementMethod.getInstance());
		Helper.fixURLSpans(readme);
	}

	/**
	 * Returns an HTML-formatted description of the given repository.
	 *
	 * @param repository The repository as coming from the Bitbucket API.
	 * @return Textual, HTML-formatted description of the project.
	 */
	private String getRepositoryDescription(Repository repository) {
		StringBuilder desc = new StringBuilder();
		if (!TextUtils.isEmpty(repository.getDescription())) {
			desc.append(repository.getDescription());
		}
		if (!TextUtils.isEmpty(repository.getWebsite())) {
			desc.append("<br>").append(Helper.getHtmlLink(repository.getWebsite()));
		}
		return desc.toString();
	}

	/**
	 * Fills the various TextViews of the layout of this activity with
	 * information about the given repository, which is assumed to be the
	 * parent project of some other project.
	 *
	 * @param parent The repository as coming from the Bitbucket API.
	 */
	private void setParentProjectInfo(Repository parent) {
		final String parentOwner = parent.getOwner();
		final String parentSlug = parent.getSlug();
		final boolean hasDescription = !TextUtils.isEmpty(parent.getDescription());

		forkOfLabel.setText(getString(R.string.fork_of) + " ");

		SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append(getString(R.string.fork_of));
		sb.append(' ');
		int parentOwnerIdxStart = sb.length();
		sb.append(parentOwner);
		int parentOwnerIdxEnd = sb.length();
		sb.append('/');
		int parentSlugIdxStart = sb.length();
		sb.append(parentSlug);
		int parentSlugIdxEnd = sb.length();
		if (hasDescription) {
			sb.append(':');
		}

		sb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(getActivity(), UserProfileActivity.class);
				intent.putExtra("user", parentOwner);
				startActivity(intent);
			}
		}, parentOwnerIdxStart, parentOwnerIdxEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

		sb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent intent = RepositoryActivity.createIntent(getActivity(), parentOwner,
						parentSlug);
				startActivity(intent);
			}
		}, parentSlugIdxStart, parentSlugIdxEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

		forkOfLabel.setText(sb);
		forkOfLabel.setMovementMethod(LinkMovementMethod.getInstance());
		forkOfLabel.setVisibility(View.VISIBLE);

		if (hasDescription) {
			forkParentDescription.setText(parent.getDescription());
			forkParentDescription.setVisibility(View.VISIBLE);
		}
	}

}
