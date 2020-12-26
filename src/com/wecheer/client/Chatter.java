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
 * Chatter类描述了用户cud（我方）和用户uid（对方）之间存在的唯一私聊
 * @author  Aiden
 * @version 1.0 Alpha
 * @since   jdk-10.0.2
 */

public class Chatter implements Sp{
	/* 类变量 */
    private static final transient Object lock = new Object();
    private static Vector<Chatter> instances = new Vector<Chatter>(); // 当前进行中的私聊对象容器
    
    /* 成员变量 */
    private byte[] buf = new byte[4096001]; // 传输文件大小上限为4MB
    private Boolean isActivated = false;    // 窗口是否被激活
    private String uid;                     // 对方id
    private String cud;                     // 我方id
    
    private JFrame mainframe;
    private JTextPane pane;
    private JTextPane mypane;
    private JScrollPane scrollpane;
    private JScrollPane scrollmypane;
    private JButton insertbutton;
    private JButton sendbutton;
    
    /* 私有构造方法 */
    private Chatter(String targetuid, String currentuid, Socket socket) {
    	this.uid = targetuid;
    	this.cud = currentuid;
    	this.isActivated = true; // 一方成功发起私聊后，双方都弹出私聊窗口并能互发消息
    	
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
    	this.pane = new JTextPane(doc);  // 聊天区
    	this.pane.setEditable(false);  
    	this.mypane = new JTextPane();   // 发送区
    	this.scrollpane = new JScrollPane(pane); 
    	this.scrollpane.setBounds(5, 5, 578, 400);
    	this.scrollmypane = new JScrollPane(mypane); 
    	this.scrollmypane.setBounds(5, 420, 578, 300); 
    	this.insertbutton = new JButton("选择图片并发送"); 
    	this.insertbutton.setBounds(160, 725, 150, 35); 
    	this.sendbutton = new JButton("发送"); 
    	this.sendbutton.setBounds(320, 725, 150, 35);
    	this.mainframe.add(scrollpane); 
    	this.mainframe.add(insertbutton); 
    	this.mainframe.add(scrollmypane); 
    	this.mainframe.add(sendbutton);  
    	this.mainframe.setVisible(true);
    	this.mainframe.setDefaultCloseOperation(2);
    	
    /***************************************************************************************************/
    /*************************************** 发送处理和界面更新 ****************************************/
    	// 发送私聊文本和UI显示
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
        
    	// 发送私聊图片和UI显示
    	// 为简单起见，一旦选定了插入的图像就立即发送，而不是暂存在发送区
    	this.insertbutton.addActionListener(new ActionListener(){
        	@Override
            public void actionPerformed(ActionEvent e) {
        		// 打开图片文件
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
							doc.insertString(doc.getLength(), "\n我：", set);
						} catch (BadLocationException e) {}
                		pane.insertIcon(new ImageIcon(jfc.getSelectedFile().toString()));
                	}
                });
                File src = new File(jfc.getSelectedFile().toString());
                // 将图片文件读入字节数组buf
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
                			JOptionPane.showMessageDialog(null, "文件大小不能超过4MB");
                			isExceeded = true;
                			break;
                		}
                	}
                } catch (IOException e5) {}  
                if (!isExceeded) {
                // 将buf的有效载荷编码成字符串
                	Encoder encoder = Base64.getEncoder();
                	byte[] imgbuf = new byte[count];
                	for (int i = 0; i < count; i++)
                		imgbuf[i] = buf[i];
                	String str = encoder.encodeToString(imgbuf);
                // 通过socket发送字符串
                	try {
                		PrintWriter pw = new PrintWriter(socket.getOutputStream());
                		pw.println(PRIVATE_PICTURE + targetuid + str);
                		pw.flush();
                	} catch (IOException e1) {}  
                }
            } 
        });
    	
    /************************************* 发送处理和界面更新 *******************************************/
    /****************************************************************************************************/
    }
    
    
    
    /*****************************************************************************************************/
    /**************************************** 接收处理和界面更新 *****************************************/
    /* 收到私聊文本 */
    public void rcvtext(String str) {
    	if (null != this && this.isActivated) {
    		appendHText(str);
    	}
    }
    
    /* 收到私聊图片 */
    // 将接收的图片写入硬盘，并在聊天区追加收到的图片 */
    public void rcvicon(String str) {
    	if (null != this && this.isActivated) {
    		System.out.println(Integer.toString(str.length()));
    		// 生成随机文件名
    		Calendar era = Calendar.getInstance();
    		String curtime = String.valueOf(era.get(Calendar.DATE)) + "_" + String.valueOf(era.get(Calendar.MONTH) + 1) + "_"
    		               + String.valueOf(era.get(Calendar.YEAR)) + "_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
    		curtime = curtime.length() > 233 ? curtime.substring(0, 233) : curtime;
    		File dir = new File(WECHEERFILESDIR + this.cud);
    		if (!dir.exists())
    			dir.mkdirs();
    		String filename = dir + "\\" + curtime;
    		// 解码后通过字节数组rcvbuf存放图像
    		FileOutputStream fos = null;
    		Decoder decoder = Base64.getDecoder();
            byte[] rcvbuf = decoder.decode(str);
            // 通过字节流写入图像文件
    		try {
    			fos = new FileOutputStream(filename, false);
			} catch (FileNotFoundException e1) {}
    		try {
    			fos.write(rcvbuf, 0, rcvbuf.length);
			} catch (IOException e1) {}
    		// 显示收到的私聊图片
    		Document doc = this.pane.getStyledDocument();
    		SimpleAttributeSet set = new SimpleAttributeSet();
    		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
    		LocalDateTime ldt = LocalDateTime.now();
            SwingUtilities.invokeLater(new Runnable() {
            	@Override
            	public void run() {
            		try {
    	    			doc.insertString(doc.getLength(), "\n\n" + ldt.format(dtf), set);
    	    			doc.insertString(doc.getLength(), "\n" + uid + "：", set);
    	    		} catch (BadLocationException e1) {}
            		pane.insertIcon(new ImageIcon(filename));
            		try {
    	    			doc.insertString(doc.getLength(), "\n", set);
            		} catch (BadLocationException e2) {}
            	}
            });
    	}
    }
    
    /* 在聊天区追加发送的文本 */
 	private void appendHText() {
 		Document doc = this.pane.getStyledDocument();
 		Document about2send = this.mypane.getStyledDocument();
		SimpleAttributeSet set = new SimpleAttributeSet();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
		LocalDateTime ldt = LocalDateTime.now();
		try {
			doc.insertString(doc.getLength(), "\n" + ldt.format(dtf), set);
			doc.insertString(doc.getLength(), "\n我：", set);
		} catch (BadLocationException e1) {}
 		for (int i = 0; i < doc.getLength(); i++) { 
 			try {
				doc.insertString(doc.getLength(), about2send.getText(i, 1), set);
			} catch (BadLocationException e) {}
 		}
 		mypane.setText("");
 	}
    
 	/* 在聊天区追加收到的文本 */
    public void appendHText(String str) {
    	Document doc = this.pane.getStyledDocument();
    	SimpleAttributeSet set = new SimpleAttributeSet();
    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
    	LocalDateTime ldt = LocalDateTime.now();
    	SwingUtilities.invokeLater(new Runnable() {
    		@Override
    		public void run() {
    			try {
    	   			doc.insertString(doc.getLength(), "\n\n" + ldt.format(dtf), set);
    	   			doc.insertString(doc.getLength(), "\n" + uid + "：" + str, set);
    	   		} catch (BadLocationException e1) {}
    		}	
    	});
    }
    
    /**************************************** 接收处理和界面更新  ****************************************/
    /*****************************************************************************************************/
    
    
    
    
    /*****************************************************************************************************/
    /*********************************************** 私聊逻辑 ********************************************/
    /* 根据私聊连接状态获取Chatter私聊会话实例（对象）*/
    // 同一时刻仅允许用户A和用户B的一个私聊对象存在
    public static Chatter getInstance(String targetuid, String currentuid, Socket socket) {
		synchronized(lock) {
			for (int i = 0; i < Chatter.instances.size(); i++) {
				// 双方都未离开
				// 返回本方当前实例
				if ((instances.get(i).uid).equals(targetuid) && instances.get(i).isActivated)
					return instances.get(i);
				
				// 对方离开当前会话后重新发起（进入）当前会话
				// 激活我方当前实例并返回
				else if ((instances.get(i).uid).equals(targetuid) && !instances.get(i).isActivated) {
					enableInstance(instances.get(i));
					return instances.get(i);
				}
			}
		}
		
		// 否则创建新的Chatter私聊会话实例（对象）
		Chatter instance = new Chatter(targetuid, currentuid, socket);
		// 为新私聊实例添加发送结束私聊请求功能
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
		// 将新私聊实例加入本方的进行中的私聊实例Vector
		synchronized(lock) {
			instances.addElement(instance);
		}
    	return instance;
    }
    
    /* 对方若退出当前私聊，则失效我方私聊实例，即我方不能再通过当前私聊窗口发消息 */
    // 但之前的聊天记录仍在我方JTextPane中
    public static String disableInstance(Chatter instance) {
    	if (instance.isActivated) {
    		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				instance.sendbutton.setEnabled(false);
    				instance.sendbutton.setText("对方离开了");
    			}
    		});
    		instance.isActivated = false;
    	}

    	return "Current Chatter instance has been expired!";
    }
    
    /* 对方若回到当前私聊，则激活本方私聊实例（前提是我方没有关闭当前私聊窗口）*/
    // 即本方可以再通过当前私聊窗口发消息（因为我方仍持有当前私聊对象），且之前的聊天记录仍在本方JTextPane中
    // 但对方的当前私聊窗口是新的（因为对方关闭私聊窗口再打开，导致实例化了新的Chatter对象）
    public static String enableInstance(Chatter instance) {
    	if (!instance.isActivated) {
    		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				instance.sendbutton.setEnabled(true);
    				instance.sendbutton.setText("发送");
    			}
    		});
    		instance.isActivated = true;
    	}

    	return "Current Chatter instance has been enabled!";
    }
    /******************************************** 私聊逻辑 ***********************************************/
    /*****************************************************************************************************/

} 
