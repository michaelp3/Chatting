package chatting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

public class ChatServer extends Thread {
	static Hashtable<String, PrintWriter> map = new Hashtable<String, PrintWriter>();
	static Hashtable<String, Socket> map2 = new Hashtable<String, Socket>();
	BufferedReader br;
	String userId;
	InputStream in;
	
	Socket socket;
	InputStream is;
	FileOutputStream fos;
	BufferedOutputStream bos;
	int bufferSize;
	
	static int x = 0;
	
	public ChatServer(String userId, BufferedReader br, PrintWriter pw, InputStream in, Socket socket) throws IOException {
		this.userId = userId;
		this.br = br;
		this.in = in;
		this.socket = socket;
		
		bufferSize = 0;
		
//		boolean exists = false;
//		for(String key: map.keySet()) {
//			if (key.equals(userId)) {
//				exists = true;
//				break;
//			}
//		}
//		if(!exists) {
//			map.put(userId, pw);
//			sendMessage(userId + "님이 입장 하였습니다.");
//		}
		if(!map.containsKey(userId)){
			sendMessage(userId + "님이 입장 하였습니다.");
		}
		map.put(userId, pw);
		map2.put(userId, socket);
	}
	
	void sendMessage(String line) throws IOException {
		//전달 받은 메세지를 모든 소켓에 뿌린다.
		Enumeration<String> keys = map.keys();
		while(keys.hasMoreElements()) {
			String idKey = keys.nextElement();
			PrintWriter pw = map.get(idKey);
			pw.println(line);
			pw.flush();
		}
	}
	
	void receiveFile() {
		try {
			ServerSocket nserverSocket = new ServerSocket(12345);
			Socket nSocket = nserverSocket.accept();
			is = nSocket.getInputStream();
			bufferSize = nSocket.getReceiveBufferSize();
			fos = new FileOutputStream("server_test" + x + ".txt");
			x++;
			bos = new BufferedOutputStream(fos);
			byte[] bytes = new byte[bufferSize];
			int count;
            while ((count = is.read(bytes)) >= 0) {
                bos.write(bytes, 0, count);
            }
            bos.close();
            is.close();
            nSocket.close();
            nserverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void sendFile() throws IOException {
		Enumeration<String> keys = map.keys();
		ServerSocket nServerSocket = new ServerSocket(9999);
		while(keys.hasMoreElements()) {
			String idKey = keys.nextElement();
			PrintWriter pw = map.get(idKey);
			pw.println("/f");
			pw.flush();
			Socket nSocket = nServerSocket.accept();
			InputStream fis = new FileInputStream("server_test" + (x-1) + ".txt");
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
			nSocket.close();
		}
		nServerSocket.close();
	}
	
	public void run() {
		while(true) {
			try {
				String line = br.readLine();
				if(line != null) {
					if(line.equals("quit")) {
						line = userId + "이 채팅방을 나갔습니다.";
						sendMessage(line);
						map.remove(userId);
						map2.remove(userId);
					}
					else if(line.equals("/f")) {
						receiveFile();
						sendFile();
					}
					else {
						sendMessage(line);
					}
				}
				else {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		// 서버 - ServerSocket -> accept -> Socket
		BufferedReader br = null;
		ServerSocket serverSocket = null;
		InputStream in = null;
		OutputStream out = null;
		
		try {
			serverSocket = new ServerSocket(9990);
			System.out.println("클라이언트의 연결 대기 중...");
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("클라이언트 연결 됨.");
				// 클라이언트 메세지 받기
				in = socket.getInputStream();
				out = socket.getOutputStream();
//				br = new BufferedReader(new InputStreamReader(in));
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String userId = br.readLine();
				System.out.println(userId + "님이 접속 됨.");
				PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
				
//				map.put(userId, pw);
				
				// 브로드 케스트 준비
				ChatServer server = new ChatServer(userId, br, pw, in, socket);
				server.start();
			}
		} catch (IOException e) {
			System.out.println("채팅 중 오류 발생!");
		}
		

	}

}
