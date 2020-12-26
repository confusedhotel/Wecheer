package com.wecheer.client;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.net.*;

public class Login extends Thread{
	public static void main(String[] args) throws IOException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Login login = new Login();
				login.start();
			}
		});
	}
	
	public void run() {
		JFrame login = new JFrame();
		login.setTitle("µÇÂ¼");		
		login.setSize(300, 200);		
		login.setDefaultCloseOperation(3);
		login.setLocationRelativeTo(null);	
		login.setResizable(false);		
		login.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		Dimension dim1 = new Dimension(230, 30);
		JLabel labName = new JLabel("ÕËºÅ");	
		JTextField textname = new JTextField();						
		JLabel labpass= new JLabel("ÃÜÂë");
		JPasswordField textword = new JPasswordField();
		JButton button0 = new JButton();
		JButton button1 = new JButton();
		button0.setText("µÇÂ¼");
		button1.setText("×¢²á");
		textname.setPreferredSize(dim1);
		textword.setPreferredSize(dim1);
		login.add(labName); 
		login.add(textname); 
		login.add(labpass);
		login.add(textword);
		login.add(button0);
		login.add(button1);
		login.setVisible(true);
			
		Client client = new Client(textname, textword, login, true);
		button0.addActionListener(client);
		Client uregister = new Client(textname, textword, login, false);
		button1.addActionListener(uregister);
	}
}
