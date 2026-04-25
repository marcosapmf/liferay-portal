/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.osb.faro.web.internal.instance.lifecycle;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.site.initializer.SiteInitializer;
import com.liferay.site.initializer.SiteInitializerFactory;

import java.io.File;

import java.net.URL;

import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * @author Marcos Martins
 */
@Component(
	immediate = true,
	service = PortalInstanceLifecycleListener.class
)
public class DataPlatformSitePortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_log.warn("DataPlatformSite: @Activate called");

		_bundleTracker = new BundleTracker<>(
			bundleContext, Bundle.ACTIVE,
			new BundleTrackerCustomizer<Bundle>() {

				@Override
				public Bundle addingBundle(
					Bundle bundle, BundleEvent bundleEvent) {

					_log.warn(
						"DataPlatformSite: addingBundle " +
							bundle.getSymbolicName());

					if (!_SITE_INITIALIZER_BSN.equals(
							bundle.getSymbolicName())) {

						return null;
					}

					_initializeSiteForAllCompanies(bundle);

					return bundle;
				}

				@Override
				public void modifiedBundle(
					Bundle bundle, BundleEvent bundleEvent, Bundle object) {
				}

				@Override
				public void removedBundle(
					Bundle bundle, BundleEvent bundleEvent, Bundle object) {
				}

			});

		_bundleTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_bundleTracker.close();
	}

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		Bundle[] bundles = _bundleTracker.getBundles();

		if ((bundles == null) || (bundles.length == 0)) {
			_log.warn(
				"Site initializer bundle not found: " +
					_SITE_INITIALIZER_BSN +
						". Deploy liferay-data-platform-site-initializer first.");

			return;
		}

		_initializeSiteForCompany(bundles[0], company);
	}

	private void _initializeSiteForAllCompanies(Bundle bundle) {
		try {
			List<Company> companies = _companyLocalService.getCompanies();

			for (Company company : companies) {
				_initializeSiteForCompany(bundle, company);
			}
		}
		catch (Exception exception) {
			_log.error(
				"Failed to initialize data platform site", exception);
		}
	}

	private void _initializeSiteForCompany(Bundle bundle, Company company)
		throws Exception {

		long companyId = company.getCompanyId();

		Group group = _groupLocalService.fetchGroupByExternalReferenceCode(
			_SITE_ERC, companyId);

		if (group != null) {
			Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
				group.getGroupId(), false, "/home");

			if (layout != null) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Data platform site already fully initialized for " +
							"company " + companyId + ", group " +
								group.getGroupId());
				}

				return;
			}

			if (_log.isInfoEnabled()) {
				_log.info(
					"Data platform site group exists but has no home page " +
						"for company " + companyId + ", group " +
							group.getGroupId() + ". Re-initializing.");
			}
		}
		else {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Creating data platform site for company " + companyId);
			}
		}

		long userId = _userLocalService.getDefaultUserId(companyId);

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(companyId)) {

			_initializeSiteInTransaction(bundle, companyId, group, userId);
		}
	}

	private void _initializeSiteInTransaction(
			Bundle bundle, long companyId, Group group, long userId)
		throws Exception {

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

			if (group == null) {
				group = _groupLocalService.addGroup(
					_SITE_ERC, userId,
					GroupConstants.DEFAULT_PARENT_GROUP_ID,
					StringPool.NULL, 0, GroupConstants.DEFAULT_LIVE_GROUP_ID,
					HashMapBuilder.put(
						LocaleUtil.getDefault(), _SITE_NAME
					).build(),
					null, GroupConstants.TYPE_SITE_OPEN, null, true,
					GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION,
					"/liferay-data-platform", true, false, true,
					serviceContext);
			}

			URL zipURL = bundle.getEntry(
				"site-initializer/site-initializer.zip");

			if (zipURL == null) {
				_log.warn("site-initializer.zip not found in bundle");

				return;
			}

			File tempFile = FileUtil.createTempFile(zipURL.openStream());
			File tempFolder = FileUtil.createTempFolder();

			try {
				FileUtil.unzip(tempFile, tempFolder);

				SiteInitializer siteInitializer =
					_siteInitializerFactory.create(
						new File(tempFolder, "site-initializer"), _SITE_NAME);

				siteInitializer.initialize(group.getGroupId());
			}
			finally {
				tempFile.delete();
				FileUtil.deltree(tempFolder);
			}
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);
		}
	}

	private static final String _SITE_ERC = "LIFERAY_DATA_PLATFORM";

	private static final String _SITE_INITIALIZER_BSN =
		"liferaydataplatformsiteinitializer";

	private static final String _SITE_NAME = "Liferay Data Platform";

	private static final Log _log = LogFactoryUtil.getLog(
		DataPlatformSitePortalInstanceLifecycleListener.class);

	private BundleTracker<Bundle> _bundleTracker;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private SiteInitializerFactory _siteInitializerFactory;

	@Reference
	private UserLocalService _userLocalService;

}
