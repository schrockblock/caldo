package com.rndapp.caldo;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskList implements Serializable{
	public String listName;
	public ArrayList<Task> list;
	
	public TaskList(){
		list = new ArrayList<Task>();
	}

}
