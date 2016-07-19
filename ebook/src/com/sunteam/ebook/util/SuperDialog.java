package com.sunteam.ebook.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.sunteam.ebook.R;

/**
 * dialog弹出框的统一管理
 * 
 * @author Sylar
 * 
 */
public class SuperDialog implements OnClickListener{
	private Dialog dialog;
	private Context context;
	private DialogCallBack callBack;
	private TextView titleText,contentText;

	// 确定后回调方法
	public interface DialogCallBack {
		public void dialogConfrim();
	}

	public void initeCallBack(DialogCallBack call) {
		callBack = call;   
	}

	public SuperDialog(Context c) {
		this.context = c;
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_super);
		titleText = (TextView) dialog.findViewById(R.id.dialog_title);
		contentText = (TextView) dialog.findViewById(R.id.dialog_content);
		Button cancelBtn = (Button) dialog.findViewById(R.id.dialog_cancel);
		Button confrimBtn = (Button) dialog.findViewById(R.id.dialog_confrim);
		cancelBtn.setOnClickListener(this);
		confrimBtn.setOnClickListener(this);
	}

	public void showTitle(int titleId) {
		titleText.setText(titleId);
		titleText.setVisibility(View.VISIBLE);
	}

	/**
	 * 通用的Dialog
	 * 
	 * @param context
	 * @param flage
	 * @param msg
	 * @return
	 */
	public void showSuperDialog(int contentId) {
		contentText.setText(contentId);
		dialog.show();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.dialog_cancel:
			dialog.dismiss();
			break;
		case R.id.dialog_confrim:
			dialog.dismiss();
			if(null != callBack)
				callBack.dialogConfrim();
			break;
		}
	}
}
