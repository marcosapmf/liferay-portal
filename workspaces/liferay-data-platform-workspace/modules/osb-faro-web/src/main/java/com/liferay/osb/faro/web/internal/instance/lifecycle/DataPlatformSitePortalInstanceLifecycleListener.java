/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.osb.faro.web.internal.instance.lifecycle;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.site.initializer.SiteInitializer;
import com.liferay.site.initializer.SiteInitializerRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcos Martins
 */
@Component(service = PortalInstanceLifecycleListener.class)
public class DataPlatformSitePortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		long companyId = company.getCompanyId();

		Group group = _groupLocalService.fetchGroupByExternalReferenceCode(
			_SITE_ERC, companyId);

		if (group != null) {
			return;
		}

		SiteInitializer siteInitializer =
			_siteInitializerRegistry.getSiteInitializer(_SITE_INITIALIZER_KEY);

		if (siteInitializer == null) {
			_log.warn(
				"Site initializer not found: " + _SITE_INITIALIZER_KEY +
					". Deploy liferay-data-platform-site-initializer first.");

			return;
		}

		long userId = _userLocalService.getDefaultUserId(companyId);

		User user = _userLocalService.getUser(userId);

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();
		String name = PrincipalThreadLocal.getName();

		try {
			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(user));
			PrincipalThreadLocal.setName(String.valueOf(userId));

			ServiceContext serviceContext = new ServiceContext();

			serviceContext.setCompanyId(companyId);
			serviceContext.setUserId(userId);

			ServiceContextThreadLocal.pushServiceContext(serviceContext);

			group = _groupLocalService.addGroup(
				_SITE_ERC, userId, GroupConstants.DEFAULT_PARENT_GROUP_ID,
				StringPool.NULL, 0, GroupConstants.DEFAULT_LIVE_GROUP_ID,
				HashMapBuilder.put(
					LocaleUtil.getDefault(), _SITE_NAME
				).build(),
				null, GroupConstants.TYPE_SITE_OPEN, null, true,
				GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION,
				"/liferay-data-platform", true, false, true, serviceContext);

			siteInitializer.initialize(group.getGroupId());
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);
		}
	}

	private static final String _SITE_ERC = "LIFERAY_DATA_PLATFORM";

	private static final String _SITE_INITIALIZER_KEY =
		"liferaydataplatformsiteinitializer";

	private static final String _SITE_NAME = "Liferay Data Platform";

	private static final Log _log = LogFactoryUtil.getLog(
		DataPlatformSitePortalInstanceLifecycleListener.class);

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private SiteInitializerRegistry _siteInitializerRegistry;

	@Reference
	private UserLocalService _userLocalService;

}
