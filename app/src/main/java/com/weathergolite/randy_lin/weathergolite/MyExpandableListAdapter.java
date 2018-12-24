package com.weathergolite.randy_lin.weathergolite;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    View.OnClickListener ivGoToChildClickListener;
    private Context mContext;
    private String groupData;
    private ArrayList<String> itemData;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();//更新数据
            super.handleMessage(msg);
        }
    };

    public MyExpandableListAdapter(Context c, String group, ArrayList<String> itemList) {
        this.mContext = c;
        this.groupData = group;
        this.itemData = itemList;
    }


    @Override
    public int getGroupCount() {
        if (!groupData.isEmpty())
            return 1;
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemData.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemData.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderGroup groupHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.explist_group, parent, false);

            groupHolder = new ViewHolderGroup();
            groupHolder.tv_group_name = (TextView) convertView.findViewById(R.id.explist_group_tv);
           /* groupHolder.tv_group_name.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    isExpanded ? R.drawable.ic_keyboard_arrow_up_white_24dp : R.drawable.ic_keyboard_arrow_down_white_24dp,
                    0);*/
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (ViewHolderGroup) convertView.getTag();
        }
        groupHolder.tv_group_name.setText(groupData);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderItem itemHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.explist_item, parent, false);
            itemHolder = new ViewHolderItem();
            itemHolder.tv_name = (TextView) convertView.findViewById(R.id.explist_item_tv);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolderItem) convertView.getTag();
        }
        itemHolder.tv_name.setText(itemData.get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;

    }

    public void refresh(ExpandableListView expandableListView, int groupPosition) {
        handler.sendMessage(new Message());
        expandableListView.collapseGroup(groupPosition);
        expandableListView.expandGroup(groupPosition);
    }

    private static class ViewHolderGroup {
        private TextView tv_group_name;
    }

    private static class ViewHolderItem {
        private TextView tv_name;
    }
}
