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

package com.liferay.dynamic.data.mapping.upgrade.v3_7_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceReport;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceReportLocalServiceUtil;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.test.util.DDMFormInstanceTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMFormValuesTestUtil;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marcos Martins
 */
@RunWith(Arquillian.class)
public class UpgradeDDMFormInstanceReportTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_jsonFactory = new JSONFactoryImpl();

		setUpUpgradeDDMFormInstanceReport();
	}

	@Test
	public void testUpgradeWhenThereIsFormInstanceReport() throws Exception {
		DDMFormInstance formInstance =
			DDMFormInstanceTestUtil.addDDMFormInstance(
				_group, TestPropsValues.getUserId());

		createFormInstanceRecords(formInstance);

		DDMFormInstanceReport formInstanceReport =
			DDMFormInstanceReportLocalServiceUtil.
				getFormInstanceReportByFormInstanceId(
					formInstance.getFormInstanceId());

		JSONObject expectedDataJSONObject = _jsonFactory.createJSONObject(
			formInstanceReport.getData());

		_upgradeDDMFormInstanceReport.upgrade();

		formInstanceReport =
			DDMFormInstanceReportLocalServiceUtil.
				getFormInstanceReportByFormInstanceId(
					formInstance.getFormInstanceId());

		JSONObject actualDataJSONObject = _jsonFactory.createJSONObject(
			formInstanceReport.getData());

		assertData(actualDataJSONObject, expectedDataJSONObject);
	}

	@Test
	public void testUpgradeWhenThereIsNoFormInstanceReport() throws Exception {
		DDMFormInstance formInstance =
			DDMFormInstanceTestUtil.addDDMFormInstance(
				_group, TestPropsValues.getUserId());

		createFormInstanceRecords(formInstance);

		DDMFormInstanceReport formInstanceReport =
			DDMFormInstanceReportLocalServiceUtil.
				getFormInstanceReportByFormInstanceId(
					formInstance.getFormInstanceId());

		JSONObject expectedDataJSONObject = _jsonFactory.createJSONObject(
			formInstanceReport.getData());

		DDMFormInstanceReportLocalServiceUtil.deleteDDMFormInstanceReport(
			formInstanceReport);

		Assert.assertNull(
			DDMFormInstanceReportLocalServiceUtil.fetchDDMFormInstanceReport(
				formInstanceReport.getFormInstanceReportId()));

		_upgradeDDMFormInstanceReport.upgrade();

		formInstanceReport =
			DDMFormInstanceReportLocalServiceUtil.
				getFormInstanceReportByFormInstanceId(
					formInstance.getFormInstanceId());

		JSONObject actualDataJSONObject = _jsonFactory.createJSONObject(
			formInstanceReport.getData());

		assertData(actualDataJSONObject, expectedDataJSONObject);
	}

	protected void assertData(
		JSONObject actualDataJSONObject, JSONObject expectedDataJSONObject) {

		Assert.assertEquals(
			expectedDataJSONObject.getInt("totalItems"),
			actualDataJSONObject.getInt("totalItems"));

		JSONObject expectedFieldValuesJSONObject =
			expectedDataJSONObject.getJSONObject("text");
		JSONObject actualFieldValuesJSONObject =
			actualDataJSONObject.getJSONObject("text");

		JSONArray expectedValuesJSONArray =
			expectedFieldValuesJSONObject.getJSONArray("values");
		JSONArray actualValuesJSONArray =
			actualFieldValuesJSONObject.getJSONArray("values");

		Assert.assertEquals(
			expectedValuesJSONArray.length(), actualValuesJSONArray.length());

		int index = 0;

		while (index < expectedValuesJSONArray.length()) {
			JSONObject expectedValueJSONObject =
				expectedValuesJSONArray.getJSONObject(index);

			JSONObject actualValueJSONObject =
				actualValuesJSONArray.getJSONObject(index);

			Assert.assertEquals(
				expectedValueJSONObject.getLong("formInstanceRecordId"),
				actualValueJSONObject.getLong("formInstanceRecordId"));

			Assert.assertEquals(
				expectedValueJSONObject.getString("value"),
				actualValueJSONObject.getString("value"));

			index++;
		}
	}

	protected void createFormInstanceRecords(DDMFormInstance formInstance)
		throws PortalException {

		DDMForm ddmForm = DDMFormTestUtil.createDDMForm("text");

		int qtd = 0;

		while (qtd < _FORM_INSTANCE_RECORD_MAX_QTD) {
			DDMFormValues ddmFormValues =
				DDMFormValuesTestUtil.createDDMFormValues(ddmForm);

			Value value = new LocalizedValue();

			value.addString(LocaleUtil.US, "value " + qtd);

			DDMFormFieldValue ddmFormFieldValue =
				DDMFormValuesTestUtil.createDDMFormFieldValue("text", value);

			ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);

			DDMFormInstanceRecordLocalServiceUtil.addFormInstanceRecord(
				TestPropsValues.getUserId(), _group.getGroupId(),
				formInstance.getFormInstanceId(), ddmFormValues,
				getServiceContext());

			qtd++;
		}
	}

	protected ServiceContext getServiceContext() throws PortalException {
		return ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());
	}

	protected void setUpUpgradeDDMFormInstanceReport() {
		_upgradeStepRegistrator.register(
			new UpgradeStepRegistrator.Registry() {

				@Override
				public void register(
					String fromSchemaVersionString,
					String toSchemaVersionString, UpgradeStep... upgradeSteps) {

					for (UpgradeStep upgradeStep : upgradeSteps) {
						Class<?> clazz = upgradeStep.getClass();

						String className = clazz.getName();

						if (className.contains(_CLASS_NAME)) {
							_upgradeDDMFormInstanceReport =
								(UpgradeProcess)upgradeStep;
						}
					}
				}

			});
	}

	private static final String _CLASS_NAME =
		"com.liferay.dynamic.data.mapping.internal.upgrade.v3_7_1." +
			"UpgradeDDMFormInstanceReport";

	private static final int _FORM_INSTANCE_RECORD_MAX_QTD = 5;

	@Inject(
		filter = "(&(objectClass=com.liferay.dynamic.data.mapping.internal.upgrade.DDMServiceUpgrade))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@DeleteAfterTestRun
	private Group _group;

	private JSONFactory _jsonFactory;
	private UpgradeProcess _upgradeDDMFormInstanceReport;

}