document.addEventListener('DOMContentLoaded', () => {
    const wsPort = %WEBSOCKET_PORT%; 
    
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const wsUrl = `${protocol}://${window.location.hostname}:${wsPort}`;
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
                appendMessage(`[${data.sender}] ${data.message}`);
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
            // Optimistically append own message
            appendMessage(`[${user}] ${msg}`, 'text-acc');
        }
    }

    sendBtn.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    connect();
});
