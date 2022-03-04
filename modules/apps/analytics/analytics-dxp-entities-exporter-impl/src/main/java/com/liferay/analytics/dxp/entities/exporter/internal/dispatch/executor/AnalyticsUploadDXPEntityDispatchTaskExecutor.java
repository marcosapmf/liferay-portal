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

package com.liferay.analytics.dxp.entities.exporter.internal.dispatch.executor;

import com.liferay.analytics.batch.exportimport.manager.AnalyticsBatchExportImportManager;
import com.liferay.analytics.dxp.entities.exporter.dto.v1_0.DXPEntity;
import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.executor.DispatchTaskStatus;
import com.liferay.dispatch.model.DispatchLog;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.dispatch.service.DispatchLogLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcos Martins
 */
@Component(
	enabled = true, immediate = true,
	property = {
		"dispatch.task.executor.name=" + AnalyticsUploadDXPEntityDispatchTaskExecutor.KEY,
		"dispatch.task.executor.type=" + AnalyticsUploadDXPEntityDispatchTaskExecutor.KEY
	},
	service = DispatchTaskExecutor.class
)
public class AnalyticsUploadDXPEntityDispatchTaskExecutor
	extends BaseDispatchTaskExecutor {

	public static final String KEY = "upload-analytics-dxp-entities";

	@Override
	public void doExecute(
			DispatchTrigger dispatchTrigger,
			DispatchTaskExecutorOutput dispatchTaskExecutorOutput)
		throws IOException, PortalException {

		DispatchLog dispatchLog =
			_dispatchLogLocalService.fetchLatestDispatchLog(
				dispatchTrigger.getDispatchTriggerId(),
				DispatchTaskStatus.IN_PROGRESS);

		DispatchLog latestSuccessfulDispatchLog =
			_dispatchLogLocalService.fetchLatestDispatchLog(
				dispatchTrigger.getDispatchTriggerId(),
				DispatchTaskStatus.SUCCESSFUL);

		Date resourceLastModifiedDate = null;

		if (latestSuccessfulDispatchLog != null) {
			resourceLastModifiedDate = latestSuccessfulDispatchLog.getEndDate();
		}

		try {
			for (String batchEngineExportTaskItemDelegateName :
					_BATCH_ENGINE_EXPORT_TASK_ITEM_DELEGATE_NAMES) {

				_analyticsBatchExportImportManager.exportToAnalyticsCloud(
					batchEngineExportTaskItemDelegateName,
					dispatchTrigger.getCompanyId(), null,
					message -> _updateDispatchLog(
						dispatchLog.getDispatchLogId(),
						dispatchTaskExecutorOutput, message),
					resourceLastModifiedDate, DXPEntity.class.getName(),
					dispatchTrigger.getUserId());
			}
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}
	}

	@Override
	public String getName() {
		return KEY;
	}

	private void _updateDispatchLog(
			long dispatchLogId,
			DispatchTaskExecutorOutput dispatchTaskExecutorOutput,
			String message)
		throws PortalException {

		StringBundler sb = new StringBundler(5);

		if (dispatchTaskExecutorOutput.getOutput() != null) {
			sb.append(dispatchTaskExecutorOutput.getOutput());
		}

		sb.append(_dateFormat.format(new Date()));
		sb.append(StringPool.SPACE);
		sb.append(message);
		sb.append(StringPool.NEW_LINE);

		dispatchTaskExecutorOutput.setOutput(sb.toString());

		_dispatchLogLocalService.updateDispatchLog(
			dispatchLogId, new Date(), dispatchTaskExecutorOutput.getError(),
			dispatchTaskExecutorOutput.getOutput(),
			DispatchTaskStatus.IN_PROGRESS);
	}

	private static final String[]
		_BATCH_ENGINE_EXPORT_TASK_ITEM_DELEGATE_NAMES = {
			"group-dxp-entities", "organization-dxp-entities",
			"role-dxp-entities", "team-dxp-entities", "user-dxp-entities",
			"user-group-dxp-entities"
		};

	private static final DateFormat _dateFormat = new SimpleDateFormat(
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Reference
	private AnalyticsBatchExportImportManager
		_analyticsBatchExportImportManager;

	@Reference
	private DispatchLogLocalService _dispatchLogLocalService;

}