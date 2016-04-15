package com.sunteam.ebook.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sunteam.ebook.DaisyActivity;
import com.sunteam.ebook.R;
import com.sunteam.ebook.TxtActivity;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TTSUtils;

/**
 * 主列表类
 * 
 * @author wzp
 *
 */
public class MainListAdapter extends BaseAdapter implements OnClickListener
{
	private Context mContext = null;
	private ArrayList<String> gListData = null;
	private int selectItem = 0;	//当前选中的项，默认是第一项
	
	public MainListAdapter( Context context, String[] list )
	{
		this.mContext = context;
		this.gListData = new ArrayList<String>();
		this.selectItem = 0;
		
		for( int i = 0; i < list.length; i++ )
		{
			gListData.add(list[i]);
		}
		
		readSelectItemContent();	//此处需要加上tts朗读selectItem内容
	}
	
	public MainListAdapter( Context context, ArrayList<String> list )
	{
		this.mContext = context;
		this.gListData = list;
		this.selectItem = 0;
		
		readSelectItemContent();	//此处需要加上tts朗读selectItem内容
	}

	public void setSelectItem( int selectItem )
	{
		this.selectItem = selectItem;
		
		readSelectItemContent();	//此处需要加上tts朗读selectItem内容
		
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
		
		readSelectItemContent();	//此处需要加上tts朗读selectItem内容
		
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
		
		readSelectItemContent();	//此处需要加上tts朗读selectItem内容
		
		this.notifyDataSetInvalidated();
	}
	
	//按了确定键
	public void enter()
	{
		//进入到selectItem对应的界面
		Intent intent;
		if(selectItem == 0){
			intent = new Intent(mContext,TxtActivity.class);
			intent.putExtra("isTxt", true);
		}else if(selectItem == 2){
			intent = new Intent(mContext,TxtActivity.class);
			intent.putExtra("isTxt", false);
		}else{
			intent = new Intent(mContext,DaisyActivity.class);
		}
		mContext.startActivity(intent);
	}
	
	//tts朗读selectItem内容
	private void readSelectItemContent()
	{
		TTSUtils.getInstance().speak(gListData.get(selectItem));
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
        
        int index = PublicUtils.getColorSchemeIndex();	//配色方案
        
        if( selectItem == position )	//选中
		{
        	convertView.setBackgroundResource(EbookConstants.SelectBkColorID[index]);
		}
		else
		{
			convertView.setBackgroundResource(R.color.transparent);
		}
		
    	if( !TextUtils.isEmpty( gListData.get(position) ) )
    	{
    		vh.tvMenu.setText( gListData.get(position) );
    	}
    	else
    	{
    		vh.tvMenu.setText( "" );
    	}
    	vh.tvMenu.setTextColor(mContext.getResources().getColor(EbookConstants.FontColorID[index]));
                
        return convertView;
	}

	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		int id = v.getId();
		int position = 0;
		
		String tag = (String)v.getTag();
		position = Integer.parseInt(tag);
		
		switch( id )
		{
			case R.id.menu:
				if( this.selectItem != position )
				{
					setSelectItem( position );
				}
				else
				{
					enter();	//进入下一级界面
				}
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
