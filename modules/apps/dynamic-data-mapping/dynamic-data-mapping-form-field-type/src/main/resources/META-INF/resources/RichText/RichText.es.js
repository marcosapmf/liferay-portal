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

import {Editor} from 'frontend-editor-ckeditor-web';
import React, {useCallback, useRef} from 'react';

import {FieldBase} from '../FieldBase/ReactFieldBase.es';
import {useSyncValue} from '../hooks/useSyncValue.es';

const ClassicEditor = ({contents, editorConfig, onChange, ...otherProps}) => {
	const editorRef = useRef();

	const getHTML = useCallback(() => {
		let data = contents;

		const editor = editorRef.current.editor;

		if (editor && editor.instanceReady) {
			data = editor.getData();

			if (CKEDITOR.env.gecko && CKEDITOR.tools.trim(data) === '<br />') {
				data = '';
			}
		}

		return data;
	}, [contents]);

	return (
		<Editor
			className="lfr-editable"
			config={{
				toolbar: 'simple',
				...editorConfig,
			}}
			onChange={() => {
				const editor = editorRef.current.editor;

				if (editor.checkDirty()) {
					onChange(getHTML());

					editor.resetDirty();
				}
			}}
			onInstanceReady={() => {
				const editor = editorRef.current.editor;

				editor.setData(contents);
			}}
			ref={editorRef}
			{...otherProps}
		/>
	);
};

const RichText = ({
	editorConfig,
	id,
	name,
	onChange,
	predefinedValue,
	readOnly,
	value,
	visible,
	...otherProps
}) => {
	const [currentValue, setCurrentValue] = useSyncValue(
		value ? value : predefinedValue
	);

	return (
		<FieldBase
			{...otherProps}
			id={id}
			name={name}
			readOnly={readOnly}
			style={readOnly ? {pointerEvents: 'none'} : null}
			visible={visible}
		>
			<ClassicEditor
				contents={currentValue}
				data={currentValue}
				editorConfig={editorConfig.JSONObject}
				name={name}
				onChange={(data) => {
					if (currentValue !== data) {
						setCurrentValue(data);

						onChange({}, data);
					}
				}}
				readOnly={readOnly}
			/>

			<input
				defaultValue={currentValue}
				id={id || name}
				name={name}
				type="hidden"
			/>
		</FieldBase>
	);
};

export default RichText;
