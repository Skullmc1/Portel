# Portel

## Websites hosted directly on Minecraft servers

Portel is a Minecraft plugin that allows you to host a simple website directly from your server. It starts a lightweight web server that serves files from a folder within the plugin's configuration directory.

## Features

-   **Lightweight:** Uses Java's built-in HTTP server, so it's very light on resources.
-   **Easy to use:** Just drop the plugin in your `plugins` folder and it works out of the box.
-   **Customizable:** You can easily customize the website by editing the files in the `plugins/Portel/web` folder.
-   **Configurable:** You can change the port the web server runs on in the `config.yml` file.

## How it works

Portel starts a web server on the port specified in the `config.yml` file (default is `8080`). When a user visits your server's IP address on that port, Portel serves the `index.html` file from the `plugins/Portel/web` folder. You can create a whole website with multiple HTML, CSS, and JavaScript files, and they will all be served by the plugin.

## Getting Started

1.  Download the latest release from the [releases page](https://github.com/Skullmc1/Portel/releases).
2.  Place the downloaded `.jar` file into your server's `plugins` folder.
3.  Restart your server.
4.  Open a web browser and navigate to `http://<your-server-ip>:8080`. You should see the default Portel welcome page.

## Configuration

The configuration file is located at `plugins/Portel/config.yml`.

```yaml
# The port the web server runs on.
port: 8080
# The file that gets served when you access the root URL.
index-file: index.html
```

## Building from source

To build the plugin from source, you will need to have Java 21 and Gradle installed.

1.  Clone the repository: `git clone https://github.com/Skullmc1/Portel.git`
2.  Navigate to the project directory: `cd Portel`
3.  Build the plugin: `./gradlew build`

The compiled `.jar` file will be located in the `build/libs` directory.

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or create a pull request.
