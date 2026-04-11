# Call Your Mechanic - Frontend

A React application for the Call Your Mechanic service, built with Vite, TypeScript, and Tailwind CSS.

## Features

- User and mechanic dashboards
- Real-time tracking with Google Maps integration
- Live location updates via Server-Sent Events
- Responsive design with shadcn/ui components

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Set up environment variables:
   ```bash
   cp .env.example .env
   ```

3. Configure your Google Maps API key:
   - Get an API key from [Google Cloud Console](https://console.cloud.google.com/google/maps-apis)
   - Enable the Maps JavaScript API
   - Add your API key to `.env`:
     ```
     VITE_GOOGLE_MAPS_API_KEY=your_actual_api_key_here
     ```

4. Start the development server:
   ```bash
   npm run dev
   ```

## Google Maps Integration

The tracking page uses Google Maps to display real-time location updates. Make sure to:

1. Enable billing on your Google Cloud project
2. Enable the Maps JavaScript API
3. Restrict your API key to your domain for security

## Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run test` - Run tests
