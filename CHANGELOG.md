# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
