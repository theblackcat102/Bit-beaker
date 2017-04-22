package fi.iki.kuitsi.bitbeaker;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import com.petebevin.markdown.MarkdownProcessor;

import org.xml.sax.XMLReader;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarkupHelper {

	private MarkupHelper() { }

	private static final String LINK_TAG = "[[";
	private static final String HTML_HREF_ATTR_END = "\">";
	private static final String HTML_LINK_TAG_END = "</a>";
	private static final String HTML_HREF_ATTR = "href=\"";
	private static final String HTML_SRC_ATTR = "src=\"";
	private static final Date MARKDOWN_INTRODUCTION;
	private static MarkdownProcessor m = new MarkdownProcessor();

	static {
		Calendar cal = new GregorianCalendar();
		cal.set(2012, Calendar.OCTOBER, 4);
		MARKDOWN_INTRODUCTION = new Date(cal.getTimeInMillis());
	}

	/**
	 * Content is rendered using Markdown for any comment updated after Oct 4 2012, and Creole for
	 * any comment updated prior to that date.
	 *
	 * @param utc_updated_timestamp Last modified field from API. Note: issue and issue/comments
	 * resources have different names for it!
	 * @return true if content should be rendered using Creole; false means Markdown
	 * @see <a href="https://bitbucket.org/site/master/issue/6474/content_rendered-field-in-comments-bb-7656#comment-3235968">Bitbucket issue #6474</a>
	 */
	public static boolean isCreole(Date utc_updated_timestamp) {
		return MARKDOWN_INTRODUCTION.after(utc_updated_timestamp);
	}

	/**
	 * Add Bitbucket's markup support for comments and issue descriptions to TextView.
	 * <p/>
	 * Remember to call <code>setMovementMethod(LinkMovementMethod.getInstance())</code>
	 * on TextView if you want links to be clickable ;)
	 *
	 * @param input String with markup tags (Markdown, Creole)
	 * @param owner owner of currently viewed repo, used in links
	 * @param slug slug of currently viewed repo, used in links
	 * @param creole Render contents with Creole (true) or Markdown (false)
	 * @return Spanned ready to TextView.setText()
	 */
	public static Spanned handleMarkup(final String input, final String owner, final String slug, boolean creole) {
		if (creole) {
			return handleHTML(handleCreoleAsString(input, owner, slug), null, null);
		}
		String output = convertBitbucketCodeBlocks(input);
		return handleHTML(handleMarkdownAsString(output, owner, slug), null, null);
	}

	/**
	 * Prepares HTML String for TextView.
	 * <p/>
	 * Remember to call <code>setMovementMethod(LinkMovementMethod.getInstance())</code>
	 * on TextView if you want links to be clickable ;)
	 *
	 * @param inputHtml String with HTML tags
	 * @param view TextView where HTML String is added to
	 * @param ctx Context, used for loading resources etc.
	 * @return Spanned ready to TextView.setText()
	 */
	public static Spanned handleHTML(final String inputHtml, final TextView view, final Context ctx) {
		String html = fixWrongStyles(inputHtml);
		if (view == null || ctx == null) {
			return Html.fromHtml(html, null, new MarkupHelper.TagHandler());
		}
		Html.ImageGetter imageGetter = AppComponentService.obtain(ctx.getApplicationContext())
				.imageGetterFactory().create(ctx, view);
		return Html.fromHtml(html, imageGetter, new MarkupHelper.TagHandler());
	}

	/**
	 * Prepares HTML String for TextView.
	 * <p/>
	 * This method supports more html tags in TextView via TagHandler than Html.fromHtml(String)
	 *
	 * @param html String with html tags
	 * @return Spanned ready to TextView.setText()
	 * @see {@link #handleHTML(String, TextView, Context)} if you need support for &lt;img&gt; tags
	 */
	public static Spanned handleHTML(final String html) {
		return handleHTML(html, null, null);
	}

	/**
	 * android.text.Html.fromHtml flips styles of tags <code>em</code> to bold
	 * and <code>strong</code> to italics and we don't like that.
	 *
	 * @param input String with HTML tags
	 * @return String containing alternative tags with desired rendering
	 */
	private static String fixWrongStyles(final String input) {
		String output = input.replaceAll("<em>(.+?)</em>", "<i>$1</i>");
		output = output.replaceAll("<strong>(.+?)</strong>", "<b>$1</b>");
		return output;
	}

	private static String handleMarkdownAsString(final String input, final String owner, final String slug) {
		String output = m.markdown(input);
		output = getEmailAddressesLinkified(output);
		output = getBareUrlsLinkified(output);
		output = getBitbucketLinksLinkified(output, owner, slug);

		return output;
	}

	private static String handleCreoleAsString(final String creoleInput, final String owner, final String slug) {
		String input = TextUtils.htmlEncode(creoleInput);
		input = input.replaceAll("\\r", "");
		input = input.replaceAll("\\n", "<br />");

		StringBuilder output = new StringBuilder();
		String codeStartTag = "{{{";
		String codeEndTag = "}}}";
		int code_begin = 0;
		int code_end = 0;
		int index = 0;// 0 -> input.length()

		while (code_begin > -1) {
			code_begin = input.indexOf(codeStartTag, index);
			code_end = input.indexOf(codeEndTag, code_begin);

			// we found complete code tag
			if (code_begin > -1 && code_end > -1) {
				// text before code tag
				output.append(decorateCreole(input.substring(index, code_begin), owner, slug));

				// contents of code tag
				String code_contents = input.substring(code_begin + codeStartTag.length(), code_end);
				output.append("<pre><code>").append(code_contents).append("</code></pre>");

				index = code_end + codeEndTag.length();
			}
		}

		// text after the last code tag
		output.append(decorateCreole(input.substring(index, input.length()), owner, slug));

		return output.toString();
	}

	/**
	 * Helper method.
	 *
	 * @see {@link #handleMarkup(String, String, String, boolean)}
	 */
	public static String decorateCreole(final String input, final String owner, final String slug) {
		// headings
		String output = input.replaceAll("====(.+?)====", "<h4>$1</h4>");
		output = output.replaceAll("===(.+?)===", "<h3>$1</h3>");
		output = output.replaceAll("==(.+?)==", "<h2>$1</h2>");
		output = output.replaceAll("(</h\\d>)(<br />)+", "$1"); // remove extra line breaks after headings

		output = output.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>"); // bold
		output = output.replaceAll("(?<!:)//(.+?)(?<!:)//", "<i>$1</i>"); // italic

		// links
		output = getEmailAddressesLinkified(output);
		output = getBareUrlsLinkified(output);
		output = output.replaceAll("\\[\\[(.*?)\\|(.*?)\\]\\]", "<a href=\"$1\">$2</a>");
		output = getBitbucketLinksLinkified(output, owner, slug);

		return output;
	}

	private static String getBitbucketLinksLinkified(final String input, final String owner, final String slug) {
		String output = input.replaceAll("#(\\d+)", "<a href=\"https://bitbucket.org/" + owner + "/" + slug + "/issues/$1\">#$1</a>");
		output = output.replaceAll("(?<!\\w)@([\\w\\d_-]{1,30})", "<a href=\"https://bitbucket.org/$1\">@$1</a>"); // username up to 30 chars: letters, numbers, underscores, hyphens
		output = output.replaceAll("(?<![a-zA-Z0-9])([a-fA-F0-9]{7,40})(?!([a-zA-Z0-9\"]|</a>))(?=[^>]*(<|$))", "<a href=\"https://bitbucket.org/" + owner + "/" + slug + "/commits/$1\">$1</a>");// changeset 7-40 hex chars
		return output;
	}

	public static String getBareUrlsLinkified(final String input) {
		Matcher matcher = Patterns.WEB_URL.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			int matchStart = matcher.start();
			int matchEnd = matcher.end();
			// This URL might be surrounded with the [[ and ]] tags, assigning it a custom link text:
			int possibleLinkTagStart = (matchStart >= LINK_TAG.length() ? matchStart - LINK_TAG.length() : 0);
			// This URL might be the domain part of an already linkified email address, right before the closing </a> tag:
			int possibleHtmlLinkEnd = (matchEnd + HTML_LINK_TAG_END.length() > input.length() ? input.length() : matchEnd + HTML_LINK_TAG_END.length());
			// On the other hand this URL might also be the domain part of an already linkified email address, inside the href-parameter:
			int possibleHrefAttributeEnd = (matchEnd + HTML_HREF_ATTR_END.length() > input.length() ? input.length() : matchEnd + HTML_HREF_ATTR_END.length());
			// This URL might be an absolute URL value of a src attribute (img tag)
			int possibleSrcAttrStart = (matchStart >= HTML_SRC_ATTR.length() ? matchStart - HTML_SRC_ATTR.length() : 0);
			int possibleHrefAttrStart = (matchStart >= HTML_HREF_ATTR.length() ? matchStart - HTML_HREF_ATTR.length() : 0);

			final String address = matcher.group(0);
			if ("README.md".equalsIgnoreCase(address)) {
				continue;
			}
			if (!input.substring(possibleLinkTagStart, matchStart).equals(LINK_TAG)
					&& !input.substring(matchEnd, possibleHrefAttributeEnd).equals(HTML_HREF_ATTR_END)
					&& !input.substring(matchEnd, possibleHtmlLinkEnd).equals(HTML_LINK_TAG_END)
					&& !input.substring(possibleHrefAttrStart, matchStart).equals(HTML_HREF_ATTR)
					&& !input.substring(possibleSrcAttrStart, matchStart).equals(HTML_SRC_ATTR)
					&& ( (input.length() == matchEnd) // match found at the end of string
						|| ( (input.length() >= matchEnd + 1) && !(input.charAt(matchEnd) == '"') ) // already linkified URL
						|| ( (input.length() >= matchEnd + 2) && !(input.substring(matchEnd, matchEnd + 2).equals("/\"")) ) // Patterns.WEB_URL does not include trailing slash
					) ) {
				String protocol = "";
				if (!address.startsWith("http://")
						&& !address.startsWith("https://")
						&& !address.startsWith("ftp://")) {
					protocol = "http://";
				}
				matcher.appendReplacement(sb, "<a href=\"" + protocol + address + "\">" + address + "</a>");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private static String getEmailAddressesLinkified(final String input) {
		Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			int matchStart = matcher.start();
			int start = (matchStart >= LINK_TAG.length() ? matchStart - LINK_TAG.length() : 0);
			final String address = matcher.group(0);
			if (!input.substring(start, matchStart).equals(LINK_TAG)) {
				matcher.appendReplacement(sb, "<a href=\"mailto:" + address + "\">" + address + "</a>");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static String fixRelativeLinks(final String contents) {
		return contents.replaceAll("<a href=\\\"/", "<a href=\"https://bitbucket.org/");
	}

	/**
	 * Pre-processes Bitbucket code blocks for Markdown.
	 *
	 * @param input String that may contain Bitbucket code blocks ```some code```
	 * @return String where code blocks are formatted according to Markdown specs
	 */
	private static String convertBitbucketCodeBlocks(final String input) {
		Matcher matcher = Pattern.compile("```(.+?)```", Pattern.DOTALL).matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String codeblock = matcher.group(1)//
					.replaceAll("\\r\\n", "\n")//
					.replaceAll("\\n", "\n    ");// code blocks in Markdown are indented with 4 spaces
			matcher.appendReplacement(sb, Matcher.quoteReplacement(codeblock));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	static class TagHandler implements Html.TagHandler {
		/**
		 * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
		 * and on top of Stack is the most nested list.
		 */
		Stack<String> lists = new Stack<String>();
		/**
		 * Tracks indexes of ordered lists so that after a nested list ends
		 * we can continue with correct index of outer list.
		 */
		Stack<Integer> olNextIndex = new Stack<Integer>();
		/**
		 * List indentation in pixels. Nested lists use multiple of this.
		 */
		private static final int indent = 10;
		private static final int listItemIndent = indent * 2;
		private static final BulletSpan bullet = new BulletSpan(indent);

		@Override
		public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
			if ("code".equalsIgnoreCase(tag)) {
				if (opening) {
					start(output, new Code());
				} else {
					end(output, Code.class, new TypefaceSpan("monospace"));
				}
			/*} else if (tag.equalsIgnoreCase("pre")) {
				// keeping preformatted text structure inside HTML formatted TextView requires
				// probably scrollviews and some other processing so let's do something simpler :)
				if (opening) {
					start(output, new Pre());
				} else {
					//TODO: htmlencode contents of editable without destroying existing Spans
					//TODO: replace \n -> <br>, space -> &nbsp; and \t -> &nbsp;&nbsp;&nbsp;&nbsp;
					end(output, Pre.class, new BackgroundColorSpan(Color.LTGRAY));
				}*/
			} else if ("ul".equalsIgnoreCase(tag)) {
				if (opening) {
					lists.push(tag);
				} else {
					lists.pop();
				}
			} else if ("ol".equalsIgnoreCase(tag)) {
				if (opening) {
					lists.push(tag);
					olNextIndex.push(Integer.valueOf(1)).toString();//TODO: add support for lists starting other index than 1
				} else {
					lists.pop();
					olNextIndex.pop().toString();
				}

			} else if ("li".equalsIgnoreCase(tag)) {
				if (opening) {
					if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
						output.append("\n");
					}
					String parentList = lists.peek();
					if ("ol".equalsIgnoreCase(parentList)) {
						start(output, new Ol());
						output.append(olNextIndex.peek().toString()).append(". ");
						olNextIndex.push(Integer.valueOf(olNextIndex.pop().intValue() + 1));
					} else if ("ul".equalsIgnoreCase(parentList)) {
						start(output, new Ul());
					}
				} else {
					if ("ul".equalsIgnoreCase(lists.peek())) {
						if (output.charAt(output.length() - 1) != '\n') {
							output.append("\n");
						}
						// Nested BulletSpans increases distance between bullet and text, so we must prevent it.
						int bulletMargin = indent;
						if (lists.size() > 1) {
							bulletMargin = indent - bullet.getLeadingMargin(true);
							if (lists.size() > 2) {
								// This get's more complicated when we add a LeadingMarginSpan into the same line:
								// we have also counter it's effect to BulletSpan
								bulletMargin -= (lists.size() - 2) * listItemIndent;
							}
						}
						BulletSpan newBullet = new BulletSpan(bulletMargin);
						end(output,
								Ul.class,
								new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
								newBullet);
					} else if ("ol".equalsIgnoreCase(lists.peek())) {
						if (output.charAt(output.length() - 1) != '\n') {
							output.append("\n");
						}
						int numberMargin = listItemIndent * (lists.size() - 1);
						if (lists.size() > 2) {
							// Same as in ordered lists: counter the effect of nested Spans
							numberMargin -= (lists.size() - 2) * listItemIndent;
						}
						end(output,
								Ol.class,
								new LeadingMarginSpan.Standard(numberMargin));
					}
				}
			} else if (("html".equalsIgnoreCase(tag) || "body".equalsIgnoreCase(tag))) {
				// just ignore these
			} else {
				if (opening) Log.d("TagHandler", "Found an unsupported tag " + tag);
			}
		}

		private static void start(Editable text, Object mark) {
			int len = text.length();
			text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
		}

		private static void end(Editable text, Class<?> kind, Object... replaces) {
			int len = text.length();
			Object obj = getLast(text, kind);
			int where = text.getSpanStart(obj);
			text.removeSpan(obj);
			if (where != len) {
				for (Object replace : replaces) {
					text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}

		private static Object getLast(Spanned text, Class<?> kind) {
			/*
			 * This knows that the last returned object from getSpans()
			 * will be the most recently added.
			 */
			Object[] objs = text.getSpans(0, text.length(), kind);
			if (objs.length == 0) {
				return null;
			}
			return objs[objs.length - 1];
		}

		protected static class Code {
		}

		//protected static class Pre { }
		protected static class Ul {
		}

		protected static class Ol {
		}

	}

}
