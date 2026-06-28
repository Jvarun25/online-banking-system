/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#eef2f9',
          100: '#d6e0ef',
          200: '#aec1df',
          300: '#85a2cf',
          400: '#5d83bf',
          500: '#3f6aa8',
          600: '#2f5286',
          700: '#243f68',
          800: '#1a2d4a',
          900: '#101b2d',
          950: '#080e17',
        },
        mint: {
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
