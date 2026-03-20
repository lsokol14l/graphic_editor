/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0f172a',
        tide: '#0f766e',
        coral: '#ea580c',
        cloud: '#f8fafc'
      },
      boxShadow: {
        soft: '0 12px 30px rgba(15, 23, 42, 0.10)'
      }
    }
  },
  plugins: []
}
