package shima.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class DragDropListView extends ListView implements AdapterView.OnItemLongClickListener {
	private DragDropper dragDropper;
	
	public DragDropListView(Context context) { this(context, null); }
	public DragDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		dragDropper = new DragDropper(this);
		setOnItemLongClickListener(this);
	}
	@Override public boolean onTouchEvent(MotionEvent ev) {
		dragDropper.onTouchEvent(ev);
		return super.onTouchEvent(ev);
	}
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long id) {
		return dragDropper.onItemLongClick(adapter, view, pos, id);
	}
}