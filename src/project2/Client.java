package project2;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;



public class Client {

	private Socket MyClient;
	private DataInputStream input; // To read from Server
	private DataOutputStream output; // To write to Server
	private BufferedReader readFromConsole; // To read from Console
	private String read;
	private String readServer;
	private final int port=40000;
	private String[] split;
	private StringBuilder fileName;
	
	public Client(){
		
		try {
			MyClient = new Socket(InetAddress.getLocalHost(), port);
			input = new DataInputStream(MyClient.getInputStream());
			output = new DataOutputStream(MyClient.getOutputStream());

			readFromConsole = new BufferedReader(new InputStreamReader(System.in));

			System.out.println(input.readUTF()); // To read Welcome Banner from Server
			Thread.sleep(3000);

			while(true){
				read = readFromConsole.readLine(); // Read from Console
				output.writeUTF(read); // Send to Server as Command...LS/PUT etc

				readServer = input.readUTF(); // Reply from Server
				System.out.println(readServer);
				Thread.sleep(1000);

				if( (read.startsWith("GET") || read.startsWith("get")) && (!readServer.startsWith("403"))){
					//If Condition is true when File is found!

					// File will be STored in Current working Directory of Client

					split = read.split(" ");

					fileName = new StringBuilder(split[1]);

					String[] splitFileName = fileName.toString().split("/"); // To get filename from full-path
					int index=0;
					for(int i=0; i < splitFileName.length; i++)
						index = i;

					File dir = new File(".");

					fileName = new StringBuilder(dir.getCanonicalPath());
					fileName.append("/");
					fileName.append(splitFileName[index]);

					int len = input.readInt();
					byte[] data = new byte[len];
					if (len > 0)
						input.readFully(data);

					File clientFile = new File(fileName.toString()); //Destination of File on Client
					DataOutputStream d = new DataOutputStream(new FileOutputStream(clientFile));
					d.write(data);
					d.close();
					System.out.println(new String(data));
				}


				if( (read.startsWith("put") || read.startsWith("PUT")) && (!readServer.startsWith("403"))){

					split = read.split(" ");

					int size = Integer.parseInt(split[2].toString());

					fileName = new StringBuilder(split[1]);

					File f = new File(fileName.toString());

					byte[] data = new byte[size];
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
					bis.read(data, 0, size);

					Thread.sleep(1000);
					System.out.println(new String(data));

					output.writeInt(data.length);
					output.write(data, 0, data.length); //Write data in Bytes

					bis.close();
				}

				if(read.equalsIgnoreCase("QUIT")){
					MyClient.close();
					input.close();
					output.close();
					break;
				}

				Thread.sleep(3000);
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
}
