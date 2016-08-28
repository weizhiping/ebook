package com.sunteam.ebook.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sunteam.common.utils.Tools;
import com.sunteam.ebook.R;
import com.sunteam.ebook.entity.TTSSpeakMode;
import com.sunteam.ebook.util.EbookConstants;
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
	private ListView mLv = null;
	private ArrayList<String> gListData = null;
	private int selectItem = 0;	//当前选中的项，默认是第一项
	private OnEnterListener mOnEnterListener = null;
	private TTSSpeakMode mode;
	private Tools mTools;
	private float mScale = 1.0f;
	
	public interface OnEnterListener 
	{
		public void onEnterCompleted( int selectItem, String menu, boolean isAuto );
	}

	//设置确定监听器
	public void setOnEnterListener( OnEnterListener listener )
	{
		mOnEnterListener = listener;
	}
	
	public MainListAdapter( Context context, ListView lv, OnEnterListener listener, ArrayList<String> list, TTSSpeakMode mode )
	{
		this.mContext = context;
		this.mLv = lv;
		this.gListData = list;
		this.selectItem = 0;
		this.mOnEnterListener = listener;
		this.mode = mode;
		this.mTools = new Tools(mContext);
		this.mScale = context.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数
	}

	public void setSelectItem( int selectItem )
	{
		this.selectItem = selectItem;
		
		readSelectItemContent();	//此处需要加上tts朗读selectItem内容
		
		this.notifyDataSetChanged();
	}
	
	public int getSelectItem()
	{
		return	this.selectItem;
	}
	
	public String getSelectItemContent()
	{
		if( gListData != null && gListData.size() > 0)
		{
			return	gListData.get(selectItem);
		}
		
		return	"";
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
		
		int first = mLv.getFirstVisiblePosition();
		if( selectItem < first )
		{
			int select = mLv.getSelectedItemPosition();
			mLv.setSelection(--select);
		}
		else if( selectItem == gListData.size() - 1 )
		{
			mLv.setSelection(selectItem);
		}
		
		this.notifyDataSetChanged();
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
		
		int last = mLv.getLastVisiblePosition();
		if( selectItem > last )
		{
			int select = mLv.getSelectedItemPosition();
			mLv.setSelection(++select);
		}
		else if( 0 == selectItem )
		{
			mLv.setSelection(0);
		}
		
		this.notifyDataSetChanged();
	}
	
	//是否还有下一项
	public boolean isDown()
	{
		if( this.selectItem < gListData.size() - 1 )
		{
			return	true;
		}
		else
		{
			return	false;
		}
	}
	
	//得到当前反显条目
	public String getCurItem()
	{
		return	gListData.get(selectItem);
	}
	
	//按了确定键
	public void enter(boolean isAuto)
	{
		if( mOnEnterListener != null )
		{
			mOnEnterListener.onEnterCompleted( this.selectItem, this.gListData.get(selectItem), isAuto );
		}
	}
	
	//tts朗读selectItem内容
	private void readSelectItemContent()
	{
		switch( this.mode )
		{
			case READ_MODE_NORMAL:		//普通模式
				TTSUtils.getInstance().speakTips(gListData.get(selectItem));
				break;
			case READ_MODE_CN:			//中文模式
				TTSUtils.getInstance().testRoleCn(gListData.get(selectItem), gListData.get(selectItem));
				break;
			case READ_MODE_EN:			//英文模式
				TTSUtils.getInstance().testRoleEn(gListData.get(selectItem), gListData.get(selectItem));
				break;
			case READ_MODE_SPEED:		//语速模式
				int speed = Integer.parseInt(gListData.get(selectItem));
				TTSUtils.getInstance().testSpeed(speed, mContext.getString(R.string.tts_speed)+speed);
				break;
			case READ_MODE_PITCH:		//语调模式
				int pitch = Integer.parseInt(gListData.get(selectItem));
				TTSUtils.getInstance().testPitch(pitch, mContext.getString(R.string.tts_pitch)+pitch);
				break;
			case READ_MODE_VOLUME:		//音量模式
				int volume = Integer.parseInt(gListData.get(selectItem));
				TTSUtils.getInstance().testVolume(volume, mContext.getString(R.string.tts_volume)+volume);
				break;
			case READ_MODE_EFFECT:		//音效模式
				TTSUtils.getInstance().testEffect(gListData.get(selectItem), gListData.get(selectItem));
				break;
			default:
				break;
		}
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
    		vh.tvMenu.setOnClickListener(this);

        	convertView.setTag(vh);
        }
        else
        {
        	vh = (ViewHolder) convertView.getTag();
        }
        
        vh.tvMenu.setTag(String.valueOf(position));
        
        if( selectItem == position )	//选中
		{
        	convertView.setBackgroundColor(mTools.getHighlightColor());
		}
		else
		{
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}
		
        float fontSize = mTools.getFontSize()*mScale;
    	vh.tvMenu.setTextColor(mTools.getFontColor());	//设置字体颜色
    	vh.tvMenu.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*mScale);	//设置字体大小
    	vh.tvMenu.setHeight((int)fontSize); 		// 设置控件高度
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
					enter(false);	//进入下一级界面
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
