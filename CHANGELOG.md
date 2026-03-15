# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.0] - 2026-03-15

### Added
-   **Hot-Reloading**: Added a `FileWatcher` service that automatically clears the internal web cache when files in the `web/` directory are modified, created, or deleted. No more manual restarts needed after editing assets!
-   Added `hot-reloading` toggle in `config.yml`.

### Changed
-   Refactored `onDisable` and `reload` logic to properly manage asynchronous services.

## [1.5.0] - 2026-03-15

### Added
-   Shared `SSLUtils` to unify SSL configuration between Web and WebSocket servers.
-   Configurable chat prefix and colors for web-to-game messages.
-   In-memory static file caching for faster asset delivery.
-   Asynchronous cleanup task for the RateLimiter to prevent memory growth.
-   Thread pool (10 threads) for the web server to handle concurrent requests better.

### Fixed
-   **Security**: Blocked unauthenticated web-to-game chat injection via a new toggle.
-   **Security**: Implemented strict path validation to prevent Path Traversal attacks.
-   **Security**: Added HTML escaping to PlaceholderAPI output to prevent XSS.
-   Fixed a bug where default assets were overwritten on every startup.
-   Synchronized IP logging to prevent thread-safety issues and file corruption.
-   Renamed `ips.yml` to `ips.log` and made the log file name configurable.

## [1.4.2] - 2025-11-17

### Fixed

-   Updated the shadowJar plugin to a non-deprecated version.

## [1.4.1] - 2025-11-17

### Fixed

-   Fixed a `NoClassDefFoundError` by including the Adventure API in the plugin JAR.

## [1.4.0] - 2025-11-17

### Changed

-   Complete command overhaul with a new look and feel.
-   Commands and feedback are now more user-friendly.
-   Added a more detailed help command.
-   Integrated Adventure API for chat styling.

## [1.3.1] - 2025-11-17

### Changed

-   Default assets will not be overwritten if they already exist.

## [1.3.0] - 2025-11-17

### Added

-   `/portel reload` command to reload the configuration.
-   Customizable 429 error page for rate limiting.
-   Configuration options for console and IP logging.

### Changed

-   Refactored the main `Portel.java` class into smaller, more manageable classes.

## [1.2.1] - 2025-11-17

### Added

-   Customizable 403 and 404 error pages.

## [1.2.0] - 2025-11-17

### Added

-   Whitelist/blacklist system to control access to the web server.
-   IP logging to track visitors.

## [1.1.1] - 2025-11-17

### Fixed

-   Missing `Content-Type` header in HTTP responses, which caused browsers to fail to render CSS, favicons, and logos.

## [1.1.0] - 2025-11-17

### Added

-   Rate limiting feature to prevent DoS attacks.
-   `/portel restart` command to restart the web server.

## [1.0.0] - 2025-11-17

### Added

-   Initial release of the Portel plugin.
-   Web server to host a website from the plugin's folder.
-   Configuration for port and index file.
