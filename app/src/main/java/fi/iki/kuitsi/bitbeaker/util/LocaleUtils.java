package fi.iki.kuitsi.bitbeaker.util;

import java.util.Locale;

/**
 * Operations to assist when working with a {@link Locale}.
 */
public class LocaleUtils {

	private LocaleUtils() { }

	/**
	 * <p>Converts a String to a Locale.</p>
	 *
	 * <p>This method takes the string format of a locale and creates the
	 * locale object from it.</p>
	 *
	 * <pre>
	 *   LocaleUtils.toLocale("")           = new Locale("", "")
	 *   LocaleUtils.toLocale("en")         = new Locale("en", "")
	 *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
	 *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
	 * </pre>
	 *
	 * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
	 * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
	 * Thus, the result from getVariant() may vary depending on your JDK.</p>
	 *
	 * <p>This method validates the input strictly.
	 * The language code must be lowercase.
	 * The country code must be uppercase.
	 * The separator must be an underscore.
	 * The length must be correct.
	 * </p>
	 *
	 * @param str  the locale String to convert, null returns null
	 * @return a Locale, null if null input
	 * @throws IllegalArgumentException if the string is an invalid format
	 * @see Locale#forLanguageTag(String)
	 */
	public static Locale toLocale(final String str) {
		if (str == null) {
			return null;
		}
		if (str.isEmpty()) { // LANG-941 - JDK 8 introduced an empty locale where all fields are blank
			return new Locale("", "");
		}
		if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
			throw new IllegalArgumentException("Invalid locale format: " + str);
		}
		final int len = str.length();
		if (len < 2) {
			throw new IllegalArgumentException("Invalid locale format: " + str);
		}
		final char ch0 = str.charAt(0);
		if (ch0 == '_') {
			if (len < 3) {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			final char ch1 = str.charAt(1);
			final char ch2 = str.charAt(2);
			if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			if (len == 3) {
				return new Locale("", str.substring(1, 3));
			}
			if (len < 5) {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			if (str.charAt(3) != '_') {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			return new Locale("", str.substring(1, 3), str.substring(4));
		}

		String[] split = str.split("_", -1);
		int occurrences = split.length - 1;
		switch (occurrences) {
			case 0:
				if (StringUtils.isAllLowerCase(str) && (len == 2 || len == 3)) {
					return new Locale(str);
				} else {
					throw new IllegalArgumentException("Invalid locale format: " + str);
				}

			case 1:
				if (StringUtils.isAllLowerCase(split[0])
						&& (split[0].length() == 2 || split[0].length() == 3)
						&& split[1].length() == 2 && StringUtils.isAllUpperCase(split[1])) {
					return new Locale(split[0], split[1]);
				} else {
					throw new IllegalArgumentException("Invalid locale format: " + str);
				}

			case 2:
				if (StringUtils.isAllLowerCase(split[0])
						&& (split[0].length() == 2 || split[0].length() == 3)
						&& (split[1].length() == 0 || (split[1].length() == 2 && StringUtils.isAllUpperCase(split[1])))
						&& split[2].length() > 0) {
					return new Locale(split[0], split[1], split[2]);
				}

				//$FALL-THROUGH$
			default:
				throw new IllegalArgumentException("Invalid locale format: " + str);
		}
	}
}
