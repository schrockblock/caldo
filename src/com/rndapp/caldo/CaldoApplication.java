package com.rndapp.caldo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.app.Application;

public class CaldoApplication extends Application {
	private ArrayList<TaskList> mTaskLists;
	
	@Override
    public void onCreate() {
		super.onCreate();
		
		try {
			FileInputStream fin = openFileInput("task_lists");
			ObjectInputStream ois = new ObjectInputStream(fin);
			mTaskLists = (ArrayList<TaskList>) ois.readObject();
			ois.close();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			mTaskLists = new ArrayList<TaskList>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (mTaskLists == null){
			mTaskLists = new ArrayList<TaskList>();
		}
	}
	
	public ArrayList<TaskList> getTasks(){
		return mTaskLists;
	}
	
	public void setTasks(ArrayList<TaskList> taskLists){
		mTaskLists = taskLists;
	}
}
