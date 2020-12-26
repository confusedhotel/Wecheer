package com.wecheer.client;

import java.io.*;
import java.util.*;
import java.util.Base64.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.awt.*;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.wecheer.simpleprot.Sp;

/**
 * Chatter���������û�cud���ҷ������û�uid���Է���֮����ڵ�Ψһ˽��
 * @author  Aiden
 * @version 1.0 Alpha
 * @since   jdk-10.0.2
 */

public class Chatter implements Sp{
	/* ����� */
    private static final transient Object lock = new Object();
    private static Vector<Chatter> instances = new Vector<Chatter>(); // ��ǰ�����е�˽�Ķ�������
    
    /* ��Ա���� */
    private byte[] buf = new byte[4096001]; // �����ļ���С����Ϊ4MB
    private Boolean isActivated = false;    // �����Ƿ񱻼���
    private String uid;                     // �Է�id
    private String cud;                     // �ҷ�id
    
    private JFrame mainframe;
    private JTextPane pane;
    private JTextPane mypane;
    private JScrollPane scrollpane;
    private JScrollPane scrollmypane;
    private JButton insertbutton;
    private JButton sendbutton;
    
    /* ˽�й��췽�� */
    private Chatter(String targetuid, String currentuid, Socket socket) {
    	this.uid = targetuid;
    	this.cud = currentuid;
    	this.isActivated = true; // һ���ɹ�����˽�ĺ�˫��������˽�Ĵ��ڲ��ܻ�����Ϣ
    	
    	JFrame.setDefaultLookAndFeelDecorated(true);
    	this.mainframe = new JFrame(targetuid);
    	this.mainframe.setSize(600, 800);		
    	this.mainframe.setDefaultCloseOperation(1);
    	this.mainframe.setLocationRelativeTo(null);	
    	this.mainframe.setResizable(false);
    	this.mainframe.setLayout(null);
    	this.mainframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	StyleContext sc = new StyleContext();
    	DefaultStyledDocument doc = new DefaultStyledDocument(sc);
    	this.pane = new JTextPane(doc);  // ������
    	this.pane.setEditable(false);  
    	this.mypane = new JTextPane();   // ������
    	this.scrollpane = new JScrollPane(pane); 
    	this.scrollpane.setBounds(5, 5, 578, 400);
    	this.scrollmypane = new JScrollPane(mypane); 
    	this.scrollmypane.setBounds(5, 420, 578, 300); 
    	this.insertbutton = new JButton("ѡ��ͼƬ������"); 
    	this.insertbutton.setBounds(160, 725, 150, 35); 
    	this.sendbutton = new JButton("����"); 
    	this.sendbutton.setBounds(320, 725, 150, 35);
    	this.mainframe.add(scrollpane); 
    	this.mainframe.add(insertbutton); 
    	this.mainframe.add(scrollmypane); 
    	this.mainframe.add(sendbutton);  
    	this.mainframe.setVisible(true);
    	this.mainframe.setDefaultCloseOperation(2);
    	
    /***************************************************************************************************/
    /*************************************** ���ʹ���ͽ������ ****************************************/
    	// ����˽���ı���UI��ʾ
    	this.sendbutton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) { 
        		SwingUtilities.invokeLater(new Runnable() {
                	@Override
                	public void run() {
                		appendHText();
                	}
                });
                PrintWriter output = null;
                try {
					output = new PrintWriter(socket.getOutputStream());
					output.println(PRIVATE_MESSAGE + targetuid + mypane.getText());
	                output.flush();
                } catch (IOException e3) {}
            }
        });
        
    	// ����˽��ͼƬ��UI��ʾ
    	// Ϊ�������һ��ѡ���˲����ͼ����������ͣ��������ݴ��ڷ�����
    	this.insertbutton.addActionListener(new ActionListener(){
        	@Override
            public void actionPerformed(ActionEvent e) {
        		// ��ͼƬ�ļ�
        	    FileNameExtensionFilter fltr = new FileNameExtensionFilter("JPG & GIF Images", "jpg", "png");
                JFileChooser jfc = new JFileChooser(); 
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setFileFilter(fltr);
                jfc.showOpenDialog(mainframe);
                SwingUtilities.invokeLater(new Runnable() {
                	@Override
                	public void run() {
                		Document doc = pane.getStyledDocument();
                		SimpleAttributeSet set = new SimpleAttributeSet();
                		try {
							doc.insertString(doc.getLength(), "\n�ң�", set);
						} catch (BadLocationException e) {}
                		pane.insertIcon(new ImageIcon(jfc.getSelectedFile().toString()));
                	}
                });
                File src = new File(jfc.getSelectedFile().toString());
                // ��ͼƬ�ļ������ֽ�����buf
                FileInputStream fin = null;
                try {
                	fin = new FileInputStream(src);
				} catch (FileNotFoundException e1) {}
                int count = 0;
                Boolean isExceeded = false;
                try {
                	int bytes = fin.read();
                	while(bytes != -1){
                		buf[count++] = (byte)bytes;
                		bytes = fin.read();
                		if (count > 4096000) {
                			JOptionPane.showMessageDialog(null, "�ļ���С���ܳ���4MB");
                			isExceeded = true;
                			break;
                		}
                	}
                } catch (IOException e5) {}  
                if (!isExceeded) {
                // ��buf����Ч�غɱ�����ַ���
                	Encoder encoder = Base64.getEncoder();
                	byte[] imgbuf = new byte[count];
                	for (int i = 0; i < count; i++)
                		imgbuf[i] = buf[i];
                	String str = encoder.encodeToString(imgbuf);
                // ͨ��socket�����ַ���
                	try {
                		PrintWriter pw = new PrintWriter(socket.getOutputStream());
                		pw.println(PRIVATE_PICTURE + targetuid + str);
                		pw.flush();
                	} catch (IOException e1) {}  
                }
            } 
        });
    	
    /************************************* ���ʹ���ͽ������ *******************************************/
    /****************************************************************************************************/
    }
    
    
    
    /*****************************************************************************************************/
    /**************************************** ���մ���ͽ������ *****************************************/
    /* �յ�˽���ı� */
    public void rcvtext(String str) {
    	if (null != this && this.isActivated) {
    		appendHText(str);
    	}
    }
    
    /* �յ�˽��ͼƬ */
    // �����յ�ͼƬд��Ӳ�̣�����������׷���յ���ͼƬ */
    public void rcvicon(String str) {
    	if (null != this && this.isActivated) {
    		System.out.println(Integer.toString(str.length()));
    		// ��������ļ���
    		Calendar era = Calendar.getInstance();
    		String curtime = String.valueOf(era.get(Calendar.DATE)) + "_" + String.valueOf(era.get(Calendar.MONTH) + 1) + "_"
    		               + String.valueOf(era.get(Calendar.YEAR)) + "_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
    		curtime = curtime.length() > 233 ? curtime.substring(0, 233) : curtime;
    		File dir = new File(WECHEERFILESDIR + this.cud);
    		if (!dir.exists())
    			dir.mkdirs();
    		String filename = dir + "\\" + curtime;
    		// �����ͨ���ֽ�����rcvbuf���ͼ��
    		FileOutputStream fos = null;
    		Decoder decoder = Base64.getDecoder();
            byte[] rcvbuf = decoder.decode(str);
            // ͨ���ֽ���д��ͼ���ļ�
    		try {
    			fos = new FileOutputStream(filename, false);
			} catch (FileNotFoundException e1) {}
    		try {
    			fos.write(rcvbuf, 0, rcvbuf.length);
			} catch (IOException e1) {}
    		// ��ʾ�յ���˽��ͼƬ
    		Document doc = this.pane.getStyledDocument();
    		SimpleAttributeSet set = new SimpleAttributeSet();
    		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy��MM��dd�� HH:mm:ss");
    		LocalDateTime ldt = LocalDateTime.now();
            SwingUtilities.invokeLater(new Runnable() {
            	@Override
            	public void run() {
            		try {
    	    			doc.insertString(doc.getLength(), "\n\n" + ldt.format(dtf), set);
    	    			doc.insertString(doc.getLength(), "\n" + uid + "��", set);
    	    		} catch (BadLocationException e1) {}
            		pane.insertIcon(new ImageIcon(filename));
            		try {
    	    			doc.insertString(doc.getLength(), "\n", set);
            		} catch (BadLocationException e2) {}
            	}
            });
    	}
    }
    
    /* ��������׷�ӷ��͵��ı� */
 	private void appendHText() {
 		Document doc = this.pane.getStyledDocument();
 		Document about2send = this.mypane.getStyledDocument();
		SimpleAttributeSet set = new SimpleAttributeSet();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy��MM��dd�� HH:mm:ss");
		LocalDateTime ldt = LocalDateTime.now();
		try {
			doc.insertString(doc.getLength(), "\n" + ldt.format(dtf), set);
			doc.insertString(doc.getLength(), "\n�ң�", set);
		} catch (BadLocationException e1) {}
 		for (int i = 0; i < doc.getLength(); i++) { 
 			try {
				doc.insertString(doc.getLength(), about2send.getText(i, 1), set);
			} catch (BadLocationException e) {}
 		}
 		mypane.setText("");
 	}
    
 	/* ��������׷���յ����ı� */
    public void appendHText(String str) {
    	Document doc = this.pane.getStyledDocument();
    	SimpleAttributeSet set = new SimpleAttributeSet();
    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy��MM��dd�� HH:mm:ss");
    	LocalDateTime ldt = LocalDateTime.now();
    	SwingUtilities.invokeLater(new Runnable() {
    		@Override
    		public void run() {
    			try {
    	   			doc.insertString(doc.getLength(), "\n\n" + ldt.format(dtf), set);
    	   			doc.insertString(doc.getLength(), "\n" + uid + "��" + str, set);
    	   		} catch (BadLocationException e1) {}
    		}	
    	});
    }
    
    /**************************************** ���մ���ͽ������  ****************************************/
    /*****************************************************************************************************/
    
    
    
    
    /*****************************************************************************************************/
    /*********************************************** ˽���߼� ********************************************/
    /* ����˽������״̬��ȡChatter˽�ĻỰʵ��������*/
    // ͬһʱ�̽������û�A���û�B��һ��˽�Ķ������
    public static Chatter getInstance(String targetuid, String currentuid, Socket socket) {
		synchronized(lock) {
			for (int i = 0; i < Chatter.instances.size(); i++) {
				// ˫����δ�뿪
				// ���ر�����ǰʵ��
				if ((instances.get(i).uid).equals(targetuid) && instances.get(i).isActivated)
					return instances.get(i);
				
				// �Է��뿪��ǰ�Ự�����·��𣨽��룩��ǰ�Ự
				// �����ҷ���ǰʵ��������
				else if ((instances.get(i).uid).equals(targetuid) && !instances.get(i).isActivated) {
					enableInstance(instances.get(i));
					return instances.get(i);
				}
			}
		}
		
		// ���򴴽��µ�Chatter˽�ĻỰʵ��������
		Chatter instance = new Chatter(targetuid, currentuid, socket);
		// Ϊ��˽��ʵ����ӷ��ͽ���˽��������
		instance.mainframe.addWindowListener(new WindowAdapter() {  
    		@Override
    		public void windowClosing(WindowEvent e) {  
    			synchronized(lock) {
    	    		instances.removeElement(instance);
    	    	}
    			PrintWriter output = null;
    			if (instance.isActivated) try {
					output = new PrintWriter(socket.getOutputStream());
					output.println("!^~quit~^!" + targetuid);
	                output.flush();
                } catch (IOException e3) {}
    			super.windowClosing(e);
    		}    
    	});
		// ����˽��ʵ�����뱾���Ľ����е�˽��ʵ��Vector
		synchronized(lock) {
			instances.addElement(instance);
		}
    	return instance;
    }
    
    /* �Է����˳���ǰ˽�ģ���ʧЧ�ҷ�˽��ʵ�������ҷ�������ͨ����ǰ˽�Ĵ��ڷ���Ϣ */
    // ��֮ǰ�������¼�����ҷ�JTextPane��
    public static String disableInstance(Chatter instance) {
    	if (instance.isActivated) {
    		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				instance.sendbutton.setEnabled(false);
    				instance.sendbutton.setText("�Է��뿪��");
    			}
    		});
    		instance.isActivated = false;
    	}

    	return "Current Chatter instance has been expired!";
    }
    
    /* �Է����ص���ǰ˽�ģ��򼤻��˽��ʵ����ǰ�����ҷ�û�йرյ�ǰ˽�Ĵ��ڣ�*/
    // ������������ͨ����ǰ˽�Ĵ��ڷ���Ϣ����Ϊ�ҷ��Գ��е�ǰ˽�Ķ��󣩣���֮ǰ�������¼���ڱ���JTextPane��
    // ���Է��ĵ�ǰ˽�Ĵ������µģ���Ϊ�Է��ر�˽�Ĵ����ٴ򿪣�����ʵ�������µ�Chatter����
    public static String enableInstance(Chatter instance) {
    	if (!instance.isActivated) {
    		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				instance.sendbutton.setEnabled(true);
    				instance.sendbutton.setText("����");
    			}
    		});
    		instance.isActivated = true;
    	}

    	return "Current Chatter instance has been enabled!";
    }
    /******************************************** ˽���߼� ***********************************************/
    /*****************************************************************************************************/

} 
