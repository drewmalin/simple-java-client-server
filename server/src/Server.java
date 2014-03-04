package com.clientserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {

	private static final int SERVER_PORT = 1337;
	private static final int MAX_CLIENTS = 16;

	private static final Random idGenerator = new Random();

	private ServerSocket serverSocket;
	private final Map<Integer, ClientThread> clientThreads;

	/**
	 * Initialize Server with a set of client handler threads, a new socket.
 	 */
	public Server() {
		this.clientThreads = new HashMap<Integer, ClientThread>();
		try {
			this.serverSocket = new ServerSocket(Server.SERVER_PORT);
			this.start();
		} catch (final IOException e) {
			System.out.println("ERROR - Failed to create server.");
			e.printStackTrace();
			System.exit(1);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Server shutting down.");
			}
		});
	}

	/**
	 * Primary server logic
	 */
	@Override
	public void run() {
		while (true) {
			if (this.clientThreads.size() < Server.MAX_CLIENTS) {
				System.out.println("SERVER - Awaiting connections. " + this.clientThreads.size() + " clients connected.");
				try {
					final Socket clientSocket = this.serverSocket.accept();
					final ClientThread tmpClient = new ClientThread(clientSocket, nextUniqueId(), this);
					if (tmpClient.handshake()) {
						tmpClient.start();
						this.clientThreads.put(tmpClient.getClientId(), tmpClient);
					}
				} catch (final IOException e) {
					System.out.println("ERROR - Failed to connect client.");
					e.printStackTrace();
				}
			}
		}
	} 

	public void transmitPacket(final Packet packet, final ClientThread ctxClient) {
		for (final ClientThread client : this.clientThreads.values()) {
			if (ctxClient == null || client != ctxClient) {
				client.sendPacket(packet);
			}
		}
	}

	public void disconnectClient(final int id) {
		if (this.clientThreads.containsKey(id)) {
			System.out.println("SERVER - Removing client: " + id);
			this.clientThreads.get(id).close();
			this.clientThreads.remove(id);
		}
	}

	private int nextUniqueId() {
		int tmpId = Server.idGenerator.nextInt();
		while (this.clientThreads.containsKey(tmpId)) {
			tmpId = Server.idGenerator.nextInt();
		}
		return tmpId;
	}

	public static void main(String[] args) {
		Server server = new Server();
	}
}