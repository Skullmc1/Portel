# Portel Roadmap

This file outlines the future development goals for Portel.

## 🚀 Exciting Features (High Priority)

- [ ] **API Integration:** Develop a robust API allowing other plugins to register custom web endpoints (GET/POST) and handle requests programmatically.
- [ ] **PlaceholderAPI Support:** Implement a simple templating engine that parses PlaceholderAPI placeholders in HTML files (e.g., displaying `%server_online%` or player stats on the hosted page).
- [x] **WebSocket Support:** Add WebSocket capabilities for real-time, bidirectional communication between the web interface and the Minecraft server (perfect for live server chats or consoles).
- [x] **HTTPS/SSL Support:** Implement support for keystores/SSL certificates to allow secure `https://` connections.

## 🛠️ Core Improvements (Medium Priority)

- [ ] **Web-Based Admin Panel:** Create a default, secured internal dashboard for admins to view logs, manage whitelists, and monitor server performance via the browser.
- [ ] **Directory Listing:** Add a configurable option to enable directory indexing, allowing users to browse file hierarchies.
- [ ] **Dynamic Management Commands:** Add commands to modify the whitelist/blacklist at runtime (e.g., `/portel whitelist add <ip>`).

## 📝 Polish & Maintenance (Low Priority)

- [ ] **Smart MIME Types:** Enhance the `WebServerManager` to automatically detect and serve correct Content-Types for a broader range of file extensions (.webp, .svg, .json, etc.).
- [ ] **Documentation:** specific documentation for API usage and configuration examples.
- [ ] **Log Viewer:** Add a command (`/portel logs`) to view recent web access logs and security blocks in-game.