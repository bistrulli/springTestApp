package com.example.restservice;

public class ResObj {

	private long id=-1;
	private String content="";

	public ResObj(long id, String content) {
		this.id = id;
		this.content = content;
	}
	
	public ResObj() {
	}

	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}
}
