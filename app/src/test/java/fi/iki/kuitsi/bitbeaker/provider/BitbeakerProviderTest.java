package fi.iki.kuitsi.bitbeaker.provider;

import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowContextImpl;

import java.util.Arrays;
import java.util.List;

import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.ROBOLECTRIC_SDK)
public class BitbeakerProviderTest {

	private static final List<Uri> VALID_URIS = Arrays.asList(
			BitbeakerContract.Events.CONTENT_URI,
			BitbeakerContract.Repository.CONTENT_URI,
			BitbeakerContract.Repository.CONTENT_STARRED_URI,
			BitbeakerContract.Repository.CONTENT_PRIVATE_URI,
			BitbeakerContract.Users.CONTENT_URI
	);

	private BitbeakerProvider provider;
	private ShadowContentResolver cr;

	@Before
	public void setUp() {
		ShadowContextImpl context = new ShadowContextImpl();
		cr = Shadows.shadowOf(context.getContentResolver());
		ProviderInfo providerInfo = new ProviderInfo();
		providerInfo.authority = BitbeakerContract.CONTENT_AUTHORITY;
		provider = Robolectric.buildContentProvider(BitbeakerProvider.class).create(providerInfo).get();
	}

	@After
	public void tearDown() {
		deleteUri(BitbeakerContract.BASE_CONTENT_URI);
	}

	@Test
	public void testValidUris() {
		for (Uri uri : VALID_URIS) {
			Cursor cursor = provider.query(uri, null, null, null, null);
			try {
				assertThat(cursor).isNotNull();
			} finally {
				cursor.close();
			}
		}
	}

	@Test
	public void testEventItemType() {
		assertThat(cr.getType(BitbeakerContract.Events.CONTENT_URI))
				.isEqualTo(BitbeakerContract.Events.CONTENT_TYPE);
	}

	@Test
	public void testRepositoryItemType() {
		assertThat(cr.getType(BitbeakerContract.Repository.CONTENT_URI))
				.isEqualTo(BitbeakerContract.Repository.CONTENT_TYPE);
		assertThat(cr.getType(BitbeakerContract.Repository.CONTENT_STARRED_URI))
				.isEqualTo(BitbeakerContract.Repository.CONTENT_TYPE);
	}

	@Test
	public void testUserItemType() {
		assertThat(cr.getType(BitbeakerContract.Users.CONTENT_URI))
				.isEqualTo(BitbeakerContract.Users.CONTENT_TYPE);
	}

	@Test
	public void testHasNoDefaultRepositories() {
		Cursor c = getRepositories();
		try {
			assert_().withFailureMessage("Has default repositories")
					.that(c.getCount()).isEqualTo(0);
		} finally {
			c.close();
		}
	}

	@Test
	public void testSaveRepository() {
		Repository repository = new Repository("user1", "repo-slug1")
				.name("Repository Name 1");
		assertInsertQuery(repository, false);
	}

	@Test
	public void testSaveRepositoryAsFavorite() {
		Repository repository = new Repository("user1", "repo-slug1")
				.name("Repository Name 1");
		assertInsertQuery(repository, true);
	}

	@Test
	public void testSavePrivateRepository() {
		Repository repository = new Repository("user1", "repo-slug-1")
				.name("Repository Name 1")
				.privateRepository(true);
		assertInsertQuery(repository, false);
	}

	// Utilities
	private Cursor getRepositories() {
		Cursor c = cr.query(BitbeakerContract.Repository.CONTENT_URI, null, null, null, null);
		assertThat(c).isNotNull();
		return c;
	}

	private Cursor getStarredRepositories() {
		Cursor c = cr.query(BitbeakerContract.Repository.CONTENT_STARRED_URI, null, null, null, null);
		assertThat(c).isNotNull();
		return c;
	}

	private void assertQueryReturns(Repository repository, boolean starred) {
		Cursor c;
		if (starred) {
			c = getStarredRepositories();
		} else {
			c = getRepositories();
		}
		try {
			assert_().withFailureMessage("More than one result")
					.that(c.getCount()).isEqualTo(1);
			while (c.moveToNext()) {
				String owner = getStringCol(c, BitbeakerContract.Repository.REPO_OWNER);
				String slug = getStringCol(c, BitbeakerContract.Repository.REPO_SLUG);
				String name = getStringCol(c, BitbeakerContract.Repository.REPO_NAME);
				boolean isPrivate = getBooleanCol(c, BitbeakerContract.Repository.REPO_PRIVATE);
				boolean isStarred = getBooleanCol(c, BitbeakerContract.Repository.REPO_STARRED);

				assertThat(owner).isEqualTo(repository.getOwner());
				assertThat(slug).isEqualTo(repository.getSlug());
				assertThat(name).isEqualTo(repository.getName());
				assertThat(isPrivate).isEqualTo(repository.isPrivateRepository());
				assertThat(isStarred).isEqualTo(starred);
			}
		} finally {
			c.close();
		}
	}

	private void assertInsertQuery(Repository repository, boolean starred) {
		addRepository(repository, starred);
		assertQueryReturns(repository, starred);
	}

	private void addRepository(Repository repository, boolean starred) {
		final Uri uri = insertRepository(repository, starred);
		assertThat(uri).isEqualTo(BitbeakerContract.Repository.CONTENT_URI);
	}

	private Uri insertRepository(Repository repository, boolean starred) {
		ContentValues values = new ContentValues();
		values.put(BitbeakerContract.Repository.REPO_OWNER, repository.getOwner());
		values.put(BitbeakerContract.Repository.REPO_SLUG, repository.getSlug());
		values.put(BitbeakerContract.Repository.REPO_NAME, repository.getName());
		values.put(BitbeakerContract.Repository.REPO_PRIVATE, repository.isPrivateRepository());
		values.put(BitbeakerContract.Repository.REPO_STARRED, starred);
		return cr.insert(BitbeakerContract.Repository.CONTENT_URI, values);
	}

	private int getCol(Cursor c, String columnName) {
		int col = c.getColumnIndex(columnName);
		String message = "Column " + columnName + " not found;"
				+ "columns: " + Arrays.toString(c.getColumnNames());
		assert_().withFailureMessage(message).that(col).isNotEqualTo(-1);
		return col;
	}

	private String getStringCol(Cursor c, String columnName) {
		return c.getString(getCol(c, columnName));
	}

	private int getIntCol(Cursor c, String columnName) {
		return c.getInt(getCol(c, columnName));
	}

	private boolean getBooleanCol(Cursor c, String columnName) {
		int i = getIntCol(c, columnName);
		return i != 0;
	}

	private void deleteUri(Uri uri) {
		int count = cr.delete(uri, null, null);
		assert_().withFailureMessage("Failed to delete " + uri).that(count).isEqualTo(1);
	}
}
