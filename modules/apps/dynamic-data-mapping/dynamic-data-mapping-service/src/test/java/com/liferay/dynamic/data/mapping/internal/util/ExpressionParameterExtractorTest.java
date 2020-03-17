/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.internal.util;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcos Martins
 */
public class ExpressionParameterExtractorTest {

	@Test
	public void testExtractParameters() {
		_assertParameterArray(
			_extractParameters("equals(Country, \"US\")"),
			new String[] {"Country", "\"US\""});

		_assertParameterArray(
			_extractParameters("equals(sum(1,1), 2)"),
			new String[] {"1", "1", "2"});
	}

	private void _assertParameterArray(
		String[] actualParameterArray, String[] expectedParameterArray) {

		Assert.assertEquals(
			Arrays.deepToString(expectedParameterArray),
			Arrays.deepToString(actualParameterArray));
	}

	private String[] _extractParameters(String visibilityExpression) {
		return ExpressionParameterExtractor.extractParameters(
			visibilityExpression);
	}

}