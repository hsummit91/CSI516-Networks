package project1;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{

	private final int port = 10000; // Port set to 10000
	private String text; // Temporary variable to store what's read from client
	private BufferedReader br; // To read from Client
	private PrintWriter pw; // To Write to Client
	private Socket socket;
	private ServerSocket s;
	
	public Server(){
		new Thread(this).start(); // Start the server
	}

	public void run(){

		try {
			s = new ServerSocket(port); //Create a server socket on the specified port


			for(;;) { // Loop Forever
				socket = s.accept(); // Accept incoming Connections
				br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Initializing BufferedReader to Read
				pw = new PrintWriter(socket.getOutputStream(), true); // Initializing Writer to Write to Client

				if(!checkHELO(br, pw)){ // Check if First Command is HELO, if NOT, Close the connection
					socket.close();
					continue;
				}

				while(true){
					// check for Other Commands
					text = br.readLine();
					if(text == null) // After QUIT Command
						break;
					if(text.equals("SHARE"))
						pw.println("404 Not on your life");
					else if(text.equals("MINE"))
						pw.println("403 No, it's mine");
					else if(text.equals("YOURS"))
						pw.println("230 Of course it's mine");
					else if(text.equals("QUIT")){

						/* 
						 * This statement below will never be printed, as "QUIT" Command is in-built in Terminal
							And that will be taken as DEFAULT when Client gives "QUIT". Reply will be "Done".
						 */
						pw.println("100 Don't go away mad, just go away");
						break;
					}
					else
						pw.println("401 Unrecognized input");
				}
				br.close(); // Close Reader
				pw.close(); // Close Writer
				socket.close(); // Close Socket
			}
		}catch(IOException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}


	}

	public boolean checkHELO(BufferedReader br, PrintWriter pw){
		boolean flag = false;

		try{
			pw.println("210 Welcome to the MINE server"); // Welcome Tag
			if(br.readLine().equals("HELO")){
				pw.println("220 Hello "+InetAddress.getLocalHost());
				flag = true;
			}
			else{ // First Command is NOT HELO
				pw.println("550 You must give a HELO Command");
				flag = false;
			}
		}catch(IOException io){
			System.err.println(io.getMessage());
			io.printStackTrace();
		}
		return flag;
	}
}
