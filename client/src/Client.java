package com.clientserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Thread {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Packet pHandle;

	public Client() {
	}

	public void connect(final String host, final int port) {
		System.out.println("Attempting to connect to " + host + ":" + port);
		try {
			this.socket = new Socket(host, port);
			this.oos = new ObjectOutputStream(this.socket.getOutputStream());
			this.ois = new ObjectInputStream(this.socket.getInputStream());
			System.out.println("Connection successful: " + this.socket.getInetAddress() + "\nHandshaking...");
			this.oos.writeObject(new Packet("HELLO"));
			this.oos.flush();
			this.pHandle = (Packet) this.ois.readObject();
			if (this.pHandle.getHeader().equals("HELLO")) {
				System.out.println("Success!");
				this.start();
				handleUserInput();
			}
			else {
				System.out.println("ERROR - Handshaking failed");
				System.exit(1);
			}
		} catch (final Exception e) {
			System.out.println("Failed to connect to server.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.pHandle = (Packet) this.ois.readObject();
				System.out.println(this.pHandle.getHeader() + ": " + this.pHandle.getData());
			} catch (final Exception e) {
				System.out.println("ERROR - Connection to server failed.");
				System.exit(1);
			}
		}
	}

	public void handleUserInput() {
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				this.oos.writeObject(new Packet(getTimestamp(), br.readLine()));
				this.oos.flush();
			} catch (final IOException e) {
				System.out.println("ERROR - Failed to retrieve console input.");
				System.exit(1);
			}
		}
	}

	public String getTimestamp() {
		return new java.text.SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date());
	}

	public static void main(String[] args) {
		final Client client = new Client();
		client.connect("localhost", 1337);
	}
}