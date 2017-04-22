/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.iki.kuitsi.bitbeaker.util;

import java.util.Comparator;

/**
 * A utility for performing a chained comparison statement.
 */
public abstract class ComparisonChain {

	static final ComparisonChain INSTANCE = new ComparisonChain() {
		@Override
		public <T> ComparisonChain compare(T lhs, T rhs, Comparator<? super T> comparator) {
			return classify(comparator.compare(lhs, rhs));
		}

		@Override
		public int result() {
			return 0;
		}

		private ComparisonChain classify(int result) {
			return (result < 0) ? LESS : (result > 0) ? GREATER : INSTANCE;
		}
	};

	static final ComparisonChain LESS = new InactiveComparisonChain(-1);
	static final ComparisonChain GREATER = new InactiveComparisonChain(1);

	private ComparisonChain() { }
	
	public static ComparisonChain start() {
		return INSTANCE;
	}

	public abstract <T> ComparisonChain compare(T lhs, T rhs, Comparator<? super T> comparator);

	public abstract int result();

	private static final class InactiveComparisonChain extends ComparisonChain {

		private final int result;

		public InactiveComparisonChain(int result) {
			this.result = result;
		}

		@Override
		public <T> ComparisonChain compare(T lhs, T rhs, Comparator<? super T> comparator) {
			return this;
		}

		@Override
		public int result() {
			return result;
		}
	}
}
