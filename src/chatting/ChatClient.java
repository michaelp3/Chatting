package chatting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ChatClient extends Thread {
	static Scanner scan = new Scanner(System.in);
	
	Socket socket;
	BufferedReader br;
	InputStream is;
	FileOutputStream fos;
	BufferedOutputStream bos;
	String id;
	static int x = 0;
	int bufferSize;
	boolean exit = false;
	
	public ChatClient(BufferedReader br, Socket client, String id) {
		socket = client;
		this.br = br;
		this.id = id;
		bufferSize = 0;
	}
	
	public void run() {
		while (!exit) {
			if (br != null) {
				try {
					String line = br.readLine();
					if(line.equals("/f")) {
						Socket nSocket = new Socket("127.0.0.1", 9999);
						is = nSocket.getInputStream();
			            bufferSize = nSocket.getReceiveBufferSize();
			            fos = new FileOutputStream(id + "_test" + x + ".txt");
			            bos = new BufferedOutputStream(fos);
//						OutputStream out = new FileOutputStream(id + "_test" + x + ".txt");
						x++;
						byte[] bytes = new byte[bufferSize];

				        int count;
				        while ((count = is.read(bytes)) >= 0) {
				            bos.write(bytes, 0, count);
				        }
				        bos.close();
				        is.close();
						nSocket.close();
					}
					else {
						System.out.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void end() {
		exit = true;
	}
	
	public static void main(String[] args) {
		// 클라이언트 - Socket
		BufferedReader br = null;
		try {
			Socket socket = new Socket("127.0.0.1", 9990);
			System.out.println("채팅 서버에 연결 되었습니다.");
			PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
			pw.println(args[0]);
			pw.flush();
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			String inMessage = br.readLine();
//			System.out.println(inMessage);
			
			
//			OutputStream out = socket.getOutputStream();
//			InputStream in = socket.getInputStream();
			
//			File file;
//			long length = file.length();
			
			
			//메세지 수신 대기
			ChatClient client = new ChatClient(br, socket, args[0]);
			client.start();
			
			while(true) {
				String line = scan.nextLine();
				if(line.equals("quit")) {
					pw.println(line);
					pw.flush();
					break;
				}
				else if(line.length() > 2 && line.substring(0,2).equals("/f")) {
					pw.println("/f");
					pw.flush();
					File file = new File(line.substring(3));
					long length = file.length();
					if(length > Long.MAX_VALUE) {
						System.out.println("파일이 너무 큼.");
					}
					else {
						Socket nSocket = new Socket("127.0.0.1", 12345);
						InputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);
						BufferedOutputStream out = new BufferedOutputStream(nSocket.getOutputStream());
						
						byte[] bytes = new byte[8192];
						int count;
						while ((count = bis.read(bytes)) > 0) {
				            out.write(bytes, 0, count);
				        }
						out.close();
						fis.close();
						bis.close();
					}
				}
				else {
					pw.println(args[0] + ": " + line);
					pw.flush();
				}
			}
			client.end();
//			if(in != null) {
//				in.close();
//			}
			socket.close();
		} catch (IOException e) {
		}
	}

}
