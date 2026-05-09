/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        zen: {
          bg: '#f9f8f4',
          text: '#333333',
          muted: '#999999',
          border: '#e2e2e2',
        }
      },
      fontFamily: {
        serif: ['"Noto Serif SC"', 'serif'],
      },
      letterSpacing: {
        'zen': '0.2em',
      }
    },
  },
  plugins: [],
}
