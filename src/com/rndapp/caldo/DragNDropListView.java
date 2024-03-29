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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

public class DragNDropListView extends ListView {

	private static final String LOG_TAG = "tasks365";

    private static final int END_OF_LIST_POSITION = -2;

    private DropListener mDropListener;
    private int draggingItemHoverPosition;
    private int dragStartPosition; // where was the dragged item originally
    private int mUpperBound; // scroll the view when dragging point is moving out of this bound
    private int mLowerBound; // scroll the view when dragging point is moving out of this bound
    private int touchSlop;
    private Dragging dragging;
    private GestureDetector longPressDetector;
    
    public DragNDropListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public DragNDropListView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        longPressDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            @Override
            public void onLongPress(final MotionEvent e) {
                int x = (int) e.getX();
                final int y = (int) e.getY();
                int itemnum = pointToPosition(x, y);
                if (itemnum == AdapterView.INVALID_POSITION) {
                    return;
                }

                if (dragging != null) {
                    dragging.stop();
                    dragging = null;
                }

                final View item = getChildAt(itemnum - getFirstVisiblePosition());
                item.setPressed(false);
                dragging = new Dragging(getContext());
                dragging.start(y, ((int) e.getRawY()) - y, item);
                draggingItemHoverPosition = itemnum;
                dragStartPosition = draggingItemHoverPosition;

                int height = getHeight();
                mUpperBound = Math.min(y - touchSlop, height / 3);
                mLowerBound = Math.max(y + touchSlop, height * 2 / 3);
            }
        });

        setOnItemLongClickListener(new OnItemLongClickListener() {
            @SuppressWarnings("unused")
            @Override
            public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
                // Return true to let AbsListView reset touch mode
                // Without this handler, the pressed item will keep highlight.
                return true;
            }
        });
    }

    /* pointToPosition() doesn't consider invisible views, but we need to, so implement a slightly different version. */
    private int myPointToPosition(int x, int y) {
        if (y < 0) {
            return getFirstVisiblePosition();
        }
        Rect frame = new Rect();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        if ((x >= frame.left) && (x < frame.right) && (y >= frame.bottom)) {
            return END_OF_LIST_POSITION;
        }
        return INVALID_POSITION;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (longPressDetector.onTouchEvent(ev)) {
            return true;
        }

        if ((dragging == null) || (mDropListener == null)) {
            // it is not dragging, or there is no drop listener
            return super.onTouchEvent(ev);
        }

        int action = ev.getAction();
        switch (ev.getAction()) {

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            dragging.stop();
            dragging = null;

            if (mDropListener != null) {
                if (draggingItemHoverPosition == END_OF_LIST_POSITION) {
                    mDropListener.onDrop(dragStartPosition, getCount() - 1);
                } else if (draggingItemHoverPosition != INVALID_POSITION) {
                    mDropListener.onDrop(dragStartPosition, draggingItemHoverPosition);
                }
            }
            resetViews();
            break;

        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            dragging.drag(x, y);
            int position = dragging.calculateHoverPosition();
            if (position != INVALID_POSITION) {
                if ((action == MotionEvent.ACTION_DOWN) || (position != draggingItemHoverPosition)) {
                    draggingItemHoverPosition = position;
                    doExpansion();
                }
                scrollList(y);
            }
            break;
        }
        return true;
    }

    private void doExpansion() {
        int expanItemViewIndex = draggingItemHoverPosition - getFirstVisiblePosition();
        if (draggingItemHoverPosition >= dragStartPosition) {
            expanItemViewIndex++;
        }

        //Log.v(LOG_TAG, "Dragging item hovers over position " + draggingItemHoverPosition + ", expand item at index " + expanItemViewIndex);

        View draggingItemOriginalView = getChildAt(dragStartPosition - getFirstVisiblePosition());
        for (int i = 0;; i++) {
            View itemView = getChildAt(i);
            if (itemView == null) {
                break;
            }
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            int height = LayoutParams.WRAP_CONTENT;
            if (itemView.equals(draggingItemOriginalView)) {
                height = 1;
            } else if (i == expanItemViewIndex) {
                height = itemView.getHeight() + dragging.getDraggingItemHeight();
            }
            params.height = height;
            itemView.setLayoutParams(params);
        }
    }

    /**
     * Reset view to original height.
     */
    private void resetViews() {
        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                layoutChildren(); // force children to be recreated where needed
                v = getChildAt(i);
                if (v == null) {
                    break;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = LayoutParams.WRAP_CONTENT;
            v.setLayoutParams(params);
        }
    }

    private void resetScrollBounds(int y) {
        int height = getHeight();
        if (y >= height / 3) {
            mUpperBound = height / 3;
        }
        if (y <= height * 2 / 3) {
            mLowerBound = height * 2 / 3;
        }
    }

    private void scrollList(int y) {
        resetScrollBounds(y);

        int height = getHeight();
        int speed = 0;
        if (y > mLowerBound) {
            // scroll the list up a bit
            speed = y > (height + mLowerBound) / 2 ? 16 : 4;
        } else if (y < mUpperBound) {
            // scroll the list down a bit
            speed = y < mUpperBound / 2 ? -16 : -4;
        }
        if (speed != 0) {
            int ref = pointToPosition(0, height / 2);
            if (ref == AdapterView.INVALID_POSITION) {
                //we hit a divider or an invisible view, check somewhere else
                ref = pointToPosition(0, height / 2 + getDividerHeight() + 64);
            }
            View v = getChildAt(ref - getFirstVisiblePosition());
            if (v != null) {
                int pos = v.getTop();
                setSelectionFromTop(ref, pos - speed);
            }
        }
    }

    public void setDropListener(DropListener l) {
        mDropListener = l;
    }

    class Dragging {

        private Context context;
        private WindowManager windowManager;
        private WindowManager.LayoutParams mWindowParams;
        private ImageView mDragView;
        private Bitmap mDragBitmap;
        private int coordOffset;
        private int mDragPoint; // at what offset inside the item did the user grab it
        private int draggingItemHeight;
        private int x;
        private int y;
        private int lastY;

        public Dragging(Context context) {
            this.context = context;
            windowManager = (WindowManager) context.getSystemService("window");
        }

        /**
         * @param y
         * @param offset - the difference in y axis between screen coordinates and coordinates in this view
         * @param view - which view is dragged
         */
        public void start(int y, int offset, View view) {
            this.y = y;
            lastY = y;
            this.coordOffset = offset;
            mDragPoint = y - view.getTop();

            draggingItemHeight = view.getHeight();

            mDragView = new ImageView(context);
            mDragView.setBackgroundResource(android.R.drawable.alert_light_frame);

            // Create a copy of the drawing cache so that it does not get recycled
            // by the framework when the list tries to clean up memory
            view.setDrawingCacheEnabled(true);
            mDragBitmap = Bitmap.createBitmap(view.getDrawingCache());
            mDragView.setImageBitmap(mDragBitmap);

            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.gravity = Gravity.TOP;
            mWindowParams.x = 0;
            mWindowParams.y = y - mDragPoint + coordOffset;
            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mWindowParams.format = PixelFormat.TRANSLUCENT;
            mWindowParams.windowAnimations = 0;

            windowManager.addView(mDragView, mWindowParams);
        }

        public void drag(int x, int y) {
            lastY = this.y;
            this.x = x;
            this.y = y;
            mWindowParams.y = y - mDragPoint + coordOffset;
            windowManager.updateViewLayout(mDragView, mWindowParams);
        }

        public void stop() {
            if (mDragView != null) {
                windowManager.removeView(mDragView);
                mDragView.setImageDrawable(null);
                mDragView = null;
            }
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
                mDragBitmap = null;
            }
        }

        public int getDraggingItemHeight() {
            return draggingItemHeight;
        }

        public int calculateHoverPosition() {
            int adjustedY = (int) (y - mDragPoint + (Math.signum(y - lastY) + 2) * draggingItemHeight / 2);
            // Log.v(LOG_TAG, "calculateHoverPosition(): lastY=" + lastY + ", y=" + y + ", adjustedY=" + adjustedY);
            int pos = myPointToPosition(0, adjustedY);
            if (pos >= 0) {
                if (pos >= dragStartPosition) {
                    pos -= 1;
                }
            }
            return pos;
        }

    }
	/*boolean mDragMode;
	boolean mLongPress;
	
	MotionEvent mSavedEvent;

	int mStartPosition;
	int mEndPosition;
	int mDragPointOffset;		//Used to adjust drag view location
	
	ImageView mDragView;
	GestureDetector mGestureDetector;
	
	DropListener mDropListener;
	RemoveListener mRemoveListener;
	DragListener mDragListener;
	
	public DragNDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mGestureDetector = createFlingDetector();
		
		this.setOnItemLongClickListener(this);
	}
	
	public void setDropListener(DropListener l) {
		mDropListener = l;
	}

	public void setRemoveListener(RemoveListener l) {
		mRemoveListener = l;
	}
	
	public void setDragListener(DragListener l) {
		mDragListener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();	
		
//		if (action == MotionEvent.ACTION_DOWN && mLongPress) {
//			mDragMode = true;
//		}else if (action == MotionEvent.ACTION_DOWN){
//			mSavedEvent = ev;
//		}

		if (!mDragMode) 
			return super.onTouchEvent(ev);

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mStartPosition = pointToPosition(x,y);
				if (mStartPosition != INVALID_POSITION) {
					int mItemPosition = mStartPosition - getFirstVisiblePosition();
                    mDragPointOffset = y - getChildAt(mItemPosition).getTop();
                    mDragPointOffset -= ((int)ev.getRawY()) - y;
					startDrag(mItemPosition,y);
					drag(0,y);// replace 0 with x if desired
				}	
				break;
			case MotionEvent.ACTION_MOVE:
				drag(0,y);// replace 0 with x if desired
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				mDragMode = false;
				mLongPress = false;
				mEndPosition = pointToPosition(x,y);
				stopDrag(mStartPosition - getFirstVisiblePosition());
				if (mDropListener != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) 
	        		 mDropListener.onDrop(mStartPosition, mEndPosition);
				break;
		}
		return true;
	}	
	
	// move the drag view
	private void drag(int x, int y) {
		if (mDragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y - mDragPointOffset;
			WindowManager mWindowManager = (WindowManager) getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(mDragView, layoutParams);

			if (mDragListener != null)
				mDragListener.onDrag(x, y, null);// change null to "this" when ready to use
		}
	}

	// enable the drag view for dragging
	private void startDrag(int itemIndex, int y) {
		stopDrag(itemIndex);

		View item = getChildAt(itemIndex);
		if (item == null) return;
		item.setDrawingCacheEnabled(true);
		if (mDragListener != null)
			mDragListener.onStartDrag(item);
		
        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
        
        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPointOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bitmap);      

        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
	}

	// destroy drag view
	private void stopDrag(int itemIndex) {
		if (mDragView != null) {
			if (mDragListener != null)
				mDragListener.onStopDrag(getChildAt(itemIndex));
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		mDragMode = true;
		
		return true;//onTouchEvent(mSavedEvent);
	}

	private GestureDetector createFlingDetector() {
		return new GestureDetector(getContext(), new SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                    float velocityY) {         	
                if (mDragView != null) {              	
                	int deltaX = (int)Math.abs(e1.getX()-e2.getX());
                	int deltaY = (int)Math.abs(e1.getY() - e2.getY());
               
                	if (deltaX > mDragView.getWidth()/2 && deltaY < mDragView.getHeight()) {
                		mRemoveListener.onRemove(mStartPosition);
                	}
                	
                	stopDrag(mStartPosition - getFirstVisiblePosition());

                    return true;
                }
                return false;
            }
        });
	}*/
}
