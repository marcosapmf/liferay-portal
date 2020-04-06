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

import ClayModal, {useModal} from '@clayui/modal';
import {RuleEditor} from 'dynamic-data-mapping-form-builder';
import React, {useContext, useEffect, useRef, useState} from 'react';

import AppContext from '../../AppContext.es';
import DataLayoutBuilderContext from '../../data-layout-builder/DataLayoutBuilderContext.es';
import {getItem} from '../../utils/client.es';
import Button from '../button/Button.es';

class RuleEditorWrapper extends RuleEditor {
	getChildContext() {
		return {
			store: {
				editingLanguageId: 'en_US',
			},
		};
	}
}

const RuleEditorModalContent = ({onClose}) => {
	const ruleEditorRef = useRef();
	const [ruleEditor, setRuleEditor] = useState(null);

	const [
		{
			config: {ruleSettings},
			spritemap,
		},
	] = useContext(AppContext);

	const [dataLayoutBuilder] = useContext(DataLayoutBuilderContext);
	const {pages} = dataLayoutBuilder.getStore();

	const [state, setState] = useState({
		isLoading: true,
		roles: [],
	});

	useEffect(() => {
		const {isLoading, roles} = state;

		if (isLoading || ruleEditor !== null) {
			return;
		}

		const ruleEditorWrapper = new RuleEditorWrapper(
			{
				...ruleSettings,
				actions: [],
				conditions: [],
				events: {
					ruleAdded: rule => {
						dataLayoutBuilder.dispatch('ruleAdded', rule);
						onClose();
					},
					ruleCancelled: () => {},
					ruleDeleted: () => {},
					ruleEdited: () => {},
				},
				key: 'create',
				pages,
				ref: 'RuleEditor',
				roles,
				spritemap,
			},
			ruleEditorRef.current
		);

		setRuleEditor(ruleEditorWrapper);
	}, [
		dataLayoutBuilder,
		onClose,
		pages,
		ruleEditor,
		ruleEditorRef,
		ruleSettings,
		spritemap,
		state,
	]);

	useEffect(() => {
		return () => ruleEditor && ruleEditor.dispose();
	}, [ruleEditor]);

	useEffect(() => {
		getItem('/o/headless-admin-user/v1.0/roles').then(
			({items: roles = []}) => {
				roles = roles.map(({id, name}) => ({
					id: `${id}`,
					label: name,
					name,
					value: `${id}`,
				}));

				setState(prevState => ({
					...prevState,
					isLoading: false,
					roles,
				}));
			}
		);
	}, []);

	return (
		<>
			<ClayModal.Header>
				{Liferay.Language.get('add-rule')}
			</ClayModal.Header>
			<ClayModal.Body>
				<div ref={ruleEditorRef}></div>
			</ClayModal.Body>
			<ClayModal.Footer
				last={
					<Button
						onClick={() => {
							ruleEditor._handleRuleAdded();
						}}
					>
						{Liferay.Language.get('save')}
					</Button>
				}
			/>
		</>
	);
};

const RuleEditorModal = ({isVisible, onClose}) => {
	const {observer} = useModal({
		onClose,
	});

	if (!isVisible) {
		return <></>;
	}

	return (
		<ClayModal
			className="data-layout-builder-rule-editor-modal"
			observer={observer}
			size="lg"
			status="info"
		>
			<RuleEditorModalContent onClose={onClose} />
		</ClayModal>
	);
};

export default RuleEditorModal;
