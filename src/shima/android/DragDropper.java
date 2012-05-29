package shima.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class DragDropper {
	private final static String TAG = DragDropper.class.getSimpleName();
	private View decorView;
	private AbsListView absListView;
	private ImageView dragee;
	private WindowManager windowManager;
	private WindowManager.LayoutParams layoutParams;
	private int formerPosition = AdapterView.INVALID_POSITION;
	private Point eventPos = new Point();
	private int[] localCoordinateOffset = { 0, 0 };
	private int[] subviewOrigin = { 0, 0 };
	private boolean dragging = false;
	private enum Scroll { STOP, FORWARD, BACKWARD };
	private Scroll scrolling = Scroll.STOP;
	private Runnable scroller = new Runnable() {
		public void run() {
			switch (scrolling) {
			case STOP:		return;
			case FORWARD:	absListView.smoothScrollBy( dragee.getHeight(), 300); break;
			case BACKWARD:	absListView.smoothScrollBy(-dragee.getHeight(), 300); break;
			}
			scroll();
		}
	};

	public DragDropper(AbsListView view) {
		absListView = view;
		decorView = ((Activity)view.getContext()).getWindow().getDecorView();
		dragee = new ImageView(view.getContext());
		windowManager = (WindowManager)view.getContext().getSystemService("window");
		layoutParams = new WindowManager.LayoutParams();
		layoutParams.height	= WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.width	= WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.flags	= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
							| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
							| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		layoutParams.format	= PixelFormat.TRANSLUCENT;
		layoutParams.gravity= Gravity.TOP | Gravity.LEFT;
	}
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long id) {
		Rect frame = new Rect();
		decorView.getWindowVisibleDisplayFrame(frame);
		adapter.getLocationOnScreen(localCoordinateOffset);
		localCoordinateOffset[0] -= frame.left; localCoordinateOffset[1] -= frame.top;
		view.getLocationOnScreen(subviewOrigin);
		subviewOrigin[0] = eventPos.x - subviewOrigin[0] + localCoordinateOffset[0] + frame.left;
		subviewOrigin[1] = eventPos.y - subviewOrigin[1] + localCoordinateOffset[1] + frame.top;
		
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		dragee.setImageBitmap(view.getDrawingCache());
//		layoutParams.x = localCoordinateOffset[0] + eventPos.x - view.getWidth()/2;
//		layoutParams.y = localCoordinateOffset[1] + eventPos.y - view.getHeight()/2;
		layoutParams.x = localCoordinateOffset[0] + eventPos.x - subviewOrigin[0];;
		layoutParams.y = localCoordinateOffset[1] + eventPos.y - subviewOrigin[1];;
		windowManager.addView(dragee, layoutParams);
		formerPosition = pos;
		dragging = true;
		return true;
	}
	public void onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "raw x=" + ev.getRawX() + ", raw y=" + ev.getRawY());
			Log.d(TAG, "    x=" + ev.getX()    + ",     y=" + ev.getY());
			eventPos.x = (int)ev.getX(); eventPos.y = (int)ev.getY();
			break;
		case MotionEvent.ACTION_UP:
			eventPos.x = (int)ev.getX(); eventPos.y = (int)ev.getY();
			if (dragging) {
				scrolling = Scroll.STOP;
				drop();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			eventPos.x = (int)ev.getX(); eventPos.y = (int)ev.getY();
			if (dragging) {
				drag();
				Log.d(TAG, "eventPos.y=" + eventPos.y + ", absListView.getHeight()=" + absListView.getHeight() + ", dragee.getHeight()" + dragee.getHeight());
//				if (eventPos.y + dragee.getHeight()/2 > absListView.getHeight()) {
				if (eventPos.y + dragee.getHeight() - subviewOrigin[1] + 10 > absListView.getHeight()) {
					scrolling = Scroll.FORWARD;
					scroll();
//				} else if (eventPos.y - dragee.getHeight()/2 < 0) {
				} else if (eventPos.y - subviewOrigin[1] - 10 < 0) {
					scrolling = Scroll.BACKWARD;
					scroll();
				} else {
					scrolling = Scroll.STOP;
				}
			}
			break;
		}
	}
	private void drag() {
//		layoutParams.x = localCoordinateOffset[0] + eventPos.x - dragee.getWidth()/2;
//		layoutParams.y = localCoordinateOffset[1] + eventPos.y - dragee.getHeight()/2;
		layoutParams.x = localCoordinateOffset[0] + eventPos.x - subviewOrigin[0];
		layoutParams.y = localCoordinateOffset[1] + eventPos.y - subviewOrigin[1];
		windowManager.updateViewLayout(dragee, layoutParams);
	}
	private void drop() {
		windowManager.removeView(dragee);
		int pos = absListView.pointToPosition(eventPos.x, eventPos.y);
		ArrayAdapter adapter = (ArrayAdapter)absListView.getAdapter();
		if (0 <= pos && pos < adapter.getCount()) {
			Object item = adapter.getItem(formerPosition);
			adapter.remove(item);
			adapter.insert(item, pos);
		} else {
			Toast.makeText(absListView.getContext(), "Oops, you're out of range.", Toast.LENGTH_SHORT).show();
		}
		dragging = false;
		absListView.invalidateViews();
	}
	private void scroll() { absListView.getHandler().postDelayed(scroller, 300); }
}