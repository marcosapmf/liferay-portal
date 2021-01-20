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

package com.liferay.data.engine.rest.internal.strategy;

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldType;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Jeyvison Nascimento
 * @author Leonardo Barros
 */
public class DefaultMapToDDMFormValuesConverterStrategy
	implements MapToDDMFormValuesConverterStrategy {

	public static DefaultMapToDDMFormValuesConverterStrategy getInstance() {
		return _defaultMapToDDMFormValuesConverterStrategy;
	}

	@Override
	public void setDDMFormFieldValues(
		Map<String, Object> dataRecordValues, DDMForm ddmForm,
		DDMFormValues ddmFormValues, Locale locale) {

		Map<String, DDMFormField> ddmFormFields = ddmForm.getDDMFormFieldsMap(
			false);

		for (Map.Entry<String, DDMFormField> entry : ddmFormFields.entrySet()) {
			DDMFormField ddmFormField = entry.getValue();

			List<DDMFormFieldValue> ddmFormFieldValues =
				createDDMFormFieldValues(
					(List<Object>)dataRecordValues.get(ddmFormField.getName()),
					ddmFormField);

			ddmFormFieldValues.forEach(ddmFormValues::addDDMFormFieldValue);
		}
	}

	protected List<DDMFormFieldValue> createDDMFormFieldValues(
		List<Object> dataRecordValues, DDMFormField ddmFormField) {

		if (ListUtil.isEmpty(dataRecordValues)) {
			return ListUtil.fromArray(
				new DDMFormFieldValue() {
					{
						setName(ddmFormField.getName());

						if (!StringUtil.equals(
								ddmFormField.getType(),
								DDMFormFieldType.FIELDSET)) {

							setValue(ddmFormField.getPredefinedValue());
						}
					}
				});
		}

		List<DDMFormFieldValue> ddmFormFieldValues = new ArrayList<>(
			dataRecordValues.size());

		for (Object object : dataRecordValues) {
			DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();

			Map<String, Object> fieldDataRecordValues =
				(Map<String, Object>)object;

			ddmFormFieldValue.setInstanceId(
				GetterUtil.getString(fieldDataRecordValues.get("instanceId")));

			ddmFormFieldValue.setName(ddmFormField.getName());

			if (StringUtil.equals(
					ddmFormField.getType(), DDMFormFieldType.FIELDSET)) {

				List<DDMFormField> nestedDDMFormFields =
					ddmFormField.getNestedDDMFormFields();

				Map<String, Object> nestedDataRecordValues =
					(Map<String, Object>)fieldDataRecordValues.get(
						"nestedFields");

				nestedDDMFormFields.forEach(
					nestedDDMFormField -> {
						List<DDMFormFieldValue> nestedDDMFormFieldValues =
							createDDMFormFieldValues(
								(List<Object>)nestedDataRecordValues.get(
									nestedDDMFormField.getName()),
								nestedDDMFormField);

						nestedDDMFormFieldValues.forEach(
							ddmFormFieldValue::addNestedDDMFormFieldValue);
					});
			}
			else {
				Map<String, Object> localizedValues =
					(Map<String, Object>)fieldDataRecordValues.get(
						"localizedValue");

				LocalizedValue localizedValue = new LocalizedValue();

				for (Map.Entry<String, Object> key :
						localizedValues.entrySet()) {

					localizedValue.addString(
						LocaleUtil.fromLanguageId(key.getKey()),
						String.valueOf(localizedValues.get(key.getKey())));

					ddmFormFieldValue.setValue(localizedValue);
				}
			}

			ddmFormFieldValues.add(ddmFormFieldValue);
		}

		return ddmFormFieldValues;
	}

	private static final DefaultMapToDDMFormValuesConverterStrategy
		_defaultMapToDDMFormValuesConverterStrategy =
			new DefaultMapToDDMFormValuesConverterStrategy();

}