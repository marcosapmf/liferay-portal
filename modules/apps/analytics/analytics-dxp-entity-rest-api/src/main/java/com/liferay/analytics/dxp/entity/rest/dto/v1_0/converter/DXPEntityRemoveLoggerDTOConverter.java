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

package com.liferay.analytics.dxp.entity.rest.dto.v1_0.converter;

import com.liferay.analytics.dxp.entity.rest.dto.v1_0.DXPEntityRemoveLogger;
import com.liferay.analytics.storage.model.AnalyticsModelRemoveLogger;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

/**
 * @author Marcos Martins
 */
public interface DXPEntityRemoveLoggerDTOConverter
	extends DTOConverter<AnalyticsModelRemoveLogger, DXPEntityRemoveLogger> {

	@Override
	public DXPEntityRemoveLogger toDTO(
			DTOConverterContext dtoConverterContext,
			AnalyticsModelRemoveLogger analyticsModelRemoveLogger)
		throws Exception;

}