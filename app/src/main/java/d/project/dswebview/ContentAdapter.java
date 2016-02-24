package d.project.dswebview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import d.project.dswebview.db.ContentVO;

/**
 * Created by JeongKuk on 2016-02-24.
 */
public class ContentAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<ContentVO> list;
    private LayoutInflater inflater;
    private TextView tvContentId;
    private TextView tvDesc;

    public ContentAdapter(Context context, int layout, List<ContentVO> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ContentVO getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cv = convertView;
        if (cv == null) {
            cv = inflater.inflate(layout, parent, false);
        }

        tvContentId = (TextView) cv.findViewById(R.id.tvContentId);
        tvDesc = (TextView) cv.findViewById(R.id.tvDesc);
        tvContentId.setText(list.get(position).getContentId());
        tvDesc.setText(list.get(position).getDesc());
        return cv;
    }
}
