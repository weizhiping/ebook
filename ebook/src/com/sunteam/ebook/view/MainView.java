package com.sunteam.ebook.view;

import java.util.ArrayList;

import com.sunteam.common.utils.Tools;
import com.sunteam.ebook.R;
import com.sunteam.ebook.adapter.MainListAdapter;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.TTSSpeakMode;
import com.sunteam.ebook.util.CallbackBundle;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.TTSUtils;
import com.sunteam.ebook.util.TTSUtils.OnTTSListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


/**
 * 主要界面公用View
 * 
 * @author wzp
 *
 */

public class MainView extends View implements OnTTSListener
{
	private static final String TAG = "MainView";
	
	private Context mContext = null;
	private View mView = null;
	private TextView mTvTitle = null;
	private View mLine = null;
	private ListView mLvMenu = null;
	private MainListAdapter mAdapter = null;
	
	public View getView()
	{
		return	mView;
	}
	
	private void initView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList, TTSSpeakMode mode )
	{
		mContext = context;
		mView = LayoutInflater.from(context).inflate( R.layout.view_main, null );
		
		mTvTitle = (TextView)mView.findViewById(R.id.title);		//标题栏
    	mLine = (View)mView.findViewById(R.id.line);				//分割线
    	mLvMenu = (ListView)mView.findViewById(R.id.menu_list);		//listview
    	
    	Tools tools = new Tools(context);
    	
    	mView.setBackgroundColor(tools.getBackgroundColor());		//设置View的背景色
    	mTvTitle.setTextColor(tools.getFontColor());				//设置title的背景色
    	mLine.setBackgroundColor(tools.getFontColor());				//设置分割线的背景色
    	
    	final float scale = context.getResources().getDisplayMetrics().density/0.75f;	//计算相对于ldpi的倍数;
    	float fontSize = tools.getFontSize() * scale;
    	mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize-2*EbookConstants.LINE_SPACE*scale); // 设置title字号 
    	mTvTitle.setHeight((int)fontSize); // 设置控件高度
    	mTvTitle.setText(title);
    	
    	mAdapter = new MainListAdapter( mContext, listener, menuList, mode );
    	mLvMenu.setAdapter(mAdapter);
    	mLvMenu.setFocusable(false);	//不获取焦点
	}
	
	public MainView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList, TTSSpeakMode mode )
	{
		super(context);
		
		initView( context, listener, title, menuList, mode );
	}
	
	public MainView( final Context context, OnEnterListener listener, final String title, ArrayList<String> menuList)
	{
		super(context);
		
		initView( context, listener, title, menuList, TTSSpeakMode.READ_MODE_NORMAL );
	}
	
	public void setSelection(int position){
		mAdapter.setSelectItem(position);
	}
	
	public void onPause()
	{
		TTSUtils.getInstance().OnTTSListener(null);
	}
	
	public void onResume()
	{
		TTSUtils.getInstance().OnTTSListener(this);
		if( mAdapter != null )
		{
			TTSUtils.getInstance().speakTips(mAdapter.getSelectItemContent());
		}
	}
	
	public void up()
	{
		mAdapter.up();
	}
	
	public void down()
	{
		mAdapter.down();
	}
	
	public void enter()
	{
		mAdapter.enter(false);
	}

	public void enter( boolean isAuto )
	{
		mAdapter.enter(isAuto);
	}
	
	public void updateAdapter()
	{
		mAdapter.notifyDataSetChanged();
	}
	
	public boolean isDown()
	{
		return	mAdapter.isDown();	
	}
	
	public String getCurItem()
	{
		return	mAdapter.getCurItem();
	}
	
	public int getSelectItem()
	{
		return	mAdapter.getSelectItem();
	}

	private boolean isScanning = true;
	private long firstTime = 0;
	private long lastTime = 0;	//按键时间，处理长按键：按住不放时，每间隔1秒处理一次按键
	private boolean keyUpFlag = false;
	private int longKeyCode = 0; //长按键值，0 表示没有按键; 只处理上键和下键
	
	private boolean isScanning() 
	{
		return isScanning;
	}

	private void setScanning(boolean isScanning) 
	{
		this.isScanning = isScanning;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event )
	{
		return	onKeyDown( keyCode, event, null, null );	
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event, int[] keyCodeList )
	{
		return	onKeyDown( keyCode, event, keyCodeList, null );	
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event, int[] keyCodeList, CallbackBundle callbackBundle) 
	{
		long time = event.getEventTime();

		if( keyUpFlag ) 
		{
			keyUpFlag = false;
			longKeyCode = 0;
			setScanning(false);
		}

		if( 0 == event.getRepeatCount() ) 
		{
			firstTime = time;
			lastTime = time;
			setScanning(false);
			processKeyEnevt(keyCode, event, keyCodeList, callbackBundle);
		}
		else if (time - lastTime >= 1000) 
		{
			lastTime = time;
			processKeyEnevt(keyCode, event, keyCodeList, callbackBundle);
		}

		if( (KeyEvent.KEYCODE_DPAD_UP == keyCode || KeyEvent.KEYCODE_DPAD_DOWN == keyCode) && time - firstTime >= 2000 ) 
		{
			if (!isScanning()) 
			{
				longKeyCode = keyCode;
				setScanning(true);
			}
		}

		return false;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{
		keyUpFlag = true;
		
		return false;
	}
	
	private boolean processKeyEnevt(int keyCode, KeyEvent event, int[] keyCodeList, CallbackBundle callbackBundle) 
	{
		int action = event.getAction();

		// 虽然按下时间到达了自动浏览的条件，但没有抬起的情况下，还是按重复按键处理，直到抬起后才按自动浏览处理。
		if (isScanning() && keyUpFlag) 
		{
			return true;
		}

		if( KeyEvent.ACTION_DOWN == action ) 
		{
			switch (keyCode) 
			{
				case KeyEvent.KEYCODE_BACK:
					Activity activity = (Activity)mContext;
					activity.finish();
					return	true;
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					mAdapter.enter(false);
					return true;
				case KeyEvent.KEYCODE_DPAD_UP:
					mAdapter.up();
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					mAdapter.down();
					return true;
				case KeyEvent.KEYCODE_5:
				case KeyEvent.KEYCODE_NUMPAD_5:		//直接进入阅读器界面
					if( keyCodeList != null )
					{
						for( int i = 0; i < keyCodeList.length; i++ )
						{
							if( keyCodeList[i] == keyCode )
							{
								mAdapter.enter(true);
								return	true;
							}
						}
					}
					break;
				case KeyEvent.KEYCODE_MENU:
					if( keyCodeList != null )
					{
						for( int i = 0; i < keyCodeList.length; i++ )
						{
							if( keyCodeList[i] == keyCode )
							{
								if( callbackBundle != null )
								{
									Bundle bundle = new Bundle();
									bundle.putInt("keyCode", keyCode);
									callbackBundle.callback(bundle);
									return	true;
								}
							}
						}
					}
					break;
				default:
					break;
			}
		} 
		else if (KeyEvent.ACTION_UP == action) 
		{
			switch (keyCode) 
			{
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					break;
				default:
					break;
			}
		}
		
		return false;
	}
	
	private void processLongKey() 
	{
		if( KeyEvent.KEYCODE_DPAD_UP == longKeyCode ) 
		{
			mAdapter.up();
		} 
		else if (KeyEvent.KEYCODE_DPAD_DOWN == longKeyCode) 
		{
			mAdapter.down();
		} 
		else 
		{
			setScanning(false);
			longKeyCode = 0;
		}
	}

	@Override
	public void onSpeakCompleted() 
	{
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(0);
	}

	@Override
	public void onSpeakError() 
	{
		// TODO Auto-generated method stub	
	}
	
	private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) 
        {
            switch (msg.what) 
            {
                case 0:
                	processLongKey();
                    break;
                default:
                    break;
            }
            return false;
        }
    }); 	
}