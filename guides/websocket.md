# Portel WebSocket Feature

Portel now includes a built-in WebSocket server to enable real-time, bidirectional communication between your Minecraft server and the hosted website. The default implementation showcases a **Live Server Chat**.

## 🚀 How It Works

1.  **Server-Side:** Portel starts a WebSocket server on a specified port (default: `8081`).
2.  **Client-Side:** The hosted webpage connects to this WebSocket server.
3.  **Communication:**
    *   **Minecraft -> Web:** When a player chats in-game, the message is sent to all connected web clients.
    *   **Web -> Minecraft:** When a user sends a message from the website, it is broadcasted to all players in-game.

## ⚙️ Configuration

In your `config.yml`, you can configure the WebSocket port. By default, it tries to use the main web server port + 1.

```yaml
port: 8080
websocket-port: 8081 # The port for the WebSocket server
```

**Note:** Ensure this port is open and forwarded if you are running the server behind a router or firewall, just like your main web server port.

## 💻 Frontend Implementation (For Custom Pages)

If you are building your own custom `index.html`, Portel provides a convenient token replacement to help you connect to the correct port without hardcoding it.

### 1. Automatic Port Injection
In your HTML or JavaScript files served by Portel, use the token `%WEBSOCKET_PORT%`. Portel will automatically replace this with the port defined in your config before sending the file to the browser.

**Example (`script.js`):**
```javascript
// Portel replaces this with the actual number from config.yml
const wsPort = %WEBSOCKET_PORT%; 
const wsUrl = `ws://${window.location.hostname}:${wsPort}`;
const ws = new WebSocket(wsUrl);
```

### 2. Message Format
The WebSocket server communicates using simple JSON for server-to-client messages.

**Incoming (from Minecraft):**
```json
{
  "sender": "PlayerName",
  "message": "Hello from the game!"
}
```

**Outgoing (to Minecraft):**
Just send a plain text string.
```javascript
ws.send("User: Hello from the web!");
```

## 🎮 In-Game Appearance

Messages sent from the website will appear in the Minecraft chat with a specific format to distinguish them from regular players.

**Format:** `[Portel] * WebUser: Hello!`
*   **Prefix:** Dark Purple `[Portel]`
*   **Separator:** Dark Purple `*`
*   **Message:** Light Purple
