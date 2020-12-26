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

	// ��������ע���IDֻ��Ϊ�����֣���ʹ��������ʽ������Ч���ȼۡ�
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
				InetAddress IPServer = InetAddress.getLocalHost(); // ģ�������·������Ϳͻ���λ��ͬһ����
				socket = new Socket(IPServer, 9102);
			} catch (UnknownHostException e) {} catch (IOException e) {}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		try { 
			showDialog(jt, jp, jf); 
		} 
		catch (UnknownHostException e1) {
			JOptionPane.showMessageDialog(null, "The IP address of the host could not be determined!", "��½", JOptionPane.ERROR_MESSAGE);
		} 
		catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "�ͻ��˴���", "��½", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void showDialog(JTextField jt, JPasswordField jp, JFrame jf) throws IOException {
		/* ���ӽ��� ���ӱ��� ���ӶϿ� */
		while (this.signin) {		
			// �ͻ����������������������
			final String currentuid = jt.getText();
			final String currentcode = new String (jp.getPassword());
			final String connapply = currentuid + currentcode;
			if (currentuid.length() == UID_LENGTH && currentcode.length() > 0) {
				PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
				printwriter.println(connapply);
				printwriter.flush();
			}
			else {
				JOptionPane.showMessageDialog(null, "��������ȷ�������Ϣ��", "��¼", JOptionPane.ERROR_MESSAGE);
				break;
			}
			String allowance = null;
			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// ���������������Ϣ��Ч��
			allowance = br.readLine();
			if (null != allowance && allowance.startsWith("δ")) {
				JOptionPane.showMessageDialog(null, "δע��", "��¼", JOptionPane.ERROR_MESSAGE);
				jf.setVisible(false);
				jf.dispose();  
				break;
			}
			else if (null != allowance && allowance.startsWith("��")) {
				JOptionPane.showMessageDialog(null, "�������", "��¼", JOptionPane.ERROR_MESSAGE);
				jf.setVisible(false);
				jf.dispose();  
				break;
			}
			else /*if (null != allowance && allowance.startsWith("portnum"))*/ {
				PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
				printwriter.println(WHO_AM_I);
				printwriter.flush();
				
				// ��ʼ������������û��б�
				final String online = br.readLine();
				String[] users = online.split("\\?", 2);
				final String currentuser = users[0];
				final String otherusers = users[1];
				String[] others = otherusers.split(";");
				JFrame mainframe = new JFrame();
				mainframe.setTitle("��������� �ų��� ѧ��2020244181");		
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
				sendpubmsg.setText("����"); 
				JButton startchatting = new JButton();
				startchatting.setText("����Ự");
				JButton addafriend = new JButton();
				addafriend.setText("��Ӻ���");
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
				colomn.add("���ź�");
				colomn.add("������");
				printwriter.println(WHO_ARE_MY_FRIENDS);
				printwriter.flush();
				
				// ��ʼ�������б�
				final String friends = br.readLine();
				String[] friendslist = friends.split(_SEMICOLON);
				for (int i = 0; i < friendslist.length; i++) {
					Vector<String> TableData = new Vector<>();
					if (friendslist[0].equalsIgnoreCase("")) {
						TableData.add("��ʱû�к���");
						TableData.add("��ʱû�к���");
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
						
				// ���͹�����Ϣ����
				sendpubmsg.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						new PublicMsg(textfield, textarea, currentuser, socket, true).execute();
					}
				});
					
				// ���ͽ���˽��������
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
				
				// ��Ӻ��ѹ���
				addafriend.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO���������������Ӻ�������
						
					}
				});
						
				// �������ӣ����չ�����Ϣ��˽���߳�
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
										JOptionPane.showMessageDialog(null, "�ͻ��˳���");
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
										JOptionPane.showMessageDialog(null, "����������");
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
										JOptionPane.showMessageDialog(null, "������������");
									}
								});
								isStarted = false;
								continue;
							}
							catch (StringIndexOutOfBoundsException e1) {
								mainframe.setVisible(false);
								mainframe.dispose();
								JOptionPane.showMessageDialog(null, "��������");
								isStarted = false;
								isDirty = true;
								continue;
							}
							final String prefix = tstr.substring(0, 10);
							final String targetuid = tstr.substring(10, tstr.length());
							final String targetuid1 = targetuid.length() < 10 ? targetuid : targetuid.substring(0, 10);
							final String img = targetuid.length() < 20 ? targetuid : targetuid.substring(10, targetuid.length());
									
							// ���չ�����Ϣ
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
									
							// ����˽����Ϣ
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
				
				// ��ֹ�ظ���¼
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
					tabbedPane.addTab("����������", null, panel, null);
					tabbedPane.addTab("�����б�", null, panelc, null);
					mainframe.add(tabbedPane);
					mainframe.setVisible(true);
				}
				
				break; 
			} // while signin
		} // ��������
		
		/* ע�� */
		if (!this.signin) {
			JFrame mainframe = new JFrame();
			mainframe.setTitle("ע��");		
			mainframe.setSize(350, 300);		
			mainframe.setDefaultCloseOperation(3);
			mainframe.setLocationRelativeTo(null);	
			mainframe.setResizable(false);		
			mainframe.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 30));
			JLabel label0 = new JLabel("����ID:");	
			JTextField jt0 = new JTextField();
			jt0.setDocument(new NumberTextField());
			jt0.setPreferredSize(new Dimension(250, 30));
			JLabel label1= new JLabel("�ǳ�:    ");
			JTextField jt1 = new JTextField();
			jt1.setPreferredSize(new Dimension(250, 30));
			JLabel label2 = new JLabel("����:    ");
			JTextField jt2 = new JTextField();
			jt2.setPreferredSize(new Dimension(250, 30));
			JButton jb = new JButton("ע��");
			mainframe.add(label0);
			mainframe.add(jt0);
			mainframe.add(label1);
			mainframe.add(jt1);
			mainframe.add(label2);
			mainframe.add(jt2);
			mainframe.add(jb);
			mainframe.setVisible(true);
			
			// ��ע�������Ϣ���͸�������
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (jt0.getText().length() < UID_LENGTH 
				     || jt0.getText().length() > UID_LENGTH
				     || jt1.getText().length() < 1
				     || jt1.getText().length() > 64
				     || jt1.getText().equalsIgnoreCase("��")
				     || jt1.getText().equalsIgnoreCase("��ʱû�к���")
					 || jt2.getText().length() > 64
					)
						JOptionPane.showMessageDialog(null, "������10λ����ID���ǳƺ����벻��Ϊ��", "ע��", JOptionPane.ERROR_MESSAGE);
					else try {
						String str = SIGN_UP + jt0.getText() + _COMMA + jt1.getText() + _COMMA + jt2.getText();
						PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
						printwriter.println(str);
						printwriter.flush();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "�ͻ��˳���", "ע��", JOptionPane.ERROR_MESSAGE);
					}
					JOptionPane.showMessageDialog(null, "�����µ�¼", "ע��", JOptionPane.INFORMATION_MESSAGE);
					mainframe.setVisible(false);
					mainframe.dispose();
				}
			});
		}
	}
}
