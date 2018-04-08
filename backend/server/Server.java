
import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;

public class Server {
    private int port;
    private HttpServer server;

    public Server(int port) {
        port = port;
        server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public static void main(String[] args) {
        

    }
}