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

package com.liferay.dynamic.data.mapping.internal.upgrade.v3_7_1;

import com.liferay.dynamic.data.mapping.constants.DDMFormInstanceReportConstants;
import com.liferay.dynamic.data.mapping.exception.NoSuchFormInstanceReportException;
import com.liferay.dynamic.data.mapping.internal.report.CheckboxMultipleDDMFormFieldTypeReportProcessor;
import com.liferay.dynamic.data.mapping.internal.report.RadioDDMFormFieldTypeReportProcessor;
import com.liferay.dynamic.data.mapping.internal.report.TextDDMFormFieldTypeReportProcessor;
import com.liferay.dynamic.data.mapping.model.DDMContent;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecordVersion;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceReport;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceVersion;
import com.liferay.dynamic.data.mapping.model.DDMStructureVersion;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.report.DDMFormFieldTypeReportProcessor;
import com.liferay.dynamic.data.mapping.service.DDMContentLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordVersionLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceReportLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceVersionLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureVersionLocalService;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcos Martins
 */
public class UpgradeDDMFormInstanceReport extends UpgradeProcess {

	public UpgradeDDMFormInstanceReport(
		DDMContentLocalService ddmContentLocalService,
		DDMFormInstanceLocalService ddmFormInstanceLocalService,
		DDMFormInstanceRecordLocalService ddmFormInstanceRecordLocalService,
		DDMFormInstanceRecordVersionLocalService
			ddmFormInstanceRecordVersionLocalService,
		DDMFormInstanceReportLocalService ddmFormInstanceReportLocalService,
		DDMFormInstanceVersionLocalService ddmFormInstanceVersionLocalService,
		DDMStructureVersionLocalService ddmStructureVersionLocalService) {

		_ddmContentLocalService = ddmContentLocalService;
		_ddmFormInstanceLocalService = ddmFormInstanceLocalService;
		_ddmInstanceRecordLocalService = ddmFormInstanceRecordLocalService;
		_ddmInstanceRecordVersionLocalService =
			ddmFormInstanceRecordVersionLocalService;
		_ddmInstanceReportLocalService = ddmFormInstanceReportLocalService;
		_ddmFormInstanceVersionLocalService =
			ddmFormInstanceVersionLocalService;
		_ddmStructureVersionLocalService = ddmStructureVersionLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		List<DDMFormInstance> ddmFormInstances =
			_ddmFormInstanceLocalService.getDDMFormInstances(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (DDMFormInstance ddmFormInstance : ddmFormInstances) {
			DDMFormInstanceReport formInstanceReport = null;

			boolean newReport = false;

			try {
				formInstanceReport =
					_ddmInstanceReportLocalService.
						getFormInstanceReportByFormInstanceId(
							ddmFormInstance.getFormInstanceId());
			}
			catch (NoSuchFormInstanceReportException
						noSuchFormInstanceReportException) {

				formInstanceReport =
					_ddmInstanceReportLocalService.addFormInstanceReport(
						ddmFormInstance.getFormInstanceId());

				newReport = true;
			}

			JSONObject formInstanceReportDataJSONObject =
				JSONFactoryUtil.createJSONObject(formInstanceReport.getData());

			List<String> fieldsToIgnore = _getFieldsToIgnore(
				formInstanceReportDataJSONObject);

			List<DDMFormInstanceRecord> formInstanceRecords =
				_ddmInstanceRecordLocalService.getFormInstanceRecords(
					ddmFormInstance.getFormInstanceId());

			for (DDMFormInstanceRecord formInstanceRecord :
					formInstanceRecords) {

				DDMFormInstanceRecordVersion formInstanceRecordVersion =
					_ddmInstanceRecordVersionLocalService.
						getLatestFormInstanceRecordVersion(
							formInstanceRecord.getFormInstanceRecordId(),
							WorkflowConstants.STATUS_APPROVED);

				formInstanceReportDataJSONObject = _processFormValues(
					_getDDMFormValues(
						fieldsToIgnore, formInstanceRecordVersion),
					formInstanceRecord.getFormInstanceRecordId(),
					formInstanceReportDataJSONObject);

				if (newReport) {
					formInstanceReportDataJSONObject.put(
						"totalItems",
						formInstanceReportDataJSONObject.getInt("totalItems") +
							1);
				}
			}

			formInstanceReport.setData(
				formInstanceReportDataJSONObject.toJSONString());

			_ddmInstanceReportLocalService.updateDDMFormInstanceReport(
				formInstanceReport);
		}
	}

	private DDMFormValues _getDDMFormValues(
			List<String> fieldsToIgnore,
			DDMFormInstanceRecordVersion formInstanceRecordVersion)
		throws Exception {

		DDMFormInstanceVersion ddmFormInstanceVersion =
			_ddmFormInstanceVersionLocalService.getFormInstanceVersion(
				formInstanceRecordVersion.getFormInstanceId(),
				formInstanceRecordVersion.getFormInstanceVersion());

		DDMStructureVersion ddmStructureVersion =
			_ddmStructureVersionLocalService.getDDMStructureVersion(
				ddmFormInstanceVersion.getStructureVersionId());

		DDMForm ddmForm =
			_ddmStructureVersionLocalService.getStructureVersionDDMForm(
				ddmStructureVersion);

		DDMFormValues ddmFormValues = new DDMFormValues(ddmForm);

		List<DDMFormFieldValue> ddmFormFieldValues = new ArrayList<>();

		DDMContent ddmContent = _ddmContentLocalService.getDDMContent(
			formInstanceRecordVersion.getStorageId());

		JSONObject dataJSONObject = JSONFactoryUtil.createJSONObject(
			ddmContent.getData());

		JSONArray fieldValuesJSONArray = dataJSONObject.getJSONArray(
			"fieldValues");

		Iterator<JSONObject> iterator = fieldValuesJSONArray.iterator();

		while (iterator.hasNext()) {
			JSONObject jsonObject = iterator.next();

			String name = jsonObject.getString("name");

			if (fieldsToIgnore.contains(name)) {
				continue;
			}

			DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();

			ddmFormFieldValue.setDDMFormValues(ddmFormValues);
			ddmFormFieldValue.setInstanceId(jsonObject.getString("instanceId"));
			ddmFormFieldValue.setName(name);

			Value value = new LocalizedValue();

			JSONObject valueJSONObject = jsonObject.getJSONObject("value");

			valueJSONObject.keySet(
			).forEach(
				key -> value.addString(
					LocaleUtil.fromLanguageId(key),
					valueJSONObject.getString(key))
			);

			ddmFormFieldValue.setValue(value);

			ddmFormFieldValues.add(ddmFormFieldValue);
		}

		ddmFormValues.setDDMFormFieldValues(ddmFormFieldValues);

		return ddmFormValues;
	}

	private List<String> _getFieldsToIgnore(
		JSONObject formInstanceReportDataJSONObject) {

		Set<String> keySet = formInstanceReportDataJSONObject.keySet();

		Stream<String> stream = keySet.stream();

		return stream.filter(
			key -> !StringUtil.equals(key, "totalItems")
		).collect(
			Collectors.toList()
		);
	}

	private JSONObject _processFormValues(
			DDMFormValues ddmFormValues, long formInstanceRecordId,
			JSONObject formInstanceReportDataJSONObject)
		throws Exception, JSONException {

		for (DDMFormFieldValue ddmFormFieldValue :
				ddmFormValues.getDDMFormFieldValues()) {

			DDMFormFieldTypeReportProcessor ddmFormFieldTypeReportProcessor =
				_ddmFormFieldTypeReportProcessorTracker.
					getDDMFormFieldTypeReportProcessor(
						ddmFormFieldValue.getType());

			if (ddmFormFieldTypeReportProcessor != null) {
				String fieldName = ddmFormFieldValue.getName();

				JSONObject fieldJSONObject =
					formInstanceReportDataJSONObject.getJSONObject(fieldName);

				if (fieldJSONObject == null) {
					fieldJSONObject = JSONUtil.put(
						"type", ddmFormFieldValue.getType()
					).put(
						"values", JSONFactoryUtil.createJSONObject()
					);
				}

				JSONObject processedFieldJSONObject =
					ddmFormFieldTypeReportProcessor.process(
						ddmFormFieldValue,
						JSONFactoryUtil.createJSONObject(
							fieldJSONObject.toJSONString()),
						formInstanceRecordId,
						DDMFormInstanceReportConstants.
							EVENT_ADD_RECORD_VERSION);

				formInstanceReportDataJSONObject.put(
					fieldName, processedFieldJSONObject);
			}
		}

		return formInstanceReportDataJSONObject;
	}

	private final DDMContentLocalService _ddmContentLocalService;
	private DDMFormFieldTypeReportProcessorTracker
		_ddmFormFieldTypeReportProcessorTracker =
			new DDMFormFieldTypeReportProcessorTracker();
	private final DDMFormInstanceLocalService _ddmFormInstanceLocalService;
	private final DDMFormInstanceVersionLocalService
		_ddmFormInstanceVersionLocalService;
	private final DDMFormInstanceRecordLocalService
		_ddmInstanceRecordLocalService;
	private final DDMFormInstanceRecordVersionLocalService
		_ddmInstanceRecordVersionLocalService;
	private final DDMFormInstanceReportLocalService
		_ddmInstanceReportLocalService;
	private final DDMStructureVersionLocalService
		_ddmStructureVersionLocalService;

	private class DDMFormFieldTypeReportProcessorTracker {

		public DDMFormFieldTypeReportProcessor
			getDDMFormFieldTypeReportProcessor(String type) {

			if (StringUtil.equals(type, "checkbox_multiple") ||
				StringUtil.equals(type, "select")) {

				return new CheckboxMultipleDDMFormFieldTypeReportProcessor();
			}
			else if (StringUtil.equals(type, "radio")) {
				return new RadioDDMFormFieldTypeReportProcessor();
			}
			else if (StringUtil.equals(type, "text")) {
				TextDDMFormFieldTypeReportProcessor
					textDDMFormFieldTypeReportProcessor =
						new TextDDMFormFieldTypeReportProcessor();

				textDDMFormFieldTypeReportProcessor.
					setDDMFormInstanceRecordLocalService(
						_ddmInstanceRecordLocalService);

				return textDDMFormFieldTypeReportProcessor;
			}

			return null;
		}

	}

}