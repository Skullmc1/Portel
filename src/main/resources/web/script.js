console.log("Portel script loading...");

document.addEventListener("DOMContentLoaded", () => {
  console.log("Portel DOM Content Loaded");
  const wsPort = "%WEBSOCKET_PORT%";

  if (wsPort === "%" + "WEBSOCKET_PORT" + "%") {
    console.error(
      "CRITICAL: WebSocket port placeholder was not replaced by the server! WebSocket will fail.",
    );
  }

  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  const wsUrl = `${protocol}://${window.location.hostname}:${wsPort}`;
  console.log("Connecting to WebSocket at:", wsUrl);
  let ws;

  const chatMessages = document.getElementById("chat-messages");
  const chatInput = document.getElementById("chat-input");
  const usernameInput = document.getElementById("username-input");
  const sendBtn = document.getElementById("send-btn");
  const statusText = document.getElementById("chat-status-text");

  function updateStatus(status) {
    if (!statusText) return;
    if (status === "connected") {
      statusText.innerText = "Connected";
      statusText.style.color = "#8A2BE2";
      statusText.style.textShadow = "0 0 10px rgba(138,43,226,0.5)";
    } else {
      statusText.innerText = "Disconnected";
      statusText.style.color = "#ef4444";
      statusText.style.textShadow = "none";
    }
  }

  function connect() {
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      updateStatus("connected");
      chatMessages.innerHTML = "";
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        const currentUser = usernameInput.value.trim() || "WebUser";

        if (
          data.source === "game" &&
          (data.sender === "WebUser" || data.sender === currentUser)
        ) {
          return;
        }

        const isSelf = data.sender === currentUser;
        appendMessage(
          `[${data.sender}] ${data.message}`,
          isSelf ? "text-[#8A2BE2] font-semibold" : "text-gray-400",
        );
      } catch (e) {
        appendMessage(event.data);
      }
    };

    ws.onclose = () => {
      updateStatus("disconnected");
      setTimeout(connect, 3000);
    };

    ws.onerror = (err) => {
      console.error("WebSocket error", err);
      ws.close();
    };
  }

  function appendMessage(text, color = "text-gray-300") {
    const div = document.createElement("div");
    div.className = `mb-2 break-words p-3 rounded-lg bg-[#111] border border-gray-900 shadow-sm animate-fade ${color}`;
    div.innerText = text;
    chatMessages.appendChild(div);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function sendMessage() {
    const msg = chatInput.value.trim();
    const user = usernameInput.value.trim() || "WebUser";

    if (msg && ws && ws.readyState === WebSocket.OPEN) {
      ws.send(`${user}: ${msg}`);
      chatInput.value = "";
    } else {
      console.warn(
        "Cannot send message. WS State:",
        ws ? ws.readyState : "null",
      );
    }
  }

  sendBtn.addEventListener("click", sendMessage);
  chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  const observerOptions = {
    threshold: 0.1,
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.style.animationPlayState = "running";
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);

  document.querySelectorAll(".animate-fade").forEach((el) => {
    el.style.animationPlayState = "paused";
    observer.observe(el);
  });

  connect();
});
