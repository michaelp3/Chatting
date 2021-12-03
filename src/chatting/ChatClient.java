package chatting;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket; 
import java.util.Scanner;

public class ChatClient extends Thread {
	static Scanner scan = new Scanner(System.in);
	BufferedReader br;
	String id;
	boolean exit = false;
	
	public ChatClient(BufferedReader br) {
		this.br = br;
	}
	
	public void run() {
		while (!exit) {
			if (br != null) {
				try {
					String line = br.readLine();
					System.out.println(line);
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
			String inMessage = br.readLine();
			System.out.println(inMessage);
			
			//메세지 수신 대기
			ChatClient client = new ChatClient(br);
			client.start();
			
			while(true) {
				String line = scan.nextLine();
				if(line.equals("quit")) {
					pw.println(args[0] + "이 채팅방을 나갔습니다.");
					pw.flush();
					break;
				}
				System.out.println(line);
				pw.println(args[0] + ": " + line);
				pw.flush();
			}
			client.end();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
