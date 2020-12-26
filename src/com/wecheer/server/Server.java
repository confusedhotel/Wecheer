package com.wecheer.server;

import java.io.*;
import java.util.*;

import com.wecheer.simpleprotx.Spx;

import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class Server extends Thread implements Spx{
	private static boolean StartServer = true;
	private static Vector<PrintWriter> PrintWriterRecord = new Vector<PrintWriter>();
	private static Vector<String> UnameOn = new Vector<String>();
	private static Map <String, PrintWriter> Mouth = new HashMap<String, PrintWriter>();
	private static Map <PrintWriter, String> UserOn = new HashMap<PrintWriter, String>();
	public static final transient Object NotBusy = new Object();
	public static final transient Object Semaphore = new Object();
	
	private Socket socket;
	private Boolean isRunning = false;

	public Server(Socket ClientSocket) {
		this.isRunning = true;
		this.socket = ClientSocket;
	}
	
	public void send(PrintWriter output, String str) {
		synchronized(Server.NotBusy) {
			output.println(str);
			output.flush();
		}
	}
	
	public void addUser(String uid, String uname, PrintWriter pw) {
		synchronized(Server.Semaphore) {
			Mouth.put(uid, pw);
			UserOn.put(pw, uid);
			UnameOn.add(uname);
			PrintWriterRecord.add(pw);
		}
	}
	
	public void removeUser(String uid, String uname, PrintWriter pw) {
		synchronized(Server.Semaphore) {
			Mouth.remove(uid);
			PrintWriterRecord.remove(pw);
			UnameOn.removeElement(uname);
			UserOn.remove(pw);
		}
	}

	public void broadcast(String str) throws IOException {
		for (int i = 0; i < PrintWriterRecord.size(); i++) {
			PrintWriter pw = PrintWriterRecord.get(i);
			send(pw, PUBLIC_MESSAGE + str);
		}
	}
	
	public String lookForOnlineUsers(String currentuser) {
		String others = currentuser;
		for (int i = 0; i < UnameOn.size(); i++) {
			String other = UnameOn.get(i);
			if (null == other || other.equals(currentuser))
				continue;
			others = others + _SEMICOLON + other;
		}
		return others;
	}
	
	public void broadcastOnlineUsers(String str) throws IOException {
		for (int i = 0; i < PrintWriterRecord.size(); i++) {
			PrintWriter pw = PrintWriterRecord.get(i);
			send(pw, PUBLIC_USERS_ONLINE + str);
		}
	}
	
	public void lanDiscoveryBroadcast(String currentuser, String comego) {
		try {
			broadcast(currentuser + comego + "。 总在线人数：" + PrintWriterRecord.size());
		} catch (IOException e1) {}
		String others = "......";
		for (int i = 0; i < UnameOn.size(); i++) {
			String other = UnameOn.get(i);
			if (comego.equalsIgnoreCase(_COME))
				others = others + _SEMICOLON + other;
			else if (!other.equals(currentuser))
				others = others + _SEMICOLON + other;
		}
		try {
			broadcastOnlineUsers(others);
		} catch (IOException e) {}
	}

	public void run() {
		try {
			Connection conn = null; 
			try { 
				Class.forName ("com.mysql.jdbc.Driver"); 
				conn = DriverManager.getConnection(
						WECHEER_DATABASE_NAME, 
						WECHEER_DATABASE_ROOT_NAME, 
						WECHEER_DATABASE_ROOT_KEY
				);
			} catch (ClassNotFoundException e) {} catch (SQLException se) {}
			System.out.println(socket.toString());
			final BufferedReader currentbr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			final PrintWriter currentpw = new PrintWriter(socket.getOutputStream());
			
			final String connapply = currentbr.readLine();
			final String userinfo = new String(connapply);
			final String applyprefix = connapply.length() <= 9 ? connapply : connapply.substring(0, 10);
			if (applyprefix.equals(SIGN_UP)) {
				final String newinfo = connapply.length() <= 9 ? connapply : connapply.substring(10, connapply.length());
				String[] strs = newinfo.split(_COMMA);
				final String currentuid = strs[0];
				final String currentuser = strs[1];
				final String currentcode = strs[2];
				PreparedStatement p = conn.prepareStatement(JDBC_INSERT_USERINFO);
				p.clearParameters();
				p.setString(1, currentuid);
				p.setString(2, currentuser);
				p.setString(3, currentcode);
				p.executeUpdate();
				p.close();
			} // if SIGN_UP
			else {
				final String currentuid = userinfo.length() <= 9 ? userinfo : userinfo.substring(0, 10);
				final String currentcode = userinfo.substring(currentuid.length(), userinfo.length());
				String refuser = "0000000000";
				String refcode = "0000000000";
				PreparedStatement p0 = conn.prepareStatement(JDBC_UID_EXISTS);
				p0.clearParameters();
				p0.setString(1, currentuid);
				ResultSet r0 = p0.executeQuery();
				if (r0.next()) {
					refuser = r0.getString(1);
					p0 = conn.prepareStatement(JDBC_CORRESPONDED_KEY);
					p0.clearParameters();
					p0.setString(1, refuser);
					ResultSet r1 = p0.executeQuery();
					if (r1.next()) {
						refcode = r1.getString(1);
						if (currentcode.equals(refcode)) {
							while (UserOn.containsValue(currentuid)) {
								PrintWriter exprintwriter = Mouth.get(currentuid);
								send(exprintwriter, null);
								PreparedStatement p = conn.prepareStatement(JDBC_WHOAMI);
								p.setString(1, currentuid);
								ResultSet r = p.executeQuery();
								String formeruser = "";
								if (r.next())
									formeruser = r.getString(1);
								removeUser(currentuid, formeruser, exprintwriter);
							}
							send(currentpw, "portnum" + Integer.toString(socket.getLocalPort()));
						} else send(currentpw, "密码错误");
					}
				} else send(currentpw, "未注册");
			
				String ulookup = currentbr.readLine();
				String currentuser = "";
				if (ulookup.equalsIgnoreCase(WHO_AM_I)) {
					PreparedStatement p = conn.prepareStatement(JDBC_WHOAMI);
					p.setString(1, currentuid);
					ResultSet r = p.executeQuery();
					if (r.next())
						currentuser = r.getString(1);
					addUser(currentuid, currentuser, currentpw);
					send(currentpw, currentuser + _QMARK + lookForOnlineUsers(currentuser));
				}
			
				ulookup = currentbr.readLine();
				if (ulookup.equalsIgnoreCase(WHO_ARE_MY_FRIENDS)) {
					PreparedStatement p = conn.prepareStatement(JDBC_WHOARE_MYFRIENDS);
					p.clearParameters();
					p.setString(1, currentuid);
					ResultSet r11 = p.executeQuery();
					String friends = "";
					while (r11.next()) {
						friends = friends + r11.getString(1) + _COMMA + r11.getString(2) + _SEMICOLON;
					}
					send(currentpw, friends);
				}
			
				lanDiscoveryBroadcast(currentuser, "来了");
    		
				/* 聊天转发 */
				while (Server.StartServer && this.isRunning) {
					// readline
					String str = null;
					try {
						str = currentbr.readLine();
					} catch (IOException e) {
						this.isRunning = false;
						continue;
					} catch (NullPointerException n) {
						this.isRunning = false;
						continue;
					}
					final String tstr = str;
					final String prefix = tstr.length() < 10 ? "illegal" : tstr.substring(0, 10);
				
					// 转发公共消息
					if (prefix.equals(PUBLIC_MESSAGE)) {
						String bodies = tstr.substring(10, tstr.length());
						synchronized (Server.NotBusy) {
							try {
								broadcast(currentuser + "：" + bodies);
							} catch (IOException e) {}
							System.out.println(currentuser + "：" + bodies);
						}
					}
				
					// 转发建立私聊请求
					else if (prefix.equals(PRIVATE_CONNECTION_REQUEST)) {
						String targetuid = tstr.substring(10, tstr.length());
						if (targetuid.startsWith("暂时没有好友")) {
							send(currentpw, PRIVATE_PARTY_OFFLINE + "你暂时还没有好友。快去添加好友吧。");
						}
						else if (Mouth.containsKey(targetuid)) {
							synchronized (Server.NotBusy) {
								PrintWriter targetpw = Mouth.get(targetuid);
								send(targetpw, PRIVATE_CONNECTION_ENQUEST + currentuid);
								send(currentpw, PRIVATE_CONNECTION_ENQUEST + targetuid);
							}
							System.out.println(currentuser + " and "  + targetuid + " has connected.");
						}
						else {
							send(currentpw, PRIVATE_PARTY_OFFLINE + "对方不在线");
						}
					}
				
					// 转发私人消息
					else if (prefix.equals(PRIVATE_MESSAGE)) {
						String targetuid = tstr.substring(10, 20);
						String bodies = tstr.substring(20, tstr.length());
						if (Mouth.containsKey(targetuid)) {
							synchronized (Server.NotBusy) {
								PrintWriter targetpw = Mouth.get(targetuid);
								send(targetpw, PRIVATE_MESSAGE + currentuid + currentuser + "：" + bodies);
							}
							System.out.println(currentuser + " privately talked to " + targetuid + "：" + bodies);
						}
					}
				
					else if (prefix.equals(PRIVATE_PICTURE)) {
						String targetuid = tstr.substring(10, 20);
						String bodies = tstr.substring(20, tstr.length());
						if (Mouth.containsKey(targetuid)) {
							synchronized (Server.NotBusy) {
								PrintWriter targetpw = Mouth.get(targetuid);
								send(targetpw, PRIVATE_PICTURE + currentuid + bodies);
							}
							System.out.println(currentuser + " privately sent a picture to " + targetuid);
						}
					}
				
					// 转发结束私聊请求
					else if (prefix.equals(PRIVATE_DISCONNECTION_REQUEST)) {
						String targetuid = tstr.substring(10, 20);
						if (Mouth.containsKey(targetuid)) {
							synchronized (Server.NotBusy) {
								PrintWriter targetpw = Mouth.get(targetuid);
								send(targetpw, PRIVATE_DISCONNECTION_ENQUEST + currentuid);
							}
						}
						System.out.println(currentuser + " has disconnected to " + targetuid);
					}
				}
				removeUser(currentuid, currentuser, currentpw);
				lanDiscoveryBroadcast(currentuser, _LEAVE);
				currentpw.close();
				currentbr.close();
				socket.close();
			} // else SIGN_IN
		} catch (IOException ex) {} catch (SQLException e) {}
	} // Run user message processing thread

	public static void main(String[] args) throws IOException {
		ServerSocket ServerSocket = new ServerSocket((args.length >= 1) ? Integer.parseInt(args[0]) : 9102);
		KeyboardListener keyboardlistener = new KeyboardListener(PrintWriterRecord);
		keyboardlistener.start();
		System.out.println("服务器已启动");
		while (StartServer) {
			Socket socket = ServerSocket.accept();
			Server server = new Server(socket);
			server.start();
		}
		ServerSocket.close();
	}
}

final class KeyboardListener extends Thread {
	boolean StartKeyboardInput = true;
	private Vector<PrintWriter> PrintWriterRecord;

	public KeyboardListener(Vector<PrintWriter> PrintWriterRecord) {
		this.PrintWriterRecord = PrintWriterRecord;
	}

	public void run() {
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in));
		while (StartKeyboardInput) {
			String str = null;
			try {
				str = bufferedreader.readLine();
				synchronized(Server.NotBusy) {
					for (int i = 0; i < PrintWriterRecord.size(); i++) {
						PrintWriter printwriter = PrintWriterRecord.get(i);
						printwriter.println("*&pubmsg&*" + "Server：" + str);
						printwriter.flush();
					}
				}
			} catch (IOException e) {}
		}
	}
}