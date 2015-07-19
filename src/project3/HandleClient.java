package project3;
import java.io.*;
import java.net.*;

public class HandleClient extends Thread{
	private Socket sock;
	private static final String root = "/Users/Summit/webserver/"; // Webserver Directory on Computer

	private static final String HTTP_200 = "HTTP/1.1 200 OK\r\n";

	private static final String HTTP_500 = "HTTP/1.1 500 Internal Server Error\r\n"+
			"Content-type: text/html\r\n\r\n"+
			"<html><head></head><body>500 Internal Server Error</body></html>\n";

	public HandleClient(Socket sock) {
		this.sock = sock;
		start();
	}

	
	@Override
	public void run() {
		BufferedReader in; // To read Request
		OutputStream out; // To send Data
		String req;  // Temp store  User Request
		FileInputStream fin; // To read file-content
		String type = ""; // To store MIME type.
		System.setProperty("user.dir", root);

		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = sock.getOutputStream();
			req = in.readLine();
			
			System.out.println(req);

			if(!isGET(req)){ // If not a GET request
				out.write(HTTP_500.getBytes()); // Send Error
				out.close();
			}

			String fileName = req.substring(req.indexOf(" "), req.lastIndexOf(" ")).substring(2).trim(); // Get Filename from Request

			// Check for Different filetypes, and set Content-Type accordingly
			if (fileName.endsWith(".html") || fileName.endsWith(".htm"))
				type="text/html";
			else if(fileName.endsWith(".txt"))
				type="text/plain";
			else if (fileName.endsWith(".css"))
				type="text/css";
			else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
				type="image/jpeg";
			else if (fileName.endsWith(".png"))
				type="image/png";
			else if (fileName.endsWith(".gif"))
				type="image/gif";
			else if (fileName.endsWith(".pdf"))
				type="application/pdf";
			else
				type="application/octet-stream";

			try{
				if(fileName.length()!=0) // User has entered a filename
				{
					fin = new FileInputStream(root+fileName);
					out.write(( HTTP_200+"Content-type: "+type+"\r\nContent-Length: "+fin.available()+"\r\n\r\n" ).getBytes());

					byte[] buffer = new byte[4096]; // Buffer to store file content
					int data = 0;
					while ( (data = fin.read(buffer) ) >0)
						out.write(buffer, 0, data); // Send file content
					out.close();
				}
				else{ // Filename not given, so default page is displayed
					createDefault();
					fin = new FileInputStream(root+"example.html");
					type="text/html";
					out.write(( HTTP_200+"Content-type: "+type+"\r\nContent-Length: "+fin.available()+"\r\n\r\n" ).getBytes());

					byte[] buffer = new byte[4096];
					int data = 0;
					while ( (data = fin.read(buffer) ) >0)
						out.write(buffer, 0, data);			
					out.close();
				}

			}catch(FileNotFoundException f){
				out.write( ("HTTP/1.1 404 Not Found\r\n"+
						"Content-type: text/html\r\n\r\n"+
						"<html><head></head><body>"+fileName+" not found</body></html>\n").getBytes());
			}
			out.close();

		}
		catch(IOException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

	}

	public void createDefault(){ // To create default page
		try{
			File f = new File(root);

			StringBuilder sb = new StringBuilder();

			File[] list = f.listFiles(); // List content of directory
			int i;
			for(i=0; i<list.length; i++){
				if(list[i].isFile())
					sb.append(list[i].getName()+"<br>");
				else if(list[i].isDirectory())
					sb.append(list[i].getName()+"<br>");
			}

			File file = new File(root+"example.html");
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write("<html><head><title>Networks Project 3</title></head><body><h1>This is the default web server directory contents!</h1><br>");
			output.write(sb.toString() + "</body></html>");
			output.close();
			
		}catch(IOException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean isGET(String str){ // Check for GET Request
		if(str.contains(" ")){
			if (str.substring(0, str.indexOf(" ")).equalsIgnoreCase("GET"))
				return true; // Its a GET Request
			else
				return false;
		}
		return false;
	}
}
