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

package com.liferay.jenkins.results.parser.spira.result;

import com.liferay.jenkins.results.parser.Job;
import com.liferay.jenkins.results.parser.TopLevelBuild;
import com.liferay.jenkins.results.parser.spira.SpiraTestCaseRun;
import com.liferay.jenkins.results.parser.test.clazz.group.AxisTestClassGroup;

/**
 * @author Michael Hashimoto
 */
public class SFBatchAxisSpiraTestResult extends BatchAxisSpiraTestResult {

	@Override
	public SpiraTestCaseRun.Status getSpiraTestCaseRunStatus() {
		Job job = _axisTestClassGroup.getJob();

		String jobName = job.getJobName();

		if (jobName.equals("test-portal-source-format")) {
			TopLevelBuild topLevelBuild = spiraBuildResult.getTopLevelBuild();

			String result = topLevelBuild.getResult();

			if (!result.equals("SUCCESS")) {
				return SpiraTestCaseRun.Status.FAILED;
			}

			return SpiraTestCaseRun.Status.PASSED;
		}

		return super.getSpiraTestCaseRunStatus();
	}

	protected SFBatchAxisSpiraTestResult(
		SpiraBuildResult spiraBuildResult,
		AxisTestClassGroup axisTestClassGroup) {

		super(spiraBuildResult, axisTestClassGroup);

		_axisTestClassGroup = axisTestClassGroup;
	}

	private final AxisTestClassGroup _axisTestClassGroup;

}