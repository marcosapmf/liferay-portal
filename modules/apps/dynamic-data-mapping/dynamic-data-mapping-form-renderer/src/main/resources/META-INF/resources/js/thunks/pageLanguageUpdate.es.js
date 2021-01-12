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

import {PagesVisitor} from 'dynamic-data-mapping-form-renderer';
import {fetch} from 'frontend-js-web';

import {EVENT_TYPES} from '../actions/eventTypes.es';

const formatDataRecord = (languageId, pages, preserveValue) => {
	const dataRecordValues = {};

	const visitor = new PagesVisitor(pages);

	const setDataRecord = (
		dataRecordValues,
		{
			fieldName,
			instanceId,
			localizable,
			localizedValue,
			nestedFields,
			type,
			value,
			visible,
		}
	) => {
		if (type === 'fieldset') {
			const fieldsetDataRecordValues = {
				instanceId,
				nestedFields: {},
			};

			if (!dataRecordValues[fieldName]) {
				dataRecordValues[fieldName] = [];
			}

			dataRecordValues[fieldName].push(fieldsetDataRecordValues);

			Object.keys(nestedFields).forEach((key) => {
				setDataRecord(
					fieldsetDataRecordValues.nestedFields,
					nestedFields[key]
				);
			});

			return;
		}

		let _value = '';

		if (visible) {
			if (preserveValue) {
				_value = value;
			}
			else if (value) {
				try {
					_value = JSON.parse(value);
				}
				catch (e) {
					_value = value;
				}
			}
		}

		if (localizable) {
			const fieldDataRecordValues = {
				instanceId,
				localizedValue: {
					...localizedValue,
					[languageId]: _value,
				},
			};

			if (!dataRecordValues[fieldName]) {
				dataRecordValues[fieldName] = [];
			}

			dataRecordValues[fieldName].push(fieldDataRecordValues);
		}
		else {
			dataRecordValues[fieldName] = _value;
		}
	};

	visitor.mapFields((field) => {
		setDataRecord(dataRecordValues, field);
	});

	return dataRecordValues;
};

const getDataRecordValues = ({
	nextEditingLanguageId,
	pages,
	preserveValue,
	prevEditingLanguageId,
}) => {
	if (preserveValue) {
		return formatDataRecord(nextEditingLanguageId, pages, true);
	}

	return formatDataRecord(prevEditingLanguageId, pages);
};

const getField = (fieldName, instanceId, pages) => {
	const visitor = new PagesVisitor(pages);

	return visitor.findField((field) => {
		return field.fieldName === fieldName && field.instanceId === instanceId;
	});
};

export default function pageLanguageUpdate({
	ddmStructureLayoutId,
	nextEditingLanguageId,
	pages,
	portletNamespace,
	preserveValue,
	prevEditingLanguageId,
	readOnly,
}) {
	return (dispatch) => {
		const newDataRecordValues = getDataRecordValues({
			nextEditingLanguageId,
			pages,
			preserveValue,
			prevEditingLanguageId,
		});

		fetch(
			`/o/data-engine/v2.0/data-layouts/${ddmStructureLayoutId}/context`,
			{
				body: JSON.stringify({
					dataRecordValues: newDataRecordValues,
					namespace: portletNamespace,
					pathThemeImages: themeDisplay.getPathThemeImages(),
					readOnly,
					scopeGroupId: themeDisplay.getScopeGroupId(),
					siteGroupId: themeDisplay.getSiteGroupId(),
				}),
				headers: {
					'Accept-Language': nextEditingLanguageId.replace('_', '-'),
					'Content-Type': 'application/json',
				},
				method: 'POST',
			}
		)
			.then((response) => response.json())
			.then((response) => {
				const updateField = (fieldDataRecordValues, fieldName) => {
					fieldDataRecordValues.forEach((fieldDataRecordValue) => {
						const field = getField(
							fieldName,
							fieldDataRecordValue.instanceId,
							response.pages
						);

						if (field.type === 'fieldset') {
							Object.keys(
								fieldDataRecordValue.nestedFields
							).forEach((fieldName) => {
								updateField(
									fieldDataRecordValue.nestedFields[
										fieldName
									],
									fieldName
								);
							});
						}
						else {
							if (fieldDataRecordValue) {
								field.localizedValue =
									fieldDataRecordValue.localizedValue;
							}
							else if (!field.localizedValue) {
								field.localizedValue = {};
							}
						}
					});
				};

				Object.keys(newDataRecordValues).forEach((fieldName) => {
					updateField(newDataRecordValues[fieldName], fieldName);
				});

				dispatch({
					payload: {
						editingLanguageId: nextEditingLanguageId,
						pages: response.pages,
					},
					type: EVENT_TYPES.ALL,
				});

				dispatch({
					payload: newDataRecordValues,
					type: EVENT_TYPES.UPDATE_DATA_RECORD_VALUES,
				});
			});
	};
}
