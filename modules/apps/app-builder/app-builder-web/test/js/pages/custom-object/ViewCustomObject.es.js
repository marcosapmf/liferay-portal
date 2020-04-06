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

import {act, cleanup, fireEvent, render} from '@testing-library/react';
import React from 'react';
import {HashRouter} from 'react-router-dom';

import {AppContextProvider} from '../../../../src/main/resources/META-INF/resources/js/AppContext.es';
import ViewCustomObject from '../../../../src/main/resources/META-INF/resources/js/pages/custom-object/ViewCustomObject.es';

import '@testing-library/jest-dom/extend-expect';

describe('ViewCustomObject', () => {
	const RESPONSE = {
		name: {
			en_US: 'Custom Object',
		},
	};

	const ViewCustomObjectWithRouter = () => (
		<>
			<div className="tools-control-group">
				<div className="control-menu-level-1-heading" />
			</div>
			<HashRouter>
				<AppContextProvider value={{}}>
					<ViewCustomObject
						match={{
							params: {
								dataDefinitionId: 1,
							},
						}}
					></ViewCustomObject>
				</AppContextProvider>
			</HashRouter>
		</>
	);

	beforeEach(() => {
		jest.useFakeTimers();
	});

	afterEach(() => {
		cleanup();
		jest.clearAllTimers();
		jest.restoreAllMocks();
	});

	afterAll(() => {
		jest.useRealTimers();
	});

	it('renders', async () => {
		fetch.mockResponseOnce(JSON.stringify(RESPONSE));

		const {asFragment} = render(<ViewCustomObjectWithRouter />);

		await act(async () => {
			jest.runAllTimers();
		});

		expect(asFragment()).toMatchSnapshot();
	});

	it('clicks on tabs and checks if they are active', async () => {
		fetch.mockResponse(JSON.stringify(RESPONSE));

		const {queryByText} = render(<ViewCustomObjectWithRouter />);

		await act(async () => {
			jest.runAllTimers();
		});

		expect(queryByText(RESPONSE.name.en_US)).toBeTruthy();

		const tableViewsTab = queryByText('table-views');
		fireEvent.click(tableViewsTab);
		expect(tableViewsTab.classList.contains('active')).toBeTruthy();
	});
});
