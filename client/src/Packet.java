package com.clientserver;

import java.io.Serializable;

public class Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String header;
	private final String data;

	public Packet(final String header, final String data) {
		this.header = header;
		this.data = data;
	}

	public Packet(final String header) {
		this.header = header;
		this.data = "";
	}

	public void setHeader(final String header) {
		this.header = header;
	}

	public String getHeader() {
		return this.header;
	}

	public String getData() {
		return this.data;
	}
}