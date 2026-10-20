// Live chat: bridges this page and the Minecraft server over Portel's WebSocket.
document.addEventListener("DOMContentLoaded", () => {
  const wsPort = "%WEBSOCKET_PORT%";

  if (wsPort === "%" + "WEBSOCKET_PORT" + "%") {
    console.error(
      "Portel: the %WEBSOCKET_PORT% placeholder was not replaced by the server. Chat will not connect.",
    );
  }

  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  const wsUrl = `${protocol}://${window.location.hostname}:${wsPort}`;
  let ws;

  const chatMessages = document.getElementById("chat-messages");
  const chatInput = document.getElementById("chat-input");
  const usernameInput = document.getElementById("username-input");
  const sendBtn = document.getElementById("send-btn");
  const statusText = document.getElementById("chat-status-text");

  // Remember the visitor's name between visits
  try {
    usernameInput.value = localStorage.getItem("portel-username") || "";
  } catch (e) {}
  usernameInput.addEventListener("change", () => {
    try {
      localStorage.setItem("portel-username", usernameInput.value.trim());
    } catch (e) {}
  });

  function updateStatus(status) {
    if (!statusText) return;
    statusText.dataset.state = status;
    statusText.textContent = status === "connected" ? "Connected" : "Reconnecting…";
  }

  function appendMessage(sender, text, cls = "") {
    const line = document.createElement("div");
    line.className = "msg " + cls;
    if (sender) {
      const who = document.createElement("span");
      who.className = "who";
      who.textContent = `<${sender}> `;
      line.appendChild(who);
    }
    line.appendChild(document.createTextNode(text));
    chatMessages.appendChild(line);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function connect() {
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      updateStatus("connected");
      chatMessages.replaceChildren();
      appendMessage("", "Connected to the server chat.", "system");
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        const currentUser = usernameInput.value.trim() || "WebUser";

        // Don't echo our own web messages that the server relays back from the game
        if (
          data.source === "game" &&
          (data.sender === "WebUser" || data.sender === currentUser)
        ) {
          return;
        }

        appendMessage(
          data.sender,
          data.message,
          data.sender === currentUser ? "self" : "",
        );
      } catch (e) {
        appendMessage("", event.data, "system");
      }
    };

    ws.onclose = () => {
      updateStatus("disconnected");
      setTimeout(connect, 3000);
    };

    ws.onerror = () => ws.close();
  }

  function sendMessage() {
    const msg = chatInput.value.trim();
    const user = usernameInput.value.trim() || "WebUser";

    if (msg && ws && ws.readyState === WebSocket.OPEN) {
      ws.send(`${user}: ${msg}`);
      chatInput.value = "";
    }
  }

  sendBtn.addEventListener("click", sendMessage);
  chatInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  connect();
});
