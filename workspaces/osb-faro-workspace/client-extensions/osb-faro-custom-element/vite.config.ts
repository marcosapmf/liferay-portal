import react from '@vitejs/plugin-react-swc';
import {defineConfig} from 'vite';

export default defineConfig({
	build: {
		assetsDir: 'static',
		outDir: 'build',
		rollupOptions: {
			output: {
				assetFileNames: 'static/[name].[hash][extname]',
				chunkFileNames: 'static/[name].[hash].js',
				entryFileNames: 'static/[name].[hash].js',
			},
		},
	},
	plugins: [react()],
	server: {
		port: 3000,
	},
});
