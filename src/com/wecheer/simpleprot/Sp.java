package com.wecheer.simpleprot;

import java.net.InetAddress;

public interface Sp {
	public static final int UID_LENGTH = 10;
	public static final int ICON_MAXSIZE = 4096001;
	public static final String WECHEERFILESDIR = "C:\\Users\\WecheerFiles\\";
	
	public static final String WHO_AM_I = "whoami?";
	public static final String WHO_ARE_MY_FRIENDS = "whoaremyfriends?";
	public static final String WHO_ARE_ONLINE = "whoareonline?";
	
	public static final String PUBLIC_MESSAGE = "*&pubmsg&*";
	public static final String PRIVATE_CONNECTION_REQUEST = "^@privte@^";
	public static final String PRIVATE_CONNECTION_ENQUEST = PRIVATE_CONNECTION_REQUEST;
	public static final String PRIVATE_DISCONNECTION_REQUEST = "!^~quit~^!";
	public static final String PRIVATE_DISCONNECTION_ENQUEST = "!#brquit#!";
	public static final String PRIVATE_PARTY_OFFLINE = "^@pvtefl@^";
	public static final String PRIVATE_MESSAGE = "~^@prvt@^~";
	public static final String PRIVATE_PICTURE = "*!#icon#!*";
	public static final String PUBLIC_USERS_ONLINE = "^*&onpu^&*";
	public static final String SIGN_UP = "~^!@su@!~~";
	
	public static final String _SEMICOLON = ";";
	public static final String _COMMA = ",";
	public static final String _QMARK = "?";
	public static final String _FULLSTOP = ".";
	public static final String _COME = "来了";
	public static final String _LEAVE = "走了";
}
