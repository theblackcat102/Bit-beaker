/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.iki.kuitsi.bitbeaker.util;

public class StringUtils {

	/**
	 * Checks if a CharSequence is empty ("") or null.
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * Checks if a CharSequence is not empty ("") and not null.

	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is not empty and not null
	 */
	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	/**
	 * Checks if a CharSequence is whitespace, empty ("") or null.
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is null, empty or whitespace
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a CharSequence is not empty (""), not null and not whitespace only.
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is
	 *  not empty and not null and not whitespace
	 */
	public static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	/**
	 * <p>Checks if the CharSequence contains only lowercase characters.</p>
	 *
	 * <p>{@code null} will return {@code false}.
	 * An empty CharSequence (length()=0) will return {@code false}.</p>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if only contains lowercase characters, and is non-null
	 */
	public static boolean isAllLowerCase(CharSequence cs) {
		if(cs != null && !isEmpty(cs)) {
			int sz = cs.length();

			for (int i = 0; i < sz; ++i) {
				if (!Character.isLowerCase(cs.charAt(i))) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>Checks if the CharSequence contains only uppercase characters.</p>
	 *
	 * <p>{@code null} will return {@code false}.
	 * An empty String (length()=0) will return {@code false}.</p>
	 *
	 * <pre>
	 * StringUtils.isAllUpperCase(null)   = false
	 * StringUtils.isAllUpperCase("")     = false
	 * StringUtils.isAllUpperCase("  ")   = false
	 * StringUtils.isAllUpperCase("ABC")  = true
	 * StringUtils.isAllUpperCase("aBC") = false
	 * </pre>
	 *
	 * @param cs the CharSequence to check, may be null
	 */
	public static boolean isAllUpperCase(final CharSequence cs) {
		if (cs == null || isEmpty(cs)) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isUpperCase(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
