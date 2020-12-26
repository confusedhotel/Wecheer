package com.wecheer.client;

import java.sql.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;

import com.wecheer.simpleprot.Sp;

final class NumberTextField extends PlainDocument {  /**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	// 用于限制注册的ID只能为纯数字；与使用正则表达式方案的效果等价。
    public NumberTextField() {
        super();
    }
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }
        char[] s = str.toCharArray();
        int length = 0;
        for (int i = 0; i < s.length; i++)
            if ((s[i] >= '0') && (s[i] <= '9'))
                s[length++] = s[i];
        super.insertString(offset, new String(s, 0, length), attr);
    }
}

public class Client implements ActionListener, Sp {
	private static Boolean isStarted = true;
	private static Boolean isUnique = true;
	private static Socket socket;
	public static final transient Object Semaphore = new Object();
	
	private JTextField jt;
	private JPasswordField jp;
	private JFrame jf;
	private Boolean signin;
	private Boolean isDirty = false;
	
	public Client(JTextField jt, JPasswordField jp, JFrame jf, boolean signin) {		
		this.jt = jt;
		this.jp = jp;
		this.jf = jf;
		this.signin = signin;
		synchronized(Semaphore) {
			if (isUnique) try {
				isUnique = false;
				InetAddress IPServer = InetAddress.getLocalHost(); // 模拟条件下服务器和客户端位于同一主机
				socket = new Socket(IPServer, 9102);
			} catch (UnknownHostException e) {} catch (IOException e) {}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		try { 
			showDialog(jt, jp, jf); 
		} 
		catch (UnknownHostException e1) {
			JOptionPane.showMessageDialog(null, "The IP address of the host could not be determined!", "登陆", JOptionPane.ERROR_MESSAGE);
		} 
		catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "客户端错误", "登陆", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void showDialog(JTextField jt, JPasswordField jp, JFrame jf) throws IOException {
		/* 连接建立 连接保持 连接断开 */
		while (this.signin) {		
			// 客户端请求建立与服务器的连接
			final String currentuid = jt.getText();
			final String currentcode = new String (jp.getPassword());
			final String connapply = currentuid + currentcode;
			if (currentuid.length() == UID_LENGTH && currentcode.length() > 0) {
				PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
				printwriter.println(connapply);
				printwriter.flush();
			}
			else {
				JOptionPane.showMessageDialog(null, "请输入正确的身份信息！", "登录", JOptionPane.ERROR_MESSAGE);
				break;
			}
			String allowance = null;
			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// 服务器返回身份信息有效性
			allowance = br.readLine();
			if (null != allowance && allowance.startsWith("未")) {
				JOptionPane.showMessageDialog(null, "未注册", "登录", JOptionPane.ERROR_MESSAGE);
				jf.setVisible(false);
				jf.dispose();  
				break;
			}
			else if (null != allowance && allowance.startsWith("密")) {
				JOptionPane.showMessageDialog(null, "密码错误", "登录", JOptionPane.ERROR_MESSAGE);
				jf.setVisible(false);
				jf.dispose();  
				break;
			}
			else /*if (null != allowance && allowance.startsWith("portnum"))*/ {
				PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
				printwriter.println(WHO_AM_I);
				printwriter.flush();
				
				// 初始化界面和在线用户列表
				final String online = br.readLine();
				String[] users = online.split("\\?", 2);
				final String currentuser = users[0];
				final String otherusers = users[1];
				String[] others = otherusers.split(";");
				JFrame mainframe = new JFrame();
				mainframe.setTitle("计算机网络 张超凡 学号2020244181");		
				mainframe.setSize(450, 700);		
				mainframe.setDefaultCloseOperation(3);
				mainframe.setLocationRelativeTo(null);	
				mainframe.setResizable(false);		
				mainframe.setLayout(new BorderLayout());
				final JTextArea textarea = new JTextArea();
				textarea.setEditable(false);
				textarea.setLineWrap(true);
				textarea.setWrapStyleWord(true);
				JScrollPane scrollpane = new JScrollPane(textarea);
				JTextField textfield = new JTextField(20);
				DefaultComboBoxModel<String> combomodel = new DefaultComboBoxModel<>(others);
				JComboBox<String> combobox = new JComboBox<String>(combomodel);
				combobox.setEditable(false);
				JButton sendpubmsg = new JButton();
				sendpubmsg.setText("发送"); 
				JButton startchatting = new JButton();
				startchatting.setText("发起会话");
				JButton addafriend = new JButton();
				addafriend.setText("添加好友");
				JPanel panel1 = new JPanel();
				JPanel panel2 = new JPanel();
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				JPanel panelc = new JPanel();
				panelc.setLayout(new BorderLayout());
				jf.setVisible(false);
				jf.dispose();
				Vector<Vector<String> > Dataset = new Vector<>();
				Vector<String> colomn = new Vector<>();
				colomn.add("徽信号");
				colomn.add("徽信名");
				printwriter.println(WHO_ARE_MY_FRIENDS);
				printwriter.flush();
				
				// 初始化好友列表
				final String friends = br.readLine();
				String[] friendslist = friends.split(_SEMICOLON);
				for (int i = 0; i < friendslist.length; i++) {
					Vector<String> TableData = new Vector<>();
					if (friendslist[0].equalsIgnoreCase("")) {
						TableData.add("暂时没有好友");
						TableData.add("暂时没有好友");
					}
					else {
						String[] friend = friendslist[i].split(_COMMA);
						TableData.add(friend[0]);
						TableData.add(friend[1]);
					}
					Dataset.add(TableData);
				}
				DefaultTableModel model = new DefaultTableModel() {
					@Override
					public boolean isCellEditable(int row, int colomn) {
						return false;
					}
				};
				model.setDataVector(Dataset, colomn);
				JTable table = new JTable(model);
				JScrollPane scrollpane1 = new JScrollPane(table);
						
				// 发送公共消息功能
				sendpubmsg.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						new PublicMsg(textfield, textarea, currentuser, socket, true).execute();
					}
				});
					
				// 发送建立私聊请求功能
				startchatting.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						String targetuid = (String) table.getValueAt(table.getSelectedRow(), 0);
						PrintWriter pw = null;
						try {
							pw = new PrintWriter(socket.getOutputStream());
							pw.println(PRIVATE_CONNECTION_REQUEST + targetuid);
							pw.flush();
						} catch (IOException e) {}
					}
				});
				
				// 添加好友功能
				addafriend.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO：向服务器发送添加好友请求
						
					}
				});
						
				// 保持连接：接收公共消息和私聊线程
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (isStarted) {
							String str = null;
							BufferedReader br = null;
							try {
								br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								str = br.readLine();
							} 
							catch (IOException e) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										jf.setVisible(false);
										JOptionPane.showMessageDialog(null, "客户端出错");
									}
								});
								isStarted = false;
								continue;
							} 
							catch (NullPointerException e) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										jf.setVisible(false);
										mainframe.setVisible(false);
										mainframe.dispose();
										JOptionPane.showMessageDialog(null, "服务器出错");
									}
								});
								isStarted = false;
								continue;
							}
							final String tstr = str;
							try {
								@SuppressWarnings("unused")
								final String svroff = tstr.substring(0, 10);
							} 
							catch (NullPointerException e0) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										jf.setVisible(false);
										jf.dispose();
										JOptionPane.showMessageDialog(null, "服务器已下线");
									}
								});
								isStarted = false;
								continue;
							}
							catch (StringIndexOutOfBoundsException e1) {
								mainframe.setVisible(false);
								mainframe.dispose();
								JOptionPane.showMessageDialog(null, "你已下线");
								isStarted = false;
								isDirty = true;
								continue;
							}
							final String prefix = tstr.substring(0, 10);
							final String targetuid = tstr.substring(10, tstr.length());
							final String targetuid1 = targetuid.length() < 10 ? targetuid : targetuid.substring(0, 10);
							final String img = targetuid.length() < 20 ? targetuid : targetuid.substring(10, targetuid.length());
									
							// 接收公共消息
							if (prefix.equals(PUBLIC_MESSAGE)) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										textarea.append("\n" + targetuid);
									}
								});
							}
							else if (prefix.equals(PRIVATE_PARTY_OFFLINE)) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										JOptionPane.showMessageDialog(null, targetuid);
									}
								});
							}
							else if (prefix.equals(PUBLIC_USERS_ONLINE)) {
								String[] unameonpu = targetuid.split(_SEMICOLON);
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										combomodel.removeAllElements();
										for (int i = 0; i < unameonpu.length; i++)
											combomodel.addElement(unameonpu[i]);
										combobox.setModel(combomodel);
									}
								});
							}
									
							// 接收私聊消息
							else {
								if (prefix.equals(PRIVATE_CONNECTION_ENQUEST)) {
									Chatter private_chatter = Chatter.getInstance(targetuid, currentuid, socket);
								}
								else {
									Chatter private_chatter = Chatter.getInstance(targetuid1, currentuid, socket);
									if (prefix.equals(PRIVATE_DISCONNECTION_ENQUEST)) {
										Chatter.disableInstance(private_chatter);
									}
									else if (prefix.equals(PRIVATE_MESSAGE)) {
										private_chatter.appendHText(targetuid);
									}
									else if (prefix.equals(PRIVATE_PICTURE)) {
										private_chatter.rcvicon(img);
									}
								}
							}
						}
					}
				}).start();
				
				// 防止重复登录
				if (isDirty) {
					isDirty = false;
				}
				else {
					panelc.add(scrollpane1);
					panel1.add(combobox);
					panel1.add(textfield);
					panel1.add(sendpubmsg);
					panel2.add(startchatting);
					panel2.add(addafriend);
					panel.add(scrollpane);
					panel.add(panel1, BorderLayout.SOUTH);
					panelc.add(panel2, BorderLayout.SOUTH);
					JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
					tabbedPane.addTab("公共聊天室", null, panel, null);
					tabbedPane.addTab("好友列表", null, panelc, null);
					mainframe.add(tabbedPane);
					mainframe.setVisible(true);
				}
				
				break; 
			} // while signin
		} // 公共聊天
		
		/* 注册 */
		if (!this.signin) {
			JFrame mainframe = new JFrame();
			mainframe.setTitle("注册");		
			mainframe.setSize(350, 300);		
			mainframe.setDefaultCloseOperation(3);
			mainframe.setLocationRelativeTo(null);	
			mainframe.setResizable(false);		
			mainframe.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 30));
			JLabel label0 = new JLabel("徽信ID:");	
			JTextField jt0 = new JTextField();
			jt0.setDocument(new NumberTextField());
			jt0.setPreferredSize(new Dimension(250, 30));
			JLabel label1= new JLabel("昵称:    ");
			JTextField jt1 = new JTextField();
			jt1.setPreferredSize(new Dimension(250, 30));
			JLabel label2 = new JLabel("密码:    ");
			JTextField jt2 = new JTextField();
			jt2.setPreferredSize(new Dimension(250, 30));
			JButton jb = new JButton("注册");
			mainframe.add(label0);
			mainframe.add(jt0);
			mainframe.add(label1);
			mainframe.add(jt1);
			mainframe.add(label2);
			mainframe.add(jt2);
			mainframe.add(jb);
			mainframe.setVisible(true);
			
			// 将注册身份信息发送给服务器
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (jt0.getText().length() < UID_LENGTH 
				     || jt0.getText().length() > UID_LENGTH
				     || jt1.getText().length() < 1
				     || jt1.getText().length() > 64
				     || jt1.getText().equalsIgnoreCase("我")
				     || jt1.getText().equalsIgnoreCase("暂时没有好友")
					 || jt2.getText().length() > 64
					)
						JOptionPane.showMessageDialog(null, "必须是10位数字ID且昵称和密码不能为空", "注册", JOptionPane.ERROR_MESSAGE);
					else try {
						String str = SIGN_UP + jt0.getText() + _COMMA + jt1.getText() + _COMMA + jt2.getText();
						PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
						printwriter.println(str);
						printwriter.flush();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "客户端出错", "注册", JOptionPane.ERROR_MESSAGE);
					}
					JOptionPane.showMessageDialog(null, "请重新登录", "注册", JOptionPane.INFORMATION_MESSAGE);
					mainframe.setVisible(false);
					mainframe.dispose();
				}
			});
		}
	}
}
