document.body.addEventListener('htmx:configRequest', function(evt) {
    // otherwise we get that stupid 500: no message body writer for octet-stream
    evt.detail.headers['Accept'] = "application/json, */*";
});
