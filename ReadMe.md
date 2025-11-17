<div align="center">
  <img src="images/Heading.png" alt="Portel Logo" width="800"/>
</div>


## Websites hosted directly on Minecraft servers

Portel is a Minecraft plugin that allows you to host a simple website directly from your server. It starts a lightweight web server that serves files from a folder within the plugin's configuration directory.

## Why use Portel?

-   **Simplicity:** Portel is designed to be easy to use. Simply drop the plugin into your server's `plugins` folder and you're ready to go. No complex setup or configuration required.
-   **Performance:** Portel is built to be lightweight and efficient. It uses Java's built-in HTTP server to minimize resource usage, ensuring that your server's performance is not affected.
-   **Customization:** Portel gives you full control over your website. You can create your own HTML, CSS, and JavaScript files to build a unique website that reflects your server's identity.
-   **Real-time updates:** Changes to your website files are reflected in real-time, so you can see your changes instantly without having to restart your server.
-   **No external hosting required:** Host your website directly on your Minecraft server, eliminating the need for a separate web hosting service.

## Getting Started

1.  Download the latest release from the [releases page](https://github.com/Skullmc1/Portel/releases).
2.  Place the downloaded `.jar` file into your server's `plugins` folder.
3.  Restart your server.
4.  Open a web browser and navigate to `http://<your-server-ip>:<port>`. You should see the default Portel welcome page. The default port is `8080`.

## Configuration

The configuration file is located at `plugins/Portel/config.yml`.

```yaml
# The port the web server runs on.
port: 8080
# The file that gets served when you access the root URL.
index-file: index.html

rate-limiting:
  enabled: true
  delay: 1000 # The delay in milliseconds between requests from the same IP address
```

## Commands

-   `/portel restart` - Restarts the web server.
    -   Permission: `portel.restart`
    -   Alias: `/p restart`

## Building from source

To build the plugin from source, you will need to have Java 21 and Gradle installed.

1.  Clone the repository: `git clone https://github.com/Skullmc1/Portel.git`
2.  Navigate to the project directory: `cd Portel`
3.  Build the plugin: `./gradlew build`

The compiled `.jar` file will be located in the `build/libs` directory.

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or create a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
