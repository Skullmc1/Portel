# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
