package com.sunteam.ebook.adapter;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sunteam.ebook.R;
import com.sunteam.ebook.ReadTxtActivity;
import com.sunteam.ebook.TxtDetailActivity;
import com.sunteam.ebook.TxtPartActivity;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.util.TextFileReaderUtils;

/**
 * 文档详细列表类
 * 
 * @author sylar
 * 
 */
public class TxtDetailListAdapter extends BaseAdapter implements
		OnClickListener {
	private Context mContext = null;
	private ArrayList<FileInfo> gListData = null;
	private int selectItem = 0; // 当前选中的项，默认是第一项
	private DatabaseManager manager;

	public TxtDetailListAdapter(Context context, ArrayList<FileInfo> list) {
		this.mContext = context;
		this.gListData = list;
		this.selectItem = 0;
		manager = new DatabaseManager(mContext);
		readSelectItemContent(); // 此处需要加上tts朗读selectItem内容
	}

	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;

		readSelectItemContent(); // 此处需要加上tts朗读selectItem内容

		this.notifyDataSetInvalidated();
	}

	// 按了上键
	public void up() {
		if (this.selectItem > 0) {
			this.selectItem--;
		} else {
			this.selectItem = gListData.size() - 1;
		}

		readSelectItemContent(); // 此处需要加上tts朗读selectItem内容

		this.notifyDataSetInvalidated();
	}

	// 按了下键
	public void down() {
		if (this.selectItem < gListData.size() - 1) {
			this.selectItem++;
		} else {
			this.selectItem = 0;
		}

		readSelectItemContent(); // 此处需要加上tts朗读selectItem内容

		this.notifyDataSetInvalidated();
	}

	// 按了确定键
	public void enter() {
		// 进入到selectItem对应的界面
		FileInfo fileInfo = gListData.get(selectItem);
		if (fileInfo.isFolder) {
			Intent intent = new Intent(mContext, TxtDetailActivity.class);
			intent.putExtra("path", fileInfo.path);
			intent.putExtra("name", fileInfo.name);
			intent.putExtra("flag", 10);
			mContext.startActivity(intent);
		} else {
			try {
				TextFileReaderUtils.getInstance().init(fileInfo.path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int count = TextFileReaderUtils.getInstance().getParagraphCount(); // 得到分段信息

			if (0 == count){ // 文件为空
			
				// 提示一下（语音和文字）
			} else if (1 == count){ // 只有一部分
			
				Intent intent = new Intent(mContext, ReadTxtActivity.class);
				intent.putExtra("path", fileInfo.path); // 路径
				intent.putExtra("part", 0); // 第几部分
				mContext.startActivity(intent);
				manager.insertBookToDb(fileInfo, 2);
			} else {
				// 根据count数量显示一个list，内容形如：第1部分 第2部分 ... 第n部分
				Intent intent = new Intent(mContext, TxtPartActivity.class);
				intent.putExtra("name", fileInfo.name); // 路径
				intent.putExtra("count", count); // 第几部分
				mContext.startActivity(intent);
				manager.insertBookToDb(fileInfo, 2);
			}
		}
	}

	// tts朗读selectItem内容
	private void readSelectItemContent() {
	//	 TTSUtils.getInstance().speak(gListData.get(selectItem).name);
	}

	public ArrayList<FileInfo> getListData() {
		return gListData;
	}

	public void setListData(ArrayList<FileInfo> list) {
		gListData = list;
	}

	@Override
	public int getCount() {
		if (null == gListData) {
			return 0;
		}
		return gListData.size();
	}

	@Override
	public Object getItem(int position) {
		if (null == gListData) {
			return null;
		}
		return gListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (null == gListData) {
			return 0;
		}
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;

		if (null == convertView) {
			vh = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.list_item_menu, null);

			vh.tvMenu = (TextView) convertView.findViewById(R.id.menu);
			vh.tvMenu.setTag(String.valueOf(position));
			vh.tvMenu.setOnClickListener(this);

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		int index = PublicUtils.getColorSchemeIndex(); // 配色方案

		if (selectItem == position) // 选中
		{
			convertView
					.setBackgroundResource(EbookConstants.SelectBkColorID[index]);
		} else {
			convertView.setBackgroundResource(R.color.transparent);
		}
		FileInfo fileInfo = gListData.get(position);
		vh.tvMenu.setText(fileInfo.name);
		vh.tvMenu.setTextColor(mContext.getResources().getColor(
				EbookConstants.FontColorID[index]));

		return convertView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		int position = 0;

		String tag = (String) v.getTag();
		position = Integer.parseInt(tag);

		switch (id) {
		case R.id.menu:
			if (this.selectItem != position) {
				setSelectItem(position);
			} else {
				enter(); // 进入下一级界面
			}
			break;
		default:
			break;
		}
	}

	private class ViewHolder {
		TextView tvMenu = null; // 菜单名称
	}
}
