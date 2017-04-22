package fi.iki.kuitsi.bitbeaker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HelperTest {

	@Test
	public void isJsonEmpty_with_empty_results() {
		assertTrue("Null should be evaluated as empty!", Helper.isJsonEmpty(null));
		assertTrue("Empty string should be evaluated as empty!", Helper.isJsonEmpty(""));
		assertTrue("String consisting of whitespace should be evaluated as empty!", Helper.isJsonEmpty("   "));
		assertTrue("String consisting of whitespace should be evaluated as empty!", Helper.isJsonEmpty(" \n\t \r"));
		assertTrue("The literal 'null' should be evaluated as empty!", Helper.isJsonEmpty("null"));
		assertTrue("The literal 'null' should be evaluated as empty even with whitespace around it!", Helper.isJsonEmpty("    null\n"));
	}

	@Test
	public void isJsonEmpty_with_nonempty_results() {
		assertFalse(Helper.isJsonEmpty("test"));
		assertFalse(Helper.isJsonEmpty("µ"));
		assertFalse(Helper.isJsonEmpty("asd null          "));
		assertFalse(Helper.isJsonEmpty("          null asd"));
		assertFalse(Helper.isJsonEmpty("       asd null   "));
	}

	@Test
	public void isImage_with_images() {
		assertTrue(Helper.isImage("foo.jpg"));
		assertTrue(Helper.isImage("BAR.PNG"));
		assertTrue(Helper.isImage("test%20file.gif"));
		assertTrue(Helper.isImage("asdasd.BMP"));
	}

	@Test
	public void isImage_with_nonimages() {
		assertFalse(Helper.isImage("foo.jpgggg"));
		assertFalse(Helper.isImage("BAR.ping"));
		assertFalse(Helper.isImage("test%20file.doc"));
		assertFalse(Helper.isImage("asdasd.BMP.exe"));
		assertFalse(Helper.isImage(""));
		assertFalse(Helper.isImage(null));
	}

	@Test
	public void getPlainHtmlLink() {
		assertEquals("<a href=\"http://www.example.com/foobar\">http://www.example.com/foobar</a>", 
					Helper.getHtmlLink("http://www.example.com/foobar"));
	}
}
