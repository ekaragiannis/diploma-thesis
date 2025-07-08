# Sensor Data Dashboard

A modern React-based web application for visualizing sensor data from the last 24 hours. The application connects to the web-server API to query sensor data from raw, hourly aggregated, and cached sources. It displays execution times for requests and maintains a history of all previous queries.

## 🚀 Usage

1. **Start the Application**: After running docker compose, visit http://localhost/

2. **Select Sensor**: Choose from available sensors in the dropdown

3. **Choose Data Type**: Select raw, hourly, or cached data

4. **Run Query**: Click "Run" to fetch and display the data

5. **View Results**: Results are displayed in the main panel with execution time

6. **Check History**: View previous queries in the right panel

## 🛠️ Technologies

The application is built using React with TypeScript for type safety, Vite for fast development and building, and uses modern state management with Zustand. For data fetching and caching, it employs TanStack Query, while styling is handled with Emotion for styled components. The UI is built with custom components and uses Axios for HTTP communication with the backend API. The application is served using nginx as a reverse proxy and static file server.
