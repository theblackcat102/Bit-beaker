package fi.iki.kuitsi.bitbeaker;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = 19, packageName="fi.iki.kuitsi.bitbeaker")
public class MarkupHelperTest {

	@Test
	public void test_decorate_headers_h2() {
		assertEquals("lorem ipsum <h2>dolor</h2> sit amet", 
				MarkupHelper.decorateCreole("lorem ipsum ==dolor== sit amet", "", ""));
	}

	@Test
	public void test_decorate_headers_h3() {
		assertEquals("lorem ipsum <h3>dolor sit</h3> amet", 
				MarkupHelper.decorateCreole("lorem ipsum ===dolor sit=== amet", "", ""));
	}

	@Test
	public void test_decorate_headers_h4() {
		assertEquals("<h4>lorem ipsum dolor sit amet</h4>", 
				MarkupHelper.decorateCreole("====lorem ipsum dolor sit amet====", "", ""));
	}

	@Test
	public void test_decorate_headers_h2_and_h3() {
		assertEquals("lorem ipsum <h2>dolor</h2> sit amet, consectetur <h3>adipisicing</h3> elit", 
				MarkupHelper.decorateCreole("lorem ipsum ==dolor== sit amet, consectetur ===adipisicing=== elit", "", ""));
	}

	@Test
	public void test_decorate_removing_line_breaks_after_headings() {
		assertEquals("<h2>aaa</h2>bbb<h3>ccc</h3>ddd",
				MarkupHelper.decorateCreole("==aaa==<br />bbb===ccc===<br /><br /><br />ddd", "", ""));
	}

	@Test
	public void test_decorate_boldface() {
		assertEquals("aaa <b>bb cc</b> dd ee ff <b>gg</b><b>hh ii</b>",
				MarkupHelper.decorateCreole("aaa **bb cc** dd ee ff **gg****hh ii**", "", ""));
	}

	@Test
	public void test_decorate_italics() {
		assertEquals("aaa <i>bb cc</i> dd ee ff <i>gg</i><i>hh ii</i>",
				MarkupHelper.decorateCreole("aaa //bb cc// dd ee ff //gg////hh ii//", "", ""));
	}

	@Test
	public void test_decorate_italics_around_bracketed_url() {
		assertEquals("aaa <i><a href=\"http://www.example.com/\">bb cc</a></i> dd",
				MarkupHelper.decorateCreole("aaa //[[http://www.example.com/|bb cc]]// dd", "", ""));
	}

	@Test
	public void test_decorate_italics_around_plain_url() {
		assertEquals("aaa <i><a href=\"http://www.example.com\">http://www.example.com</a></i> dd",
				MarkupHelper.decorateCreole("aaa //http://www.example.com// dd", "", ""));
	}

	@Test
	public void test_decorate_issue_number_links() {
		assertEquals("Failed to correctly create a link to an issue!", 
				"aaa <a href=\"https://bitbucket.org/myself/myrepo/issues/47\">#47</a> bbb",
				MarkupHelper.decorateCreole("aaa #47 bbb", "myself", "myrepo"));
		
		assertEquals("Incorrectly tried to create a link to an issue!", 
				"aaa #n bbb",
				MarkupHelper.decorateCreole("aaa #n bbb", "myself", "myrepo"));
		
		assertEquals("Incorrectly tried to create a link to an issue!", 
				"aaa # bbb",
				MarkupHelper.decorateCreole("aaa # bbb", "myself", "myrepo"));
	}

	@Test
	public void test_decorate_username_links() {
		assertEquals("Basic username link test case fails!", 
				"Hello <a href=\"https://bitbucket.org/John\">@John</a>!", 
				MarkupHelper.decorateCreole("Hello @John!", "", ""));
		
		assertEquals("Username link at the beginning of the string fails!", 
				"<a href=\"https://bitbucket.org/Test\">@Test</a> test asd", 
				MarkupHelper.decorateCreole("@Test test asd", "", ""));
	}

	@Test
	public void test_decorate_changeset_links() {
		assertEquals("Basic changeset link test case fails!", 
				"<a href=\"https://bitbucket.org/user/repo/commits/0123456789abcdef\">0123456789abcdef</a>",
				MarkupHelper.decorateCreole("0123456789abcdef", "user", "repo"));
		
		assertEquals("Another basic changeset link test case fails!", 
				"<a href=\"https://bitbucket.org/user/repo/commits/8334708\">8334708</a>",
				MarkupHelper.decorateCreole("8334708", "user", "repo"));
		
		assertEquals("Basic changeset link at the end of the string test case fails!", 
				"It was done in <a href=\"https://bitbucket.org/user/repo/commits/abcd12345\">abcd12345</a>",
				MarkupHelper.decorateCreole("It was done in abcd12345", "user", "repo"));
		
		assertEquals("Basic changeset link ending in period fails!", 
				"I dit it in <a href=\"https://bitbucket.org/user/repo/commits/1234cafe\">1234cafe</a>.",
				MarkupHelper.decorateCreole("I dit it in 1234cafe.", "user", "repo"));
		
		assertEquals("Basic changeset link in parenthesis fails!", 
				"The changes (<a href=\"https://bitbucket.org/user/repo/commits/DEADB33F\">DEADB33F</a>) are superb!",
				MarkupHelper.decorateCreole("The changes (DEADB33F) are superb!", "user", "repo"));
	}

	@Test
	public void test_decorate_several_changeset_links() {
		assertEquals("Test with two plain changeset ids fails!",
				"<a href=\"https://bitbucket.org/saibotd/bitbeaker/commits/083ab57537\">083ab57537</a>\n" +
				"<a href=\"https://bitbucket.org/saibotd/bitbeaker/commits/102be9c\">102be9c</a>",
				MarkupHelper.decorateCreole("083ab57537\n" +
		                                    "102be9c", "saibotd", "bitbeaker"));
		
		assertEquals("Test with two already HTML changeset ids fails!",
				"<a href=\"https://bitbucket.org/saibotd/bitbeaker/changeset/083ab57537\">https://bitbucket.org/saibotd/bitbeaker/changeset/083ab57537</a>\n" +
				"<a href=\"https://bitbucket.org/saibotd/bitbeaker/changeset/102be9c\">https://bitbucket.org/saibotd/bitbeaker/changeset/102be9c</a>",
				MarkupHelper.decorateCreole("https://bitbucket.org/saibotd/bitbeaker/changeset/083ab57537\n" +
		                                    "https://bitbucket.org/saibotd/bitbeaker/changeset/102be9c", "saibotd", "bitbeaker"));
		
		assertEquals("Test with two different kind of changeset ids fails!",
				"Testing:\n\n* <a href=\"https://bitbucket.org/saibotd/bitbeaker/changeset/083ab57537\">https://bitbucket.org/saibotd/bitbeaker/changeset/083ab57537</a>" +
				          "\n* <a href=\"https://bitbucket.org/saibotd/bitbeaker/commits/102be9c\">102be9c</a>",
				MarkupHelper.decorateCreole("Testing:\n\n* https://bitbucket.org/saibotd/bitbeaker/changeset/083ab57537" +
		                                    "\n* 102be9c", "saibotd", "bitbeaker"));
	}

	@Test
	public void test_decorate_changeset_links_not_being_too_aggressive() {
		assertEquals("The decoration method messed up a link that was already in HTML format!", 
				"Changeset <a href=\"https://bitbucket.org/user/repo/changeset/abc123def456\">abc123def456</a> rocks!", 
				MarkupHelper.decorateCreole("Changeset <a href=\"https://bitbucket.org/user/repo/changeset/abc123def456\">abc123def456</a> rocks!", "user", "repo"));

		assertEquals("The decoration method messed up a link within HTML tags!",
				"<img src=\"https://bitbucket.org/repo/6oM8b/images/3712381094-image_file.jpg\"/>",
				MarkupHelper.decorateCreole("<img src=\"https://bitbucket.org/repo/6oM8b/images/3712381094-image_file.jpg\"/>", "user", "repo"));

		assertEquals("The decoration method shouldn't have linkified a string in the middle of another string!", 
				     "OMG1234567ABCWTF", MarkupHelper.decorateCreole("OMG1234567ABCWTF", "owner", "slug"));
		
		assertEquals("The decoration method shouldn't have linkified a string at the beginning of another string!", 
			         "1234567ABCWTF", MarkupHelper.decorateCreole("1234567ABCWTF", "owner", "slug"));
		
		assertEquals("The decoration method shouldn't have linkified a string at the end of another string!", 
		             "OMG1234567ABC", MarkupHelper.decorateCreole("OMG1234567ABC", "owner", "slug"));
	}

	@Test
	public void test_plain_url_linkifier_not_being_too_aggressive() {
		assertEquals("URL ending with slash inside <a> tag shouldn't have been linkified as a plain URL",
				"<p>This is <a href=\"http://www.example.com/\">an example</a> inline link.</p>",
				MarkupHelper.getBareUrlsLinkified("<p>This is <a href=\"http://www.example.com/\">an example</a> inline link.</p>"));

		assertEquals("URL inside <a> tag with extra attributes shouldn't have been linkified as a plain URL",
				"<p>This is <a href=\"http://www.example.com\" title=\"Title\">an example</a> inline link.</p>",
				MarkupHelper.getBareUrlsLinkified("<p>This is <a href=\"http://www.example.com\" title=\"Title\">an example</a> inline link.</p>"));
	}

	@Test
	public void test_decorate_email_addresses() {
		assertEquals("An email address wasn't rendered properly!",
				"<a href=\"mailto:someone@example.org\">someone@example.org</a>", 
				MarkupHelper.decorateCreole("someone@example.org", "", ""));
		
		assertEquals("An email address at the start of the input wasn't rendered properly!",
				"<a href=\"mailto:someone@example.net\">someone@example.net</a> test asd foo qwerty", 
				MarkupHelper.decorateCreole("someone@example.net test asd foo qwerty", "", ""));
		
		assertEquals("An email address at the end of the input wasn't rendered properly!",
				"test asd foo qwerty <a href=\"mailto:someone@example.com\">someone@example.com</a>", 
				MarkupHelper.decorateCreole("test asd foo qwerty someone@example.com", "", ""));
		
		assertEquals("An email address with numbers wasn't rendered properly!",
				"Mail here: <a href=\"mailto:test01@example.org\">test01@example.org</a>, please!", 
				MarkupHelper.decorateCreole("Mail here: test01@example.org, please!", "", ""));
		
		assertEquals("An email address with dots wasn't rendered properly!",
				"<a href=\"mailto:first.m.last@example.org\">first.m.last@example.org</a>", 
				MarkupHelper.decorateCreole("first.m.last@example.org", "", ""));
	}

	@Test
	public void test_decorate_bracketed_urls() {
		assertEquals("foo <a href=\"http://www.example.com/\">bar</a> zok",
				MarkupHelper.decorateCreole("foo [[http://www.example.com/|bar]] zok", "", ""));
		
		assertEquals("aaa <a href=\"something.php\">bb cc dd</a>",
				MarkupHelper.decorateCreole("aaa [[something.php|bb cc dd]]", "", ""));
		
		assertEquals("<a href=\"file://foo.txt\">test1</a> test2",
				MarkupHelper.decorateCreole("[[file://foo.txt|test1]] test2", "", ""));
	}

	@Test
	public void test_decorate_plain_urls() {
		assertEquals("Basic plain URL fails!",
				"foo <a href=\"http://www.example.com\">http://www.example.com</a> zok",
				MarkupHelper.decorateCreole("foo http://www.example.com zok", "", ""));
		
		assertEquals("HTTPS URL with parameters fails!",
				"<a href=\"https://example.com/something.php?one=1\">https://example.com/something.php?one=1</a>",
				MarkupHelper.decorateCreole("https://example.com/something.php?one=1", "", ""));
		
		assertEquals("The test with two plain URLs fails!", 
				"test <a href=\"https://subdomain.www.example.net\">https://subdomain.www.example.net</a> <a href=\"http://www.example.org:8080/foo\">http://www.example.org:8080/foo</a> asd",
				MarkupHelper.decorateCreole("test https://subdomain.www.example.net http://www.example.org:8080/foo asd", "", ""));
	}

	@Test
	public void test_do_not_linkify_src_attribute_of_img() {
		assertEquals("Absolute URL value of src attribute linkified!",
				"<img src=\"https://bitbucket.org/repo/6oM8b/images/3712381094-image_file.jpg\"/>",
				MarkupHelper.getBareUrlsLinkified("<img src=\"https://bitbucket.org/repo/6oM8b/images/3712381094-image_file.jpg\"/>"));
	}

	@Test
	public void test_README_dot_md_not_becoming_a_link() {
		// .md is the top level domain of Moldova, so "readme.md" is a valid URL, but we'll handle it as an exception...
		assertEquals("readme.md", MarkupHelper.decorateCreole("readme.md", "", ""));
		assertEquals("This Readme.md should not be a link", MarkupHelper.decorateCreole("This Readme.md should not be a link", "", ""));
		assertEquals("README.md", MarkupHelper.decorateCreole("README.md", "", ""));
		assertEquals("Uppercase: README.MD", MarkupHelper.decorateCreole("Uppercase: README.MD", "", ""));
		
		// Assert that other possible readme-files still also work as they should:
		assertEquals("readme.txt", MarkupHelper.decorateCreole("readme.txt", "", ""));
		assertEquals("readme.pdf", MarkupHelper.decorateCreole("readme.pdf", "", ""));
		assertEquals("readme.doc", MarkupHelper.decorateCreole("readme.doc", "", ""));
	}

	@Test
	public void test_decorate_both_bracketed_and_plain_urls() {
		String expected = "<a href=\"http://bitbucket.org/\">foo</a> <a href=\"http://www.example.com\">http://www.example.com</a> zok";
		String actual = MarkupHelper.decorateCreole("[[http://bitbucket.org/|foo]] http://www.example.com zok", "", ""); 
		assertEquals("Test with one bracketed and one plain URL fails!", expected, actual);
	}

	@Test
	public void test_decorate_both_bracketed_and_plain_urls_times_two() {
		String expected = "<a href=\"http://a.com\">A</a> <a href=\"http://b.net\">http://b.net</a> <a href=\"http://c.org\">C</a> <a href=\"http://d.biz\">http://d.biz</a>";
		String actual = MarkupHelper.decorateCreole("[[http://a.com|A]] http://b.net [[http://c.org|C]] http://d.biz", "", "");
		assertEquals("Test with two bracketed and two plain URL fails!", expected, actual);
	}

	@Test
	public void test_decorate_both_bracketed_and_plain_urls_and_email_address() {
		String expected = "<a href=\"http://bitbucket.org/\">foo</a> <a href=\"http://www.example.com\">http://www.example.com</a> zok <a href=\"mailto:john.doe@example.com\">john.doe@example.com</a> baz";
		String actual = MarkupHelper.decorateCreole("[[http://bitbucket.org/|foo]] http://www.example.com zok john.doe@example.com baz", "", ""); 
		assertEquals("Test with one bracketed and one plain URL fails!", expected, actual);
	}

	@Test
	public void test_handleCreoleAsString() throws Exception {
		Method method = MarkupHelper.class.getDeclaredMethod("handleCreoleAsString", String.class, String.class, String.class);
		method.setAccessible(true);
		
		final String input = "==Test== {{{==asd== @name #42 **bold**}}} **bold \ntext**{{{\n\nhttp://example.com}}}";
		final String expected = "<h2>Test</h2> <pre><code>==asd== @name #42 **bold**</code></pre> <b>bold <br />text</b><pre><code><br /><br />http://example.com</code></pre>";
		final Object actual = method.invoke(null, input, "owner", "slug");
		assertEquals(expected, actual);
	}

	@Ignore
	@Test
	public void test_handleMarkdownAsString() throws Exception {
		Method method = MarkupHelper.class.getDeclaredMethod("handleMarkdownAsString", String.class, String.class, String.class);
		method.setAccessible(true);

		final String input = "Related answer https://bitbucket.org/saibotd/bitbeaker/issue/121/share-and-update-the-wiki#comment-10937512";
		final String expected = "Related answer https://bitbucket.org/saibotd/bitbeaker/issue/121/share-and-update-the-wiki#comment-10937512";
		final Object actual = method.invoke(null, input, "owner", "slug");
		assertEquals(expected, actual);
		// Fail: number at the end of the url rendered as changeset
	}
}
