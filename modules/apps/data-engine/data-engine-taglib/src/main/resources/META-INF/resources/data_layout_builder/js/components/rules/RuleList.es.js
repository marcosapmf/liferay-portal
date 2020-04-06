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

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import React, {useContext, useState} from 'react';

import AppContext from '../../AppContext.es';
import EmptyState from '../empty-state/EmptyState.es';
import SearchInput from '../search-input/SearchInput.es';
import RuleEditorModal from './RuleEditorModal.es';

export default () => {
	const [isRulesEditorVisible, setRulesEditorVisible] = useState(false);
	const [searchText, setSearchText] = useState('');

	const [
		{
			dataLayout: {dataRules},
		},
	] = useContext(AppContext);

	const filtereDataRules = dataRules
		.map((rule, index) => ({
			...rule,
			name: `Rule ${index}`,
		}))
		.filter(({name}) => new RegExp(searchText, 'ig').test(name));

	return (
		<>
			<div className="autofit-row sidebar-section">
				<div className="autofit-col autofit-col-expand">
					<SearchInput
						onChange={searchText => setSearchText(searchText)}
					/>
				</div>

				<div className="autofit-col ml-2">
					<ClayButtonWithIcon
						displayType="primary"
						onClick={() =>
							setRulesEditorVisible(!isRulesEditorVisible)
						}
						symbol="plus"
					/>
				</div>
			</div>

			{filtereDataRules.length === 0 ? (
				<EmptyState
					emptyState={{
						button: () => (
							<ClayButton
								displayType="secondary"
								onClick={() =>
									setRulesEditorVisible(!isRulesEditorVisible)
								}
							>
								{Liferay.Language.get('add-rule')}
							</ClayButton>
						),
						title: Liferay.Language.get('there-are-no-rules'),
					}}
					keywords={searchText}
					small
				/>
			) : (
				filtereDataRules.map(
					({conditions, logicalOperator, name}, index) => (
						<p key={index}>
							<div>name: {name}</div>
							<div>conditions: {JSON.stringify(conditions)}</div>
							<div>logicalOperator: {logicalOperator}</div>
						</p>
					)
				)
			)}

			<RuleEditorModal
				isVisible={isRulesEditorVisible}
				onClose={() => setRulesEditorVisible(false)}
			/>
		</>
	);
};
