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

package com.liferay.jenkins.results.parser;

/**
 * @author Michael Hashimoto
 */
public class PortalEnvironmentBuild extends PortalTopLevelBuild {

	public PortalEnvironmentBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);
	}

	@Override
	public String getBaseGitRepositoryName() {
		String branchName = getBranchName();

		if (branchName.equals("master")) {
			return "liferay-portal";
		}

		return "liferay-portal-ee";
	}

	@Override
	public String getBranchName() {
		String jobName = getJobName();

		return jobName.substring(
			jobName.indexOf("(") + 1, jobName.indexOf(")"));
	}

	@Override
	public Job.BuildProfile getBuildProfile() {
		String branchName = getBranchName();

		if (branchName.startsWith("ee-")) {
			return Job.BuildProfile.PORTAL;
		}

		return Job.BuildProfile.DXP;
	}

}