package project2;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class Server extends Thread{

	private ServerSocket s;
	private DataInputStream input; // To read from Client
	private DataOutputStream output; // To write to Client
	private Socket sock = null;
	private String str;

	private final int port=40000; //Setting port to 40000
	private final String defaultPath = "/Users/Summit"; //Default Path
	private File dir;
	private String[] split;
	private StringBuilder sb;
	
	public Server(){
		dir = new File(defaultPath);
		new Thread(this).start(); // Start the server

	}

	public void run() {

		try{	
			System.out.println("Creating Socket on Port: "+port+" on "+InetAddress.getLocalHost());
			s = new ServerSocket(port);

			for(;;){ // loop forever
				dir = new File(defaultPath); //Setting default path again so that when a new client is connected, this is reset!
				sock = s.accept(); // Accept incoming Connections
				input = new DataInputStream(sock.getInputStream()); // Initializing to Read
				output = new DataOutputStream(sock.getOutputStream()); // Initializing Writer to Write to Client

				if(!checkHELO(input, output)){ // Check if First Command is HELO, if NOT, Close the connection
					sock.close();
					continue;
				}

				while(true){
					// check for Other Commands
					str = input.readUTF();

					if(str.equals(" "))
						continue;


					//CD, PWD, LS, GET, and PUT


					else if(str.startsWith("CD") || str.startsWith("cd")){
						split = str.split(" ");

						sb = new StringBuilder(dir.getAbsolutePath());
						sb.append("/");
						sb.append(split[1]);

						if(dir.exists()){ //Valid directory
							dir = new File(sb.toString());
							output.writeUTF("201 Directory Changed");
						}
						else
							output.writeUTF("401 Directory Change failed");
					}


					else if(str.equalsIgnoreCase("PWD"))
						output.writeUTF("202 " +dir.getAbsolutePath());


					else if(str.equalsIgnoreCase("LS")){
						sb = new StringBuilder();
						String[] strs = dir.list(); //Getting list of files in the directory
						sb.append("203 List follows:\n");

						for (int i = 0; i < strs.length; i++)
							sb.append(strs[i]+"\n");
						output.writeUTF(sb.toString());
					}


					else if(str.startsWith("GET") || str.startsWith("get")){
						split = str.split(" ");

						StringBuilder fileName = new StringBuilder();
						fileName.append(split[1]);

						File f = new File(fileName.toString());

						if(f.exists()){ //File found
							byte[] data = new byte[(int)f.length()];
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
							bis.read(data, 0, data.length);

							sb = new StringBuilder();
							sb.append("204 Content-Length: "+data.length+"\n");

							output.writeUTF(sb.toString());	
							Thread.sleep(1000);

							output.writeInt(data.length);
							output.write(data, 0, data.length); //Write data in Bytes

							bis.close();
						}
						else
							output.writeUTF("403 No Such File");
					}

					else if(str.startsWith("PUT") || str.startsWith("put")){

						// File will be stored in Current Working Directory of Server

						split = str.split(" ");
						StringBuilder fileName = new StringBuilder(split[1]);

						sb = new StringBuilder();
						sb.append("204 OK to send file!");

						output.writeUTF(sb.toString());

						String[] splitFileName = fileName.toString().split("/"); //Split path to get FileName
						int index=0;
						for(int i=0; i < splitFileName.length; i++)
							index = i;
						fileName = new StringBuilder(dir.getAbsolutePath());
						fileName.append("/");
						fileName.append(splitFileName[index]);

						File serverFile = new File(fileName.toString());

						int len = input.readInt();
						byte[] data = new byte[len];
						if (len > 0)
							input.readFully(data);

						DataOutputStream d = new DataOutputStream(new FileOutputStream(serverFile));
						d.write(data);
						d.close();
					}

					else if(str.equals("QUIT")){
						output.writeUTF("100 Don't go away mad, Just Go!");
						break;
					}

					else
						output.writeUTF("Wrong Entry");
				}
				sock.close();
				input.close();
				output.close();
			}

		}catch(SocketException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}catch(IOException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean checkHELO(DataInputStream input, DataOutputStream output){
		boolean flag = false;

		try{
			output.writeUTF("210 Welcome to the MINE server"); // Welcome Tag
			if(input.readUTF().equals("HELO")){
				output.writeUTF("220 Hello "+InetAddress.getLocalHost());
				flag = true;
			}
			else{ // First Command is NOT HELO
				output.writeUTF("550 You must give a HELO Command");
				flag = false;
			}
		}catch(IOException io){
			System.err.println(io.getMessage());
			io.printStackTrace();
		}
		return flag;
	}
}
