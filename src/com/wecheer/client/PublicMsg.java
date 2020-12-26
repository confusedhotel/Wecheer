package com.wecheer.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class PublicMsg extends SwingWorker<String, String> {
	public JTextField TextField;
	public JTextArea TextArea;
	public String username;
	private Socket socket;
	private Boolean IsChatPublic;
	
	public PublicMsg(JTextField textfield, JTextArea textarea, String username, Socket socket, boolean IsChatPublic) {
		this.TextField = textfield;
		this.TextArea = textarea;
		this.username = username;
		this.socket = socket;
		this.IsChatPublic = IsChatPublic;
	}
	
	@Override
	protected String doInBackground() throws Exception {
		String str = TextField.getText();
		TextField.setText(null);
		PrintWriter printwriter = new PrintWriter(socket.getOutputStream());
		printwriter.println("*&pubmsg&*" + str);
		printwriter.flush();
		return str;
	}
}
