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

import '@testing-library/jest-dom/extend-expect';
import {cleanup, render} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import switchViewportSize from '../../../../src/main/resources/META-INF/resources/page_editor/app/actions/switchViewportSize';
import ViewportSizeSelector from '../../../../src/main/resources/META-INF/resources/page_editor/app/components/ViewportSizeSelector';
import {StoreAPIContextProvider} from '../../../../src/main/resources/META-INF/resources/page_editor/app/store';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/config',
	() => ({
		config: {
			availableViewportSizes: {
				desktop: {label: 'Desktop', sizeId: 'desktop'},
				mobile: {label: 'Mobile', sizeId: 'mobile'},
				tablet: {label: 'Tablet', sizeId: 'tablet'},
			},
		},
	})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/actions/switchViewportSize',
	() => jest.fn(() => () => {})
);

const defaultState = {
	selectedViewportSize: 'desktop',
};

const renderTranslation = ({state}) => {
	return render(
		<StoreAPIContextProvider getState={() => state}>
			<ViewportSizeSelector selectedSize={state.selectedViewportSize} />
		</StoreAPIContextProvider>
	);
};

describe('ViewportSizeSelector', () => {
	afterEach(cleanup);

	it('renders ViewportSizeSelector component', () => {
		const {getByTitle} = renderTranslation({state: defaultState});

		expect(getByTitle('Desktop')).toBeInTheDocument();
		expect(getByTitle('Mobile')).toBeInTheDocument();
		expect(getByTitle('Tablet')).toBeInTheDocument();
	});

	it('dispatches sizeId when a size is selected', () => {
		const {getByTitle} = renderTranslation({state: defaultState});
		const button = getByTitle('Mobile');

		userEvent.click(button);

		expect(switchViewportSize).toHaveBeenLastCalledWith({
			size: 'mobile',
		});
	});
});
