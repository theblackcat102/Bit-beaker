package fi.iki.kuitsi.bitbeaker;

import android.test.ApplicationTestCase;

import fi.iki.kuitsi.bitbeaker.account.Authenticator;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract;

public class BitbeakerTest extends ApplicationTestCase<Bitbeaker> {

	public BitbeakerTest() {
		super(Bitbeaker.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	public void testCreateApp() throws Exception {
		createApplication();
		assertNotNull(getApplication());
	}

	public void testAccountType() throws Exception {
		assertEquals(Authenticator.getAccountType(),
				getContext().getString(R.string.account_type));
	}

	public void testContentAuthority() {
		assertEquals(BitbeakerContract.CONTENT_AUTHORITY,
				getContext().getString(R.string.content_authority));
	}
}
