package shima.android;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

//
interface CompositeViewHolder {
	void setView(View view);
	void setId(int id);
	void setData(CompositeDataHolder item);
}
interface CompositeDataHolder {
	CompositeViewHolder createCompositeViewHolder();
}
public class CompositeAdapter extends ArrayAdapter<CompositeDataHolder> {
	private int resourceId;
	
	public CompositeAdapter(Context context, int resource, List<CompositeDataHolder> list) {
		super(context, resource, list);
		resourceId = resource;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CompositeDataHolder dataHolder = getItem(position);
		CompositeViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resourceId, null);
			viewHolder = dataHolder.createCompositeViewHolder();
			viewHolder.setView(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (CompositeViewHolder)convertView.getTag();
		}
		viewHolder.setId(position);
		viewHolder.setData(dataHolder);
		return convertView;
	}
}