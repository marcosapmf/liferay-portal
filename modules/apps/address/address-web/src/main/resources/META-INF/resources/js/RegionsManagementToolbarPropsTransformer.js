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

import {getCheckedCheckboxes, postForm} from 'frontend-js-web';

export default function propsTransformer({portletNamespace, ...otherProps}) {
	const activateRegions = (itemData) => {
		const form = document.getElementById(`${portletNamespace}fm`);

		if (form) {
			postForm(form, {
				data: {
					regionIds: getCheckedCheckboxes(
						form,
						`${portletNamespace}allRowIds`
					),
				},
				url: itemData?.activateRegionsURL,
			});
		}
	};

	const deactivateRegions = (itemData) => {
		if (
			confirm(
				Liferay.Language.get(
					'are-you-sure-you-want-to-deactivate-the-selected-regions'
				)
			)
		) {
			const form = document.getElementById(`${portletNamespace}fm`);

			if (form) {
				postForm(form, {
					data: {
						regionIds: getCheckedCheckboxes(
							form,
							`${portletNamespace}allRowIds`
						),
					},
					url: itemData?.deactivateRegionsURL,
				});
			}
		}
	};

	const deleteRegions = (itemData) => {
		if (
			confirm(
				Liferay.Language.get(
					'are-you-sure-you-want-to-delete-the-selected-regions'
				)
			)
		) {
			const form = document.getElementById(`${portletNamespace}fm`);

			if (form) {
				postForm(form, {
					data: {
						regionIds: getCheckedCheckboxes(
							form,
							`${portletNamespace}allRowIds`
						),
					},
					url: itemData?.deleteRegionsURL,
				});
			}
		}
	};

	return {
		...otherProps,
		onActionButtonClick: (event, {item}) => {
			const data = item?.data;

			const action = data?.action;

			if (action === 'activateRegions') {
				activateRegions(data);
			}
			else if (action === 'deactivateRegions') {
				deactivateRegions(data);
			}
			else if (action === 'deleteRegions') {
				deleteRegions(data);
			}
		},
	};
}
