package com.rndapp.caldo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TaskViewActivity extends Activity implements OnClickListener {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskview);
        
        Button delete = (Button)findViewById(R.id.taskViewDelete);
        delete.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()){
		case R.id.taskViewDelete:
			//delete task
			break;
		}
	}

}
