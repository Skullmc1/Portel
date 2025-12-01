# Portel HTTPS/SSL Support

Portel supports serving your website and WebSocket over secure HTTPS and WSS connections using a Java KeyStore (JKS).

## 🔒 Why Use SSL?
-   **Security:** Encrypts communication between the user and the server.
-   **Browser Trust:** Removes "Not Secure" warnings.
-   **WebSocket:** Browsers often require secure WebSockets (`wss://`) if the page is loaded via HTTPS.

## 🛠️ Configuration

To enable SSL, you need a valid `.jks` (Java KeyStore) file containing your certificate and private key.

### 1. Generate or Obtain a KeyStore
If you have a domain and certificates (e.g., from Let's Encrypt), you need to convert them to JKS format.

**Self-Signed (For Testing/Localhost):**
You can generate a self-signed keystore using the Java `keytool` command included with your JDK.
```bash
keytool -genkey -alias portel -keyalg RSA -keystore keystore.jks -keysize 2048
```
*Follow the prompts. Remember the password you set!*

### 2. Update `config.yml`
Place the `keystore.jks` file in your plugin folder (same folder as `config.yml`).

```yaml
ssl:
  enabled: true
  keystore-path: "keystore.jks"
  keystore-password: "your_password_here"
```

### 3. Restart Portel
Reloading the plugin (`/portel reload`) or restarting the server will apply the changes.

## 🌐 Accessing the Site
Once enabled:
-   **Website:** `https://your-server-ip:port/`
-   **WebSocket:** The website will automatically attempt to connect via `wss://`.

**Note:** If using a self-signed certificate, your browser will show a security warning. You must manually proceed/accept the risk to view the site.
