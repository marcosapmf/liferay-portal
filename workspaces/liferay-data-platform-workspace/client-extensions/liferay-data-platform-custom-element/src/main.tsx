import React from 'react';
import {createRoot} from 'react-dom/client';

import App from './App';

class LiferayDataPlatform extends HTMLElement {
	connectedCallback() {
		const root = createRoot(this);

		root.render(<App />);
	}
}

if (!customElements.get('liferay-data-platform')) {
	customElements.define('liferay-data-platform', LiferayDataPlatform);
}
