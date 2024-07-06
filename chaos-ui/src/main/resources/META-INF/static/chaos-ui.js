document.addEventListener('htmx:configRequest', function(evt) {
    // otherwise we get that stupid 500: no message body writer for octet-stream
    evt.detail.headers['Accept'] = "application/json, */*";
});

document.addEventListener('htmx:responseError', function(evt) {
    if (evt.target.id === "submit-demo") {
        document.getElementById("demo-output").innerHTML =
            `<span class="has-text-danger">${evt.detail.error}</span>`;
    } else {
        alert(`${evt.detail.error}\ncaused by ${evt.target.id}`);
    }
});
