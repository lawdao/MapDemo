package example.fussen.mapdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import example.fussen.mapdemo.R;
import example.fussen.mapdemo.bean.SearchAddressInfo;

/**
 * Created by Fussen on 2016/11/1.
 */

public class SearchAdapter extends BaseAdapter {

    private ArrayList<SearchAddressInfo> mData;

    private Context context;

    public SearchAdapter(Context context, ArrayList<SearchAddressInfo> data) {

        this.context = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View view = View.inflate(context, R.layout.item_adapter, null);

        TextView address = (TextView) view.findViewById(R.id.address);
        TextView des = (TextView) view.findViewById(R.id.des);
        ImageView check = (ImageView) view.findViewById(R.id.check);

        SearchAddressInfo addressInfo = mData.get(position);

        if (addressInfo.isChoose) {
            check.setVisibility(View.VISIBLE);
        } else {
            check.setVisibility(View.GONE);
        }
        address.setText(addressInfo.title);
        des.setText(addressInfo.addressName);

        return view;
    }

    public void setData(ArrayList<SearchAddressInfo> data) {
        this.mData = data;
        notifyDataSetChanged();
    }
}
