window.onerror = function(message, source, lineno, colno, error) {
  fetch('http://localhost:3001/log', {
    method: 'POST',
    body: JSON.stringify({type: 'error', message: message, lineno: lineno, error: error?error.toString():''})
  });
};
const _warn = console.warn;
console.warn = function(...args) {
  fetch('http://localhost:3001/log', {
    method: 'POST',
    body: JSON.stringify({type: 'warn', args: args})
  });
  _warn.apply(console, args);
};
