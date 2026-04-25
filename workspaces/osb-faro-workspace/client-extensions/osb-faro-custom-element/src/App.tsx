import React, {useEffect, useState} from 'react';

declare const Liferay: {
	authToken: string;
	ThemeDisplay: {
		getUserName: () => string;
	};
};

type Project = {
	id: number;
	name: string;
};

export default function App() {
	const [error, setError] = useState<string | null>(null);
	const [loading, setLoading] = useState(true);
	const [projects, setProjects] = useState<Project[]>([]);

	useEffect(() => {
		fetch('/o/faro/projects', {
			headers: {
				'x-csrf-token': Liferay.authToken,
			},
		})
			.then((res) => {
				if (!res.ok) {
					throw new Error(`HTTP ${res.status}`);
				}

				return res.json();
			})
			.then((data) => setProjects(data.items ?? []))
			.catch((err: Error) => setError(err.message))
			.finally(() => setLoading(false));
	}, []);

	return (
		<div style={{fontFamily: 'sans-serif', padding: '2rem'}}>
			<h1>OSB Faro</h1>

			<p>
				Logged in as:{' '}
				<strong>{Liferay.ThemeDisplay.getUserName()}</strong>
			</p>

			<h2>Projects</h2>

			{loading && <p>Loading…</p>}

			{error && (
				<p style={{color: 'red'}}>Failed to load projects: {error}</p>
			)}

			{!loading && !error && projects.length === 0 && (
				<p>No projects found.</p>
			)}

			<ul>
				{projects.map((project) => (
					<li key={project.id}>{project.name}</li>
				))}
			</ul>
		</div>
	);
}
