package fi.iki.kuitsi.bitbeaker.provider;

import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowContextImpl;

import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.ROBOLECTRIC_SDK)
public class FavoritesProviderTest {

	private static final Repository REPO_1 = new Repository("user1", "repo-slug1").name("Repository Name 1");
	private static final Repository REPO_2 = new Repository("user2", "repo-slug2").name("Repository Name 2");
	private static final Repository REPO_3 = new Repository("user3", "repo-slug3").name("Repository Name 3");

	private FavoritesProvider instance;
	private ShadowContentResolver cr;

	@Before
	public void setUp() {
		ShadowContextImpl context = new ShadowContextImpl();
		ProviderInfo providerInfo = new ProviderInfo();
		providerInfo.authority = BitbeakerContract.CONTENT_AUTHORITY;
		Robolectric.buildContentProvider(BitbeakerProvider.class).create(providerInfo);

		cr = Shadows.shadowOf(context.getContentResolver());
		instance = new FavoritesProvider(context.getContentResolver(), new Provider<QueryHandler>() {
			@Override
			public QueryHandler get() {
				return new SyncQueryHandler();
			}
		});
	}

	@Test
	public void save_and_status_query() {
		assertThat(instance.isFavoriteRepository(REPO_1)).isFalse();
		instance.saveFavoriteRepository(REPO_1);
		assertThat(instance.isFavoriteRepository(REPO_1)).isTrue();
	}

	@Test
	public void getFavorites_and_deleteFavorites() {
		assertThat(instance.getFavorites()).isEmpty();

		instance.saveFavoriteRepository(REPO_1);
		assertThat(instance.getFavorites()).hasSize(1);
		assertThat(instance.getFavorites()).containsExactly(REPO_1);

		instance.saveFavoriteRepository(REPO_2);
		assertThat(instance.getFavorites()).hasSize(2);
		assertThat(instance.getFavorites()).containsExactly(REPO_1, REPO_2).inOrder();

		instance.deleteFavorites();
		assertThat(instance.getFavorites()).isEmpty();
	}

	@Test
	public void removeFavoriteRepository() {
		// Add a repo, assert that it worked:
		assertThat(instance.getFavorites()).isEmpty();
		instance.saveFavoriteRepository(REPO_1);
		assertThat(instance.getFavorites()).hasSize(1);

		// Try removing a repo that wasn't there:
		instance.removeFavoriteRepository(REPO_2);
		assertThat(instance.getFavorites()).hasSize(1);

		// Add the repo:
		instance.saveFavoriteRepository(REPO_2);
		assertThat(instance.getFavorites()).hasSize(2);

		// Now remove it for real:
		instance.removeFavoriteRepository(REPO_2);
		assertThat(instance.getFavorites()).hasSize(1);
		assertThat(instance.getFavorites()).containsExactly(REPO_1);

		// Remove the other one too:
		instance.removeFavoriteRepository(REPO_1);
		assertThat(instance.getFavorites()).isEmpty();
	}

	@Test
	public void importRepo() {
		// Add two repositories
		List<Repository> repositories = new ArrayList<>(Arrays.asList(REPO_3, REPO_2));
		int numberOfInsertions = instance.addFavorites(repositories);
		assertThat(numberOfInsertions).isEqualTo(repositories.size());
		assertThat(instance.getFavorites()).hasSize(repositories.size());
		assertThat(instance.getFavorites()).containsExactly(REPO_2, REPO_3).inOrder();

		// Add three repositories, two already added
		repositories.add(REPO_1);
		numberOfInsertions = instance.addFavorites(repositories);
		assertThat(numberOfInsertions).isEqualTo(1);
		assertThat(instance.getFavorites()).hasSize(repositories.size());
		assertThat(instance.getFavorites()).containsExactly(REPO_1, REPO_2, REPO_3).inOrder();
	}

	final class SyncQueryHandler implements QueryHandler {

		@Override
		public void startInsert(int token, Object cookie, Uri uri, ContentValues initialValues) {
			cr.insert(uri, initialValues);
		}

		@Override
		public void startDelete(int token, Object cookie, Uri uri, String selection, String[] selectionArgs) {
			cr.delete(uri, selection, selectionArgs);
		}
	}
}
