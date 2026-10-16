// Logica del chat WebSocket
let nombre = "";
const miId = Math.random().toString(36).slice(2); // identifica mis mensajes

// Arma la direccion sola: sirve con localhost o con la IP de la PC
let base = location.pathname.substring(0, location.pathname.lastIndexOf("/"));
let ws = new WebSocket("ws://" + location.host + base + "/chat");

ws.onopen = function () {
    ponerEstado("Conectado", "on");
};
ws.onclose = function () {
    ponerEstado("Desconectado del servidor", "off");
    document.getElementById("btnEnviar").disabled = true;
};
ws.onerror = function () {
    ponerEstado("Error de conexion", "off");
};

ws.onmessage = function (event) {
    let datos;
    try {
        datos = JSON.parse(event.data);         // mensajes de esta pagina
    } catch (e) {
        datos = { tipo: "msg", nombre: "Anonimo", texto: event.data }; // texto simple
    }
    if (datos.tipo === "aviso") {
        agregarAviso(datos.texto);
    } else {
        agregarMensaje(datos.nombre, datos.texto, datos.hora, datos.id === miId);
    }
};

function entrar() {
    const n = document.getElementById("nombreBox").value.trim();
    if (n === "") { document.getElementById("nombreBox").focus(); return; }
    nombre = n;
    document.getElementById("login").classList.add("oculto");
    document.getElementById("messages").classList.remove("oculto");
    document.getElementById("barra").classList.remove("oculto");
    document.getElementById("titulo").textContent = nombre;
    document.getElementById("avatar").textContent = nombre.charAt(0).toUpperCase();
    document.getElementById("msgBox").focus();
    enviar({ tipo: "aviso", texto: nombre + " se unió al chat" });
}

function sendMessage() {
    let input = document.getElementById("msgBox");
    if (input.value.trim() !== "") {
        enviar({ tipo: "msg", id: miId, nombre: nombre, texto: input.value.trim(), hora: horaActual() });
        input.value = "";
        input.focus();
    }
}

function enviar(obj) {
    if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(obj));
}

// ---------- Dibujar en pantalla ----------
function agregarMensaje(autor, texto, hora, esMio) {
    const fila = document.createElement("div");
    fila.className = "fila" + (esMio ? " mia" : "");
    const b = document.createElement("div");
    b.className = "burbuja";
    b.innerHTML =
        '<div class="autor"></div><div class="texto"></div><div class="hora"></div>';
    b.querySelector(".autor").textContent = esMio ? "Tú" : autor;
    b.querySelector(".texto").textContent = texto;   // textContent evita inyectar HTML
    b.querySelector(".hora").textContent = hora || horaActual();
    fila.appendChild(b);
    mostrar(fila);
}

function agregarAviso(texto) {
    const a = document.createElement("div");
    a.className = "aviso";
    a.textContent = texto;
    mostrar(a);
}

function mostrar(elemento) {
    const caja = document.getElementById("messages");
    caja.appendChild(elemento);
    caja.scrollTop = caja.scrollHeight;
}

function ponerEstado(texto, clase) {
    document.getElementById("estado").textContent = texto;
    document.getElementById("punto").className = "punto " + clase;
}

function horaActual() {
    return new Date().toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit" });
}

// Enter para enviar / entrar
document.getElementById("msgBox").addEventListener("keydown", e => { if (e.key === "Enter") sendMessage(); });
document.getElementById("nombreBox").addEventListener("keydown", e => { if (e.key === "Enter") entrar(); });