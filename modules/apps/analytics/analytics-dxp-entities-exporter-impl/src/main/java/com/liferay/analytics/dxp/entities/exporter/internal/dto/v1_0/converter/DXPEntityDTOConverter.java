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

package com.liferay.analytics.dxp.entities.exporter.internal.dto.v1_0.converter;

import com.liferay.analytics.dxp.entities.exporter.dto.v1_0.DXPEntity;
import com.liferay.analytics.dxp.entities.exporter.dto.v1_0.ExpandoField;
import com.liferay.analytics.dxp.entities.exporter.dto.v1_0.Field;
import com.liferay.analytics.message.sender.util.AnalyticsExpandoBridgeUtil;
import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.analytics.settings.configuration.AnalyticsConfigurationTracker;
import com.liferay.analytics.settings.security.constants.AnalyticsSecurityConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.ShardedModel;
import com.liferay.portal.kernel.model.Team;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rachael Koestartyo
 */
@Component(
	property = "dto.class.name=com.liferay.analytics.dxp.entities.exporter.dto.v1_0.DXPEntity",
	service = {DTOConverter.class, DXPEntityDTOConverter.class}
)
public class DXPEntityDTOConverter
	implements DTOConverter<BaseModel<?>, DXPEntity> {

	@Override
	public String getContentType() {
		return DXPEntity.class.getSimpleName();
	}

	@Override
	public DXPEntity toDTO(
			DTOConverterContext dtoConverterContext, BaseModel<?> baseModel)
		throws Exception {

		if (!_analyticsConfigurationTracker.isActive() || (baseModel == null) ||
			_isExcluded(baseModel)) {

			return null;
		}

		if (StringUtil.equals(
				baseModel.getModelClassName(), Contact.class.getName())) {

			Contact contact = (Contact)baseModel;

			User user = _userLocalService.fetchUser(contact.getClassPK());

			if (_isExcluded(user)) {
				return null;
			}

			return _toDXPEntity(
				null, _toFields(baseModel),
				String.valueOf(contact.getContactId()),
				Contact.class.getName());
		}
		else if (StringUtil.equals(
					baseModel.getModelClassName(), Group.class.getName())) {

			Group group = (Group)baseModel;

			if (_isExcluded(group)) {
				return null;
			}

			return _toDXPEntity(
				null, _toFields(baseModel), String.valueOf(group.getGroupId()),
				Group.class.getName());
		}
		else if (StringUtil.equals(
					baseModel.getModelClassName(),
					Organization.class.getName())) {

			Organization organization = (Organization)baseModel;

			return _toDXPEntity(
				_toExpandoFields(baseModel), _toFields(baseModel),
				String.valueOf(organization.getOrganizationId()),
				Organization.class.getName());
		}
		else if (StringUtil.equals(
					baseModel.getModelClassName(), Role.class.getName())) {

			Role role = (Role)baseModel;

			if (_isExcluded(role)) {
				return null;
			}

			return _toDXPEntity(
				null, _toFields(baseModel), String.valueOf(role.getRoleId()),
				Role.class.getName());
		}
		else if (StringUtil.equals(
					baseModel.getModelClassName(), Team.class.getName())) {

			Team team = (Team)baseModel;

			return _toDXPEntity(
				null, _toFields(baseModel), String.valueOf(team.getTeamId()),
				Team.class.getName());
		}
		else if (StringUtil.equals(
					baseModel.getModelClassName(), User.class.getName())) {

			User user = (User)baseModel;

			if (_isExcluded(user)) {
				return null;
			}

			return _toDXPEntity(
				_toExpandoFields(baseModel), _toFields(baseModel),
				String.valueOf(user.getUserId()), User.class.getName());
		}
		else if (StringUtil.equals(
					baseModel.getModelClassName(), UserGroup.class.getName())) {

			UserGroup userGroup = (UserGroup)baseModel;

			return _toDXPEntity(
				null, _toFields(baseModel),
				String.valueOf(userGroup.getUserGroupId()),
				UserGroup.class.getName());
		}

		return null;
	}

	private boolean _isExcluded(BaseModel<?> baseModel) {
		ShardedModel shardedModel = (ShardedModel)baseModel;

		Dictionary<String, Object> analyticsConfigurationProperties =
			_analyticsConfigurationTracker.getAnalyticsConfigurationProperties(
				shardedModel.getCompanyId());

		if (analyticsConfigurationProperties == null) {
			return true;
		}

		return false;
	}

	private boolean _isExcluded(Group group) {
		if (!group.isSite()) {
			return true;
		}

		return false;
	}

	private boolean _isExcluded(Role role) {
		if (role.getType() == RoleConstants.TYPE_REGULAR) {
			return false;
		}

		return true;
	}

	private boolean _isExcluded(User user) {
		if ((user == null) ||
			Objects.equals(
				user.getScreenName(),
				AnalyticsSecurityConstants.SCREEN_NAME_ANALYTICS_ADMIN) ||
			Objects.equals(
				user.getStatus(), WorkflowConstants.STATUS_INACTIVE)) {

			return true;
		}

		AnalyticsConfiguration analyticsConfiguration =
			_analyticsConfigurationTracker.getAnalyticsConfiguration(
				user.getCompanyId());

		if (analyticsConfiguration.syncAllContacts()) {
			return false;
		}

		long[] organizationIds = null;

		try {
			organizationIds = user.getOrganizationIds();
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return true;
		}

		for (long organizationId : organizationIds) {
			if (ArrayUtil.contains(
					analyticsConfiguration.syncedOrganizationIds(),
					String.valueOf(organizationId))) {

				return false;
			}
		}

		for (long userGroupId : user.getUserGroupIds()) {
			if (ArrayUtil.contains(
					analyticsConfiguration.syncedUserGroupIds(),
					String.valueOf(userGroupId))) {

				return false;
			}
		}

		return true;
	}

	private DXPEntity _toDXPEntity(
		ExpandoField[] expandoFields, Field[] fields, String id, String type) {

		DXPEntity dxpEntity = new DXPEntity();

		if (ArrayUtil.isNotEmpty(expandoFields)) {
			dxpEntity.setExpandoFields(expandoFields);
		}

		if (ArrayUtil.isNotEmpty(fields)) {
			dxpEntity.setFields(fields);
		}

		dxpEntity.setId(id);
		dxpEntity.setType(type);

		return dxpEntity;
	}

	private ExpandoField[] _toExpandoFields(BaseModel<?> baseModel) {
		List<String> includeAttributeNames = new ArrayList<>();

		if (StringUtil.equals(
				baseModel.getModelClassName(), User.class.getName())) {

			ShardedModel shardedModel = (ShardedModel)baseModel;

			AnalyticsConfiguration analyticsConfiguration =
				_analyticsConfigurationTracker.getAnalyticsConfiguration(
					shardedModel.getCompanyId());

			includeAttributeNames = ListUtil.fromArray(
				analyticsConfiguration.syncedUserFieldNames());
		}

		Map<String, Serializable> attributes =
			AnalyticsExpandoBridgeUtil.getAttributes(
				baseModel.getExpandoBridge(), includeAttributeNames);

		ExpandoField[] attributeFields = new ExpandoField[attributes.size()];

		for (Map.Entry<String, Serializable> entry : attributes.entrySet()) {
			String key = entry.getKey();

			ExpandoField expandoField = new ExpandoField() {
				{
					fieldType = key.substring(key.indexOf("-") + 1);
					name = key;
					value = String.valueOf(entry.getValue());
				}
			};

			attributeFields = ArrayUtil.append(attributeFields, expandoField);
		}

		return attributeFields;
	}

	private Field[] _toFields(BaseModel<?> baseModel) {
		Map<String, Object> modelAttributes = baseModel.getModelAttributes();

		Field[] attributeFields = new Field[modelAttributes.size()];

		for (Map.Entry<String, Object> entry : modelAttributes.entrySet()) {
			Field field = new Field() {
				{
					name = entry.getKey();
					value = entry.getValue();
				}
			};

			attributeFields = ArrayUtil.append(attributeFields, field);
		}

		return attributeFields;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DXPEntityDTOConverter.class);

	@Reference
	private AnalyticsConfigurationTracker _analyticsConfigurationTracker;

	@Reference
	private UserLocalService _userLocalService;

}