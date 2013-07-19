package com.rndapp.caldo;

import java.io.Serializable;

public class Task implements Serializable{
	public String name;
	public String duration;
	public int color;
	public boolean finished;
	
	public Task(String name, String duration, int color){
		this.name = name;
		this.duration = duration;
		this.color = color;
	}

}
