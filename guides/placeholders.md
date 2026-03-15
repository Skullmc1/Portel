# Portel PlaceholderAPI Support

Portel integrates with [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) to allow you to display dynamic server information directly on your hosted web pages.

## 🚀 How It Works

When Portel serves an HTML file (ending in `.html`) or a JavaScript file (ending in `.js`), it automatically scans the file content for PlaceholderAPI placeholders and replaces them with their current values.

## 📋 Requirements

1.  **PlaceholderAPI Plugin:** You must have the PlaceholderAPI plugin installed and enabled on your server.
2.  **eCloud Expansions:** You need to download the relevant expansions for the placeholders you want to use (e.g., `/papi ecloud download Server`).

## 📝 Usage in HTML

Simply include the placeholders in your HTML code just like you would in a Minecraft chat message or scoreboard.

**Example:**

```html
<!DOCTYPE html>
<html>
<head>
    <title>My Minecraft Server</title>
</head>
<body>
    <h1>Welcome to %server_name%!</h1>
    <p>There are currently <strong>%server_online%</strong> / <strong>%server_max_players%</strong> players online.</p>
    <p>Server TPS: %server_tps%</p>
</body>
</html>
```

## ⚠️ Limitations

*   **Player Context:** Since web requests are generic and not tied to a specific logged-in Minecraft player, you can currently only use **Global** or **Server** placeholders (e.g., `%server_online%`, `%server_ram_used%`). Player-specific placeholders (like `%player_name%` or `%vault_eco_balance%`) will not work because the web server doesn't know "who" is viewing the page.
*   **Performance:** Parsing placeholders takes a small amount of time. For extremely high-traffic sites, this might add a slight overhead.
