window.addEventListener('error', function(e) {
  fetch('http://localhost:3001/log', {
    method: 'POST',
    body: "ERROR EVENT: " + e.message + " at " + e.filename + ":" + e.lineno
  });
}, true);
window.addEventListener('unhandledrejection', function(e) {
  fetch('http://localhost:3001/log', {
    method: 'POST',
    body: "UNHANDLED REJECTION: " + e.reason
  });
});
var _error = console.error;
console.error = function(...args) {
  fetch('http://localhost:3001/log', {
    method: 'POST',
    body: "CONSOLE ERROR: " + args.map(a => typeof a === 'object' ? JSON.stringify(a) : a).join(' ')
  });
  _error.apply(console, args);
};
