// tailwind.config.js
module.exports = {
	plugins: [
		require('@tailwindcss/ui'),
	],
	purge: [
		"src/main/resources/templates/**/*.mustache",
		"src/main/resources/static/js/**/*.vue",
	],
	theme: {
		extend: {
			colors: {
				"semi-75": "rgba(0, 0, 0, 0.75)",
			},
		},
	},
	variants: {
		cursor: ["responsive", "hover", "disabled"],
		opacity: ["disabled"],
	},
};
