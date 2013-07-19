package com.rndapp.caldo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.NumberPicker;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    ArrayList<TaskList> mTaskLists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        CaldoApplication ca = (CaldoApplication)getApplicationContext();
        mTaskLists = ca.getTasks();
        
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onPause(){
    	super.onPause();
    	
    	try {
			FileOutputStream fout = openFileOutput("task_lists", Context.MODE_PRIVATE);
		    ObjectOutputStream oos = new ObjectOutputStream(fout);
		    oos.writeObject(mTaskLists);
		    oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ListFragment();
            Bundle args = new Bundle();
            args.putInt(ListFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mTaskLists.size()+1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String title = "New List";
        	if (position<mTaskLists.size()){
        		title = mTaskLists.get(position).listName;
        	}
            return title;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class ListFragment extends Fragment implements OnClickListener {
        public ListFragment() {
        }
        
        public interface OnSectionButtonPressed{
        	public void onSectionButtonPressed(int sectionNum, int buttonId);
        }

        public static final String ARG_SECTION_NUMBER = "section_number";
        public TaskList mTasks;
        Button plus;
        View swatch;
        int swatchColor;
        DragNDropListView dndlv;
        DragNDropAdapter adapter;
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
//            TextView textView = new TextView(getActivity());

        	int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        	if (((MainActivity)getActivity()).mTaskLists.size() <= sectionNumber ||
        			((MainActivity)getActivity()).mTaskLists.get(sectionNumber) == null){
        		mTasks = new TaskList();

        		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
    					getActivity());
    			// set title
    			alertDialogBuilder.setTitle("Add List");
    			
    			View layout = getActivity().getLayoutInflater().inflate(R.layout.new_list_template, null);
    			final EditText name = (EditText)layout.findViewById(R.id.list_name);
    			
    			alertDialogBuilder
	    			.setCancelable(true)
	    			.setView(layout)
	    			.setPositiveButton("Ok",
	    					new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog,
	    						int id) {
	    					mTasks.listName = name.getText().toString();
	    	        		((MainActivity)getActivity()).mTaskLists.add(mTasks);
	    				}
	    			})
	    			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog,int id) {
	    					mTasks.listName = "New List";
	    	        		((MainActivity)getActivity()).mTaskLists.add(mTasks);
	    				}
	    			});
    			AlertDialog alertDialog = alertDialogBuilder.create();
    			alertDialog.show();
        	}else{
        		mTasks = ((MainActivity)getActivity()).mTaskLists.get(sectionNumber);
        	}
        	
        	View v = inflater.inflate(R.layout.frag_template, container, false);
        	plus = (Button)v.findViewById(R.id.plus_button_item);
        	plus.setOnClickListener(this);
        	
        	dndlv = (DragNDropListView)v.findViewById(R.id.taskList);
        	
        	DragNDropListView listView = dndlv;
    		adapter = new DragNDropAdapter(getActivity(), new int[]{R.layout.dragitem}, new int[]{R.id.taskll}, new int[]{R.id.task}, new int[]{R.id.duration}, mTasks);
            listView.setAdapter(adapter);
            
            listView.setDropListener(mDropListener);
            //listView.setRemoveListener(mRemoveListener);
            //listView.setDragListener(mDragListener);
        	
            return v;
        }
        
        protected void newTask(String name, String duration, int color){
        	Task newTask = new Task(name, duration, color);
        	mTasks.list.add(newTask);
        	
        	if (mTasks.list.size() == 1){
        		DragNDropListView listView = dndlv;
        		adapter = new DragNDropAdapter(getActivity(), new int[]{R.layout.dragitem}, new int[]{R.id.taskll}, new int[]{R.id.task}, new int[]{R.id.duration}, mTasks);
                listView.setAdapter(adapter);
                
                listView.setDropListener(mDropListener);
                //listView.setRemoveListener(mRemoveListener);
                //listView.setDragListener(mDragListener);
        	}else{
        		adapter.notifyDataSetChanged();
        	}
        }

        @Override
		public void onClick(View v) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			// set title
			alertDialogBuilder.setTitle("Add Task");
			
			View layout = getActivity().getLayoutInflater().inflate(R.layout.new_task_template, null);
			final NumberPicker np1 = (NumberPicker)layout.findViewById(R.id.np1);
			final NumberPicker np2 = (NumberPicker)layout.findViewById(R.id.np2);
			
			np1.setMaxValue(23);
			np1.setMinValue(0);
			np2.setMaxValue(59);
			np2.setMinValue(0);
			
			final EditText task = (EditText)layout.findViewById(R.id.taskName);
			
			swatch = (View)layout.findViewById(R.id.color_swatch);

			Button btnRed = (Button)layout.findViewById(R.id.btn_red);
			Button btnBlue = (Button)layout.findViewById(R.id.btn_blue);
			Button btnPlum = (Button)layout.findViewById(R.id.btn_plum);
			Button btnGold = (Button)layout.findViewById(R.id.btn_yellow);
			Button btnOrange = (Button)layout.findViewById(R.id.btn_orange);
			Button btnGreen = (Button)layout.findViewById(R.id.btn_green);
			
			OnClickListener listener = new OnClickListener() {
				public void onClick(View v) {
					switch (v.getId()){
					case R.id.btn_blue:
						swatchColor = getResources().getColor(R.color.pastel_blue);
						swatch.setBackgroundColor(getResources().getColor(R.color.pastel_blue));
						break;
					case R.id.btn_green:
						swatchColor = getResources().getColor(R.color.pastel_green);
						swatch.setBackgroundColor(getResources().getColor(R.color.pastel_green));
						break;
					case R.id.btn_orange:
						swatchColor = getResources().getColor(R.color.pastel_orange);
						swatch.setBackgroundColor(getResources().getColor(R.color.pastel_orange));
						break;
					case R.id.btn_plum:
						swatchColor = getResources().getColor(R.color.pastel_plum);
						swatch.setBackgroundColor(getResources().getColor(R.color.pastel_plum));
						break;
					case R.id.btn_red:
						swatchColor = getResources().getColor(R.color.pastel_red);
						swatch.setBackgroundColor(getResources().getColor(R.color.pastel_red));
						break;
					case R.id.btn_yellow:
						swatchColor = getResources().getColor(R.color.pastel_yellow);
						swatch.setBackgroundColor(getResources().getColor(R.color.pastel_yellow));
						break;
					}
				}
			};

			btnRed.setOnClickListener(listener);
			btnBlue.setOnClickListener(listener);
			btnOrange.setOnClickListener(listener);
			btnGreen.setOnClickListener(listener);
			btnGold.setOnClickListener(listener);
			btnPlum.setOnClickListener(listener);
			
			swatchColor = getResources().getColor(R.color.goldenrod);
			
			// set dialog message
			alertDialogBuilder
					//.setMessage(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)))
					.setCancelable(true)
					.setView(layout)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String minutes;
									if (np2.getValue()>9){
										minutes = ""+np2.getValue();
									}else{
										minutes = "0"+np2.getValue();
									}
									String duration = "" + np1.getValue() + ":" + minutes;
									newTask(task.getText().toString(), duration, swatchColor);
								}
							})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
        
        private DropListener mDropListener = 
        		new DropListener() {
                public void onDrop(int from, int to) {
                	ListAdapter adapter = dndlv.getAdapter();
                	if (adapter instanceof DragNDropAdapter) {
                		((DragNDropAdapter)adapter).onDrop(from, to);
                		dndlv.invalidateViews();
                	}
                }
            };
            
//            private RemoveListener mRemoveListener =
//                new RemoveListener() {
//                public void onRemove(int which) {
//                	ListAdapter adapter = dndlv.getAdapter();
//                	if (adapter instanceof DragNDropAdapter) {
//                		((DragNDropAdapter)adapter).onRemove(which);
//                		dndlv.invalidateViews();
//                	}
//                }
//            };
//            
//            private DragListener mDragListener =
//            	new DragListener() {
//
////            	int backgroundColor = 0xe0103010;
////            	int defaultBackgroundColor;
//            	
//        			public void onDrag(int x, int y, ListView listView) {
//        				
//        			}
//
//        			public void onStartDrag(View itemView) {
//        				itemView.setVisibility(View.INVISIBLE);
//        				
//        			}
//
//        			public void onStopDrag(View itemView) {
//        				itemView.setVisibility(View.VISIBLE);
//        				
//        			}
//            	
//            };
    }
}
