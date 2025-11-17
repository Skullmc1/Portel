# Portel TODO

This file contains a list of suggested features and improvements for the Portel plugin.

## High Priority

-   [x] **Security:** Implement a whitelist/blacklist system for IP addresses to control access to the web server.
-   [x] **Security:** Add rate limiting to prevent DoS attacks.
-   [x] **Feature:** Add an in-game command to manage the web server (e.g., `/portel start`, `/portel stop`, `/portel restart`, `/portel reload`).
-   [x] **Improvement:** Create a more user-friendly and customizable 404, 403 and 429 error pages.
-   [x] **Improvement:** Add configurable logging for console and IP logging.

## Medium Priority

-   [ ] **Feature:** Implement directory listing, so if a user navigates to a directory, they see a list of files in that directory.
-   [ ] **Feature:** Add support for more file types. While most should work, explicitly testing and documenting support for common web file types would be beneficial.
-   [ ] **API:** Develop an API that allows other plugins to register their own web pages or endpoints with Portel.
-   [ ] **Feature:** Add a command to manage the whitelist/blacklist (e.g., `/portel whitelist add <ip>`, `/portel blacklist remove <ip>`).

## Low Priority

-   [ ] **Feature:** Explore the possibility of a simple templating engine to allow for dynamic content in web pages (e.g., displaying server status or player count).
-   [ ] **Feature:** Add an in-game file editor to allow admins to edit the website files directly from within Minecraft.
-   [ ] **Improvement:** Add more detailed logging to track requests and errors.
-   [ ] **Improvement:** Create a more comprehensive documentation for the plugin.
-   [ ] **Feature:** Add a command to view the IP log (e.g., `/portel iplog`).
