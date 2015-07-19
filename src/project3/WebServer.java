package project3;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class WebServer {
	private static ServerSocket serverSocket;

	public static void main(String[] args) throws IOException{
		serverSocket = new ServerSocket(8088); // AT port 8088
		for(;;){
			try{
				Socket sock = serverSocket.accept(); // Waiting for clients
				new HandleClient(sock);
			}
			catch(SocketException se){
				System.err.println(se.getMessage());
				se.printStackTrace();
			}
		}
	}
}
