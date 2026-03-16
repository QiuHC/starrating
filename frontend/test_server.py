import http.server
import socketserver
import json

class Handler(http.server.SimpleHTTPRequestHandler):
    def do_POST(self):
        if self.path == '/log':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            print("FRONTEND LOG:", post_data.decode('utf-8'))
            self.send_response(200)
            self.end_headers()
        else:
            self.send_response(404)
            self.end_headers()

with socketserver.TCPServer(("", 3001), Handler) as httpd:
    print("Serving on port 3001")
    httpd.serve_forever()
