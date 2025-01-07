/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.internal.matcher;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.lang.reflect.Array;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author Marcos Martins
 */
public class UserSegmentsEntryMembershipChecker {

	public static boolean isMember(
			String filterString, Map<String, Object> userAttributes)
		throws Exception {

		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(
			"JavaScript");

		return (Boolean)scriptEngine.eval(_parse(filterString, userAttributes));
	}

	private static String _getFieldName(String key) {
		String fieldName = _fieldNames.get(key);

		if (fieldName != null) {
			return fieldName;
		}

		return key;
	}

	private static String _getFieldValue(
		String fieldName, Map<String, Object> userAttributes) {

		Object object = userAttributes.get(_getFieldName(fieldName));

		if (object == null) {
			return null;
		}

		if (object instanceof Date) {
			return _dateFormat.format((Date)object);
		}

		return String.valueOf(object);
	}

	private static String _parse(
		String filterString, Map<String, Object> userAttributes) {

		String parsedFilterString = _processFieldValues(filterString);

		parsedFilterString = _processOperators(parsedFilterString);

		parsedFilterString = _processFieldNames(
			parsedFilterString, userAttributes);

		parsedFilterString = _processContainsOperator(
			parsedFilterString, userAttributes);

		parsedFilterString = StringUtil.replace(
			parsedFilterString, "indexOf _eq_ ", "indexOf");

		return StringUtil.replace(
			parsedFilterString, "_and_", "&&"
		).replace(
			"_eq_", "=="
		).replace(
			"_ge_", ">="
		).replace(
			"_gt_", ">"
		).replace(
			"_lt_", "<"
		).replace(
			"_not_", "!"
		).replace(
			"_or_", "||"
		).replace(
			"indexOf _contains_ ", "indexOf"
		).replace(
			"_le_", "<="
		);
	}

	private static String _processContainsOperator(
		String filterString, Map<String, Object> userAttributes) {

		StringBuffer sb = new StringBuffer();

		Matcher matcher = _fieldNameContainsPattern.matcher(filterString);

		while (matcher.find()) {
			String group = matcher.group();

			if (Validator.isBlank(group)) {
				continue;
			}

			String fieldValue = _getFieldValue(group, userAttributes);

			if (fieldValue == null) {
				continue;
			}

			matcher.appendReplacement(
				sb,
				String.format(
					"%d < '%s'.indexOf", _NOT_FOUND_INDEX, fieldValue));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static String _processFieldNames(
		String filterString, Map<String, Object> userAttributes) {

		StringBuffer sb = new StringBuffer();

		Matcher matcher = _fieldNamePattern.matcher(filterString);

		while (matcher.find()) {
			String group = matcher.group();

			if (Validator.isBlank(group)) {
				continue;
			}

			String fieldValue = _getFieldValue(group, userAttributes);

			if (fieldValue == null) {
				continue;
			}

			String replacement = null;

			Object object = userAttributes.get(_getFieldName(group));

			Class<?> clazz = object.getClass();

			if (clazz.isArray()) {
				replacement = String.format(
					"%d < [%s].indexOf", _NOT_FOUND_INDEX,
					StringUtil.merge(
						TransformUtil.unsafeTransform(
							_toArray(object),
							item -> StringUtil.quote(String.valueOf(item)),
							String.class)));
			}
			else {
				replacement = String.format("'%s'", fieldValue);
			}

			matcher.appendReplacement(sb, replacement);
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static String _processFieldValues(String filterString) {
		StringBuffer sb = new StringBuffer();

		Matcher matcher = _fieldValuePattern.matcher(filterString);

		while (matcher.find()) {
			matcher.appendReplacement(sb, "(" + matcher.group() + ")");
		}

		matcher.appendTail(sb);

		matcher = _datePattern.matcher(sb.toString());

		sb = new StringBuffer();

		while (matcher.find()) {
			String group = matcher.group();

			matcher.appendReplacement(sb, String.format("'%s'", group));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private static String _processOperators(String filterString) {
		StringBuffer sb = new StringBuffer();

		String tempFilterString = filterString;

		int openQuoteIndex = tempFilterString.indexOf("'");

		while (true) {
			String input = null;

			if (openQuoteIndex == _NOT_FOUND_INDEX) {
				input = tempFilterString;
			}
			else {
				input = tempFilterString.substring(0, openQuoteIndex);
			}

			Matcher matcher = _operatorPattern.matcher(input);

			while (matcher.find()) {
				matcher.appendReplacement(
					sb, " _" + StringUtil.trim(matcher.group()) + "_ ");
			}

			matcher.appendTail(sb);

			if (openQuoteIndex == _NOT_FOUND_INDEX) {
				return sb.toString();
			}

			String tailTempFilterString = tempFilterString.substring(
				openQuoteIndex + 1);

			int closeQuoteIndex = tailTempFilterString.indexOf("'");

			if (closeQuoteIndex == _NOT_FOUND_INDEX) {
				return sb.toString();
			}

			sb.append("'");
			sb.append(tailTempFilterString.substring(0, closeQuoteIndex));
			sb.append("'");

			tempFilterString = tailTempFilterString.substring(
				closeQuoteIndex + 1);

			openQuoteIndex = tempFilterString.indexOf("'");
		}
	}

	private static Object[] _toArray(Object object) {
		Class<?> clazz = object.getClass(
		).getComponentType();

		if (clazz.isPrimitive()) {
			List<Object> list = new ArrayList<>();

			for (int i = 0; i < Array.getLength(object); i++) {
				list.add(Array.get(object, i));
			}

			return list.toArray();
		}

		return (Object[])object;
	}

	private static final int _NOT_FOUND_INDEX = -1;

	private static final DateFormat _dateFormat = new SimpleDateFormat(
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static final Pattern _datePattern = Pattern.compile(
		"\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}.\\d{3}){0,1}((Z)|" +
			"((\\+|\\-)(\\d*))){0,1}");
	private static final Pattern _fieldNameContainsPattern = Pattern.compile(
		"\\w*(?= _contains_ )");
	private static final Pattern _fieldNamePattern = Pattern.compile(
		"\\w*(?= _(eq|ge|gt|le|lt)_ )");
	private static final Map<String, String> _fieldNames = HashMapBuilder.put(
		"dateModified", "modifiedDate"
	).build();
	private static final Pattern _fieldValuePattern = Pattern.compile(
		"(?<=(\\s+contains|eq|ge|gt|le|lt)\\s+)'[\\w]*'");
	private static final Pattern _operatorPattern = Pattern.compile(
		"\\s+(and|contains|eq|ge|gt|le|lt|or)\\s+|(not\\s+)");

}