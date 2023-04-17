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

package com.liferay.portal.search.elasticsearch7.internal.sidecar;

import com.liferay.petra.string.StringBundler;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ElasticsearchDistribution implements Distribution {

	public static final String VERSION = "7.17.9";

	@Override
	public Distributable getElasticsearchDistributable() {
		return new DistributableImpl(
			StringBundler.concat(
				"https://artifacts.elastic.co/downloads/elasticsearch",
				"/elasticsearch-", VERSION, "-no-jdk-linux-x86_64.tar.gz"),
			_ELASTICSEARCH_CHECKSUM);
	}

	@Override
	public List<Distributable> getPluginDistributables() {
		return Arrays.asList(
			new DistributableImpl(
				_getDownloadURLString("analysis-icu"), _ICU_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-kuromoji"), _KUROMOJI_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-smartcn"), _SMARTCN_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-stempel"), _STEMPEL_CHECKSUM));
	}

	private String _getDownloadURLString(String plugin) {
		return StringBundler.concat(
			"https://artifacts.elastic.co/downloads/elasticsearch-plugins/",
			plugin, "/", plugin, "-", VERSION, ".zip");
	}

	private static final String _ELASTICSEARCH_CHECKSUM =
		"e2c6094377ed2ada0650864b7c30386467b920770b93e6f19d1448635f7f2024fbae" +
			"bd619ba683ed4c1b5dc8e67f2396183d81598a5e610ca2879ea3cd8dba32";

	private static final String _ICU_CHECKSUM =
		"1935e8c469bac4c5f0e7460778dfff64afa1a7bbca32f5d4acea1a64d50dbfb101b3" +
			"3fa9df8b1aed74eaa5b3a785642423aacae57d737d7966145893d6ddcc22";

	private static final String _KUROMOJI_CHECKSUM =
		"b2de15ab451e45b2f3bd4b9af3fc8832554facf1a62effbedc3bb1ba26aa5628695f" +
			"55da4086b547bd21ab908cb83ccb220522862100fd71ffce574b2b2d8945";

	private static final String _SMARTCN_CHECKSUM =
		"affbb4c56168bb45521cf62913b697c342448a560001bebf2a72ca5f1ba016455daa" +
			"52b6dd78a1fbb45f3eccb091214ca48e6221022986fe8e9c0050b0c2b5b2";

	private static final String _STEMPEL_CHECKSUM =
		"1a13ad408b137eee2ca0157891c45efd1659eb7772549195bd7cbc768ecaf3590c75" +
			"7c67d77fcf0ef760940f15f87bc589d7bdc01663624b1f82b85e07fd30b9";

}