package com.sunteam.ebook.view;

import android.content.Context;  
import android.util.AttributeSet;  
import android.widget.TextView;  

/**
 * 不用获得焦点也能实现跑马灯效果的TextView
 * 
 * @author wzp
 *
 */
public class ScrollForeverTextView extends TextView 
{  
	public ScrollForeverTextView(Context context) 
	{  
		super(context);  
		// TODO Auto-generated constructor stub  
	}  
  
	public ScrollForeverTextView(Context context, AttributeSet attrs) 
	{  
		super(context, attrs);  
	}  
  
	public ScrollForeverTextView(Context context, AttributeSet attrs, int defStyle) 
	{  
		super(context, attrs, defStyle);  
	}  
  
	@Override  
	public boolean isFocused() 
	{  
		return true;  
	}
}
