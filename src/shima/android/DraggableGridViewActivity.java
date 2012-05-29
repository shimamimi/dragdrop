package shima.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

public class DraggableGridViewActivity extends Activity {
	private final static String TAG = DraggableGridViewActivity.class.getSimpleName();
	private ArrayList<CompositeDataHolder> gridDataHolders;
	private ArrayList<CompositeDataHolder> listDataHolders;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		
		gridDataHolders = createGridDataHolders();
		DragDropGridView gridView = (DragDropGridView)findViewById(R.id.dragDropGridView);
		gridView.setAdapter(new CompositeAdapter(getApplicationContext(), R.layout.grid_item, gridDataHolders));
		
		listDataHolders = new ArrayList<CompositeDataHolder>();
		for (CompositeDataHolder h : gridDataHolders) {
			GridDataHolder g = (GridDataHolder)h;
			listDataHolders.add(new ListDataHolder(g.bitmap, g.uri));
		}
		DragDropListView listView = (DragDropListView)findViewById(R.id.dragDropListView);
		listView.setAdapter(new CompositeAdapter(getApplicationContext(), R.layout.list_item, listDataHolders));
	}
	private ArrayList<CompositeDataHolder> createGridDataHolders() {
		Log.d(TAG, "extractDataHolders() has called!");
		ArrayList<CompositeDataHolder> list = new ArrayList<CompositeDataHolder>();
		ContentResolver cr = getContentResolver();
		
		String[] proj = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
		Cursor c = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			long mediaId = c.getLong(0);
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, mediaId, MediaStore.Images.Thumbnails.MICRO_KIND, null);
			Uri uri = Uri.parse("file://" + c.getString(1));
			Log.d(TAG, "mediaId=" + mediaId + ", DATA=" + c.getString(1));
			Log.d(TAG, "uri=" + uri + ", path=" + uri.getPath());
			list.add(new GridDataHolder(bitmap, uri));
			c.moveToNext();
		}
		return list;
	}
	class GridDataHolder implements CompositeDataHolder {
		Bitmap bitmap;
		boolean selected;
		Uri uri;
		
		GridDataHolder(Bitmap b, Uri u) { bitmap = b; uri = u; selected = false; }
		public CompositeViewHolder createCompositeViewHolder() { return new GridViewHolder(); }
	}
	class GridViewHolder implements CompositeViewHolder {
		ImageView imageView;
		CheckBox checkBox;
		
		public void setView(View view) {
			imageView = (ImageView)view.findViewById(R.id.imageView);
			checkBox = (CheckBox)view.findViewById(R.id.checkBox);
		}
		public void setId(int position) {
			imageView.setId(position);
			checkBox.setId(position);
		}
		public void setData(CompositeDataHolder compositeDataHolder) {
			final GridDataHolder dataHolder = (GridDataHolder)compositeDataHolder;
			imageView.setImageBitmap(dataHolder.bitmap);
			checkBox.setChecked(dataHolder.selected);
			checkBox.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dataHolder.selected = !dataHolder.selected;
				}
			});
		}
	}
	class ListDataHolder implements CompositeDataHolder {
		Bitmap bitmap;
		Uri uri;
		
		ListDataHolder(Bitmap b, Uri u) { bitmap = b; uri = u; }
		public CompositeViewHolder createCompositeViewHolder() { return new ListViewHolder(); }
	}
	class ListViewHolder implements CompositeViewHolder {
		ImageView imageView;
		TextView textView;
		
		public void setView(View view) {
			imageView = (ImageView)view.findViewById(R.id.imageViewInList);
			textView = (TextView)view.findViewById(R.id.textViewInList);
		}
		public void setId(int position) {
			imageView.setId(position);
			textView.setId(position);
		}
		public void setData(CompositeDataHolder compositeDataHolder) {
			final ListDataHolder dataHolder = (ListDataHolder)compositeDataHolder;
			imageView.setImageBitmap(dataHolder.bitmap);
			textView.setText(dataHolder.uri.toString());
		}
	}
}