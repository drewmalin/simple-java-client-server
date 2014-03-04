package com.clientserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {
	
	private final Socket socket;
	private final Server serverHandle;
	private final int id;

	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Packet pHandle;

	public ClientThread(final Socket socket, final int id, final Server serverHandle) {
		this.socket = socket;
		this.serverHandle = serverHandle;
		this.id = id;
		try {
			this.ois = new ObjectInputStream(this.socket.getInputStream());
			this.oos = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (final IOException e) {
			System.out.println("ERROR - Failed to create client IO.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.pHandle = (Packet) ois.readObject();
				this.pHandle.setHeader("MSG (" + this.id + ") [" + this.pHandle.getHeader() + "]");
				this.serverHandle.transmitPacket(this.pHandle, this);
			} catch (final Exception e) {
				System.out.println("ERROR - Client " + this.id  + " disconnected.");
				this.serverHandle.disconnectClient(this.id);
				break;
			}
		}
	}

	public void sendPacket(final Packet p) {
		try {
			this.oos.writeObject(p);
			this.oos.flush();
		} catch (final IOException e) {
			System.out.println("ERROR - Failed to transmit packet.");
			e.printStackTrace();
		}
	}

	public boolean handshake() {
		System.out.println("Attempted connection: " + this.socket.getInetAddress());
		System.out.println("Handshaking...");
		try {
			final Packet inPacket = (Packet) this.ois.readObject();
			if (!inPacket.getHeader().equals("HELLO")) {
				System.out.println("Attempted connection refused - " + this.socket.getInetAddress());
				return false;
			}
			sendPacket(new Packet("HELLO"));
		} catch (final Exception e) {
			System.out.println("ERROR: Handshaking failed.");
			e.printStackTrace();
			return false;
		}
		System.out.println("Connection accepted from: " + this.socket.getInetAddress());
		return true;
	}

	public void close() {
		try {
			this.socket.close();
			this.ois.close();
			this.oos.close();
		} catch (final IOException e) {
			System.out.println("ERROR - Failed to close client " + this.id + ".");
			e.printStackTrace();
		}
	}

	public int getClientId() {
		return this.id;
	}
}