

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.*;




public class Server {
    private int port;
    private HttpServer app;


    public class NewUserHandler implements HttpHandler {


        public void handle(HttpExchange he) throws IOException {
            String resp = "<h1>Okay</h1>";
            he.sendResponseHeaders(200, resp.length());
            OutputStream os = he.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }
    }



    private Server(int server_port) {
        port = server_port;
        try {
            app = HttpServer.create(new InetSocketAddress(port), 0);
            app.createContext("/user/new", new NewUserHandler());
            app.setExecutor(null);
        } catch (IOException ex) {
            System.out.println("Error creating server: " + ex.getMessage());
            System.exit(1);
        }
    }

    private void start() {
        app.start();
    }

    public static void main(String[] args) {
        Server server = new Server(8080);
        server.start();
    }

}

