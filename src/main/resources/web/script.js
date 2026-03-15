console.log("Portel script loading...");

document.addEventListener('DOMContentLoaded', () => {
    console.log("Portel DOM Content Loaded");
    const wsPort = "%WEBSOCKET_PORT%"; 
    
    if (wsPort === "%" + "WEBSOCKET_PORT" + "%") {
        console.error("CRITICAL: WebSocket port placeholder was not replaced by the server! WebSocket will fail.");
    }

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const wsUrl = `${protocol}://${window.location.hostname}:${wsPort}`;
    console.log("Connecting to WebSocket at:", wsUrl);
    let ws;
    
    const chatMessages = document.getElementById('chat-messages');
    const chatInput = document.getElementById('chat-input');
    const usernameInput = document.getElementById('username-input');
    const sendBtn = document.getElementById('send-btn');

    function connect() {
        ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            chatMessages.innerHTML = '';
            appendMessage("Connected to Server Chat!", "text-green-400");
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                // data: { sender: "Name", message: "msg" }
                const currentUser = usernameInput.value.trim() || "WebUser";
                const isSelf = data.sender === currentUser;
                appendMessage(`[${data.sender}] ${data.message}`, isSelf ? 'text-acc' : 'text-t1');
            } catch (e) {
                // If not JSON, just display it
                appendMessage(event.data);
            }
        };

        ws.onclose = () => {
            appendMessage("Disconnected. Reconnecting in 3s...", "text-red-400");
            setTimeout(connect, 3000);
        };
        
        ws.onerror = (err) => {
            console.error("WebSocket error", err);
            ws.close();
        };
    }

    function appendMessage(text, color = 'text-t1') {
        const div = document.createElement('div');
        div.className = `mb-1 break-words ${color}`;
        div.innerText = text;
        chatMessages.appendChild(div);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function sendMessage() {
        const msg = chatInput.value.trim();
        const user = usernameInput.value.trim() || "WebUser";
        
        if (msg && ws && ws.readyState === WebSocket.OPEN) {
            ws.send(`${user}: ${msg}`);
            chatInput.value = '';
        } else {
            console.warn("Cannot send message. WS State:", ws ? ws.readyState : "null");
        }
    }

    sendBtn.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    connect();
});
