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

package com.liferay.dynamic.data.mapping.model.impl;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @author Marcos Martins
 */
public class DDMFormInstanceReportImpl extends DDMFormInstanceReportBaseImpl {

	public DDMFormInstanceReportImpl() {
	}

	@Override
	public String getLastModifiedDate(Locale locale, TimeZone timeZone) {
		Date modifiedDate = getModifiedDate();

		int daysBetween = DateUtil.getDaysBetween(
			new Date(modifiedDate.getTime()), new Date(), timeZone);

		String languageKey = "report-was-last-modified-on-x";

		String relativeTimeDescription = StringUtil.removeSubstring(
			Time.getRelativeTimeDescription(modifiedDate, locale, timeZone),
			StringPool.PERIOD);

		if (daysBetween < 2) {
			languageKey = "report-was-last-modified-x";

			relativeTimeDescription = StringUtil.toLowerCase(
				relativeTimeDescription);
		}

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			locale, DDMFormInstanceReportImpl.class);

		return LanguageUtil.format(
			resourceBundle, languageKey, relativeTimeDescription, false);
	}

	@Override
	public int getTotalItems() throws PortalException {
		return JSONFactoryUtil.createJSONObject(
			getData()
		).getInt(
			"totalItems"
		);
	}

}