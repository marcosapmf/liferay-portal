import React from 'react';
import {createRoot} from 'react-dom/client';

import App from './App';

class OsbFaro extends HTMLElement {
	connectedCallback() {
		const root = createRoot(this);

		root.render(<App />);
	}
}

if (!customElements.get('osb-faro')) {
	customElements.define('osb-faro', OsbFaro);
}
