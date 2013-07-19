/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rndapp.caldo;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class DragNDropAdapter extends BaseAdapter implements RemoveListener, DropListener{

	private int[] mIds;
	private int[] mDurationIDs;
	private int[] mBgllIDs;
    private int[] mLayouts;
    private ArrayList<Integer> mColors;
    private LayoutInflater mInflater;
    private ArrayList<String> mTaskNames;
    private ArrayList<String> mDurations;
    private TaskList mTasks;

    //public DragNDropAdapter(Context context, ArrayList<String> content) {
        //init(context,new int[]{android.R.layout.simple_list_item_1},new int[]{android.R.id.text1}, content);
    //}
    
    public DragNDropAdapter(Context context, int[] itemLayouts, 
    		int[] bgllIDs,
    		int[] itemIDs, 
    		int[] durationIDs,  
    		TaskList tasks) {
    	init(context,itemLayouts,bgllIDs,itemIDs, durationIDs, tasks);
    }
    
    private void init(Context context, 
    		int[] layouts, 
    		int[] bgllIDs,
    		int[] ids, 
    		int[] durationIDs, 
    		TaskList tasks) {
    	// Cache the LayoutInflate to avoid asking for a new one each time.
    	mInflater = LayoutInflater.from(context);
    	mIds = ids;
    	mDurationIDs = durationIDs;
    	mBgllIDs = bgllIDs;
    	mLayouts = layouts;
    	mTasks = tasks;
    }
    
    /**
     * The number of items in the list
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return mTasks.list.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficient to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public String getItem(int position) {
        return mTaskNames.get(position);
    }

    /**
     * Use the array index as a unique id.
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mLayouts[0], null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (CheckBox) convertView.findViewById(mIds[0]);
            holder.duration = (TextView) convertView.findViewById(mDurationIDs[0]);
            holder.taskll = (LinearLayout) convertView.findViewById(mBgllIDs[0]);
            
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        holder.text.setText(mTasks.list.get(position).name);
        holder.duration.setText(mTasks.list.get(position).duration);
        holder.taskll.setBackgroundColor(mTasks.list.get(position).color);

        return convertView;
    }

    static class ViewHolder {
    	LinearLayout taskll;
        CheckBox text;
        TextView duration;
    }

	public void onRemove(int which) {
		if (which < 0 || which > mTasks.list.size()) return;		
		mTasks.list.remove(which);
	}

	public void onDrop(int from, int to) {
		if (to < mTasks.list.size()){
			Task temp = mTasks.list.get(from);
			mTasks.list.remove(from);
			mTasks.list.add(to + 1,temp);
		}else{
			Task temp = mTasks.list.get(from);
			mTasks.list.remove(from);
			mTasks.list.add(temp);
		}
	}
}