package com.sunteam.ebook.adapter;

import java.util.ArrayList;

import com.sunteam.ebook.R;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主菜单列表类
 * 
 * @author wzp
 *
 */
public class MainMenuListAdapter extends BaseAdapter implements OnClickListener
{
	private Context mContext = null;
	private ArrayList<String> gListData = null;
	private int selectItem = 0;
	
	public MainMenuListAdapter( Context context, String[] list )
	{
		this.mContext = context;
		this.gListData = new ArrayList<String>();
		
		for( int i = 0; i < list.length; i++ )
		{
			gListData.add(list[i]);
		}
	}
	
	public MainMenuListAdapter( Context context, ArrayList<String> list )
	{
		this.mContext = context;
		this.gListData = list;
	}

	public void setSelectItem( int selectItem )
	{
		this.selectItem = selectItem;
		this.notifyDataSetInvalidated();
	}
	
	//按了上键
	public void up()
	{
		if( this.selectItem > 0 )
		{
			this.selectItem--;
		}
		else
		{
			this.selectItem = gListData.size() - 1;
		}
		
		this.notifyDataSetInvalidated();
	}
	
	//按了下键
	public void down()
	{
		if( this.selectItem < gListData.size() - 1 )
		{
			this.selectItem++;
		}
		else
		{
			this.selectItem = 0;
		}
		
		this.notifyDataSetInvalidated();
	}
	
	//按了确定键
	public void enter()
	{
		
	}
	
	public ArrayList<String> getListData()
	{
		return	gListData;
	}
	
	public void setListData( ArrayList<String> list )
	{
		gListData = list;
	}
	
	@Override
	public int getCount()
	{
		if( null == gListData )
		{
			return	0;
		}
        return gListData.size();
    }
    
    @Override
    public Object getItem(int position)
    {
    	if( null == gListData )
		{
			return	null;
		}
       return gListData.get(position);
    }
    
    @Override
    public long getItemId(int position)
    {
    	if( null == gListData )
		{
			return	0;
		}
        return	position;
    }
    
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
        ViewHolder vh = null;

        if( null == convertView )
        {
        	vh = new ViewHolder();
        	convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_menu, null);
        	
        	vh.tvMenu = (TextView)convertView.findViewById(R.id.menu);    		
    		vh.tvMenu.setTag(String.valueOf(position));
    		vh.tvMenu.setOnClickListener(this);

        	convertView.setTag(vh);
        }
        else
        {
        	vh = (ViewHolder) convertView.getTag();
        }
        
        if( selectItem == position )
		{
        	convertView.setBackgroundResource(R.color.green);
		}
		else
		{
			convertView.setBackgroundResource(R.color.white);
		}
		
    	if( !TextUtils.isEmpty( gListData.get(position) ) )
    	{
    		vh.tvMenu.setText( gListData.get(position) );
    	}
    	else
    	{
    		vh.tvMenu.setText( "" );
    	}
                
        return convertView;
	}

	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		Intent intent = null;
		int id = v.getId();
		int position = 0;
		
		String tag = (String)v.getTag();
		position = Integer.parseInt(tag);
		
		switch( id )
		{
			case R.id.menu:
				setSelectItem( position );
				break;
			default:
				break;
		}
	}
	
	private class ViewHolder
	{
		TextView tvMenu = null;				//菜单名称
	}
}
