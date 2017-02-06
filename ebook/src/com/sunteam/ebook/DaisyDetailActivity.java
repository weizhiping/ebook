package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.db.DatabaseManager;
import com.sunteam.ebook.entity.BookmarkInfo;
import com.sunteam.ebook.entity.DiasyNode;
import com.sunteam.ebook.entity.DiasySentenceNode;
import com.sunteam.ebook.entity.FileInfo;
import com.sunteam.ebook.util.DaisyFileReaderUtils;
import com.sunteam.ebook.util.EbookConstants;
import com.sunteam.ebook.util.PublicUtils;
import com.sunteam.ebook.view.MainView;

/**
 * Daisy索引界面
 * 
 * @author sylar
 */
public class DaisyDetailActivity extends Activity implements OnEnterListener {
	private static final String TAG = "DaisyDetailActivity";
	private static final int[] keyCodeList = { KeyEvent.KEYCODE_5,
			KeyEvent.KEYCODE_NUMPAD_5 };
	private FrameLayout mFlContainer = null;
	private MainView mMainView = null;
	private ArrayList<String> mMenuList = null;
	private ArrayList<DiasyNode> diasList;
	private DatabaseManager manager;
	private int catalog;// 1为txt文档，2为word文档,3为disay
	private FileInfo remberFile;
	private FileInfo fileInfo;
	private ArrayList<FileInfo> fileInfoList = null;
	private String path;
	private int position;
	private boolean isAutoPrePart = false;	//是否自动到上一个章节
	private boolean isAuto = false; // 是否自动进入阅读界面
	private boolean isResume = true;
	private BookmarkInfo mBookmarkInfo = null;
	private UpdateRemFileReceiver fileReceiver;
	private boolean isToBookStart = false;	//是否是自动跳到文件开头播放
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
		// WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //禁止休眠
		setContentView(R.layout.ebook_activity_main);
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		path = intent.getStringExtra("path");
		catalog = intent.getIntExtra("catalogType", 0);
		mBookmarkInfo = (BookmarkInfo) intent.getSerializableExtra("bookmark");
		
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		fileInfo = (FileInfo) getIntent().getSerializableExtra("fileinfo");
		diasList = (ArrayList<DiasyNode>) intent.getSerializableExtra("diasys");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra(
				"file_list");
		isAuto = intent.getBooleanExtra("isAuto", false);
		isAutoPrePart = intent.getBooleanExtra("isAutoPrePart", false);
		isToBookStart = intent.getBooleanExtra("isToBookStart", false);
		
		if( ( null == diasList ) || ( null == fileInfoList ) || ( 0 == diasList.size() ) || ( 0 == fileInfoList.size() ) )
		{
			PublicUtils.showToast(this, this.getString(R.string.ebook_file_does_not_exist), new PromptListener() {

				@Override
				public void onComplete() {
					// TODO 自动生成的方法存根
					finish();
				}
    		});
		}
		else
		{
			initViews(name);
			
			if( mBookmarkInfo != null )
			{
				position = 0;
				mMainView.setSelection(position, isAuto);
				if( diasList != null && diasList.size() > 0 )
				{
					for( int i = 0; i < diasList.size(); i++ )
					{
						if( mBookmarkInfo.seq == diasList.get(i).seq )
						{
							for( int j = 0; j < i; j++ )
							{
								mMainView.down(true);
							}
							mMainView.enter(isAuto);
							return;
						}
					}	//在当前节点中查找是否包含了书签节点
					
					for( int i = 0; i < diasList.size(); i++ )
					{
						if( isSeachBookmarkNode( mBookmarkInfo.seq, diasList.get(i).seq ) )
						{
							isResume = false;
							int selectItem = mMainView.getSelectItem();
							DiasyNode dias = diasList.get(selectItem);
							ArrayList<DiasyNode> list = DaisyFileReaderUtils.getInstance().getChildNodeList(dias.seq);
							Intent intent2 = new Intent(this,
									DaisyDetailActivity.class);
							intent2.putExtra("name", mMainView.getCurItem());
							intent2.putExtra("seq", dias.seq);
							intent2.putExtra("catalogType", catalog);
							intent2.putExtra("path", path);
							intent2.putExtra("file", remberFile);
							intent2.putExtra("fileinfo", fileInfo);
							intent2.putExtra("diasys", list);
							intent2.putExtra("file_list", fileInfoList);
							intent2.putExtra("isAuto", isAuto);
							intent2.putExtra("bookmark", mBookmarkInfo);
							startActivityForResult(intent2,
									EbookConstants.REQUEST_CODE);
							return;
						}
						
						if( mMainView.isDown() )
						{
							mMainView.down(true);
						}
					}	//在字节点中查找是否包含了书签节点
					
					return;
				}
			}	//为了实现书签跳转
			
			if (isAuto) {
				mMainView.enter(isAuto);
			}
			
			if( isAutoPrePart )
			{
				isAuto = true;
				while (mMainView.isDown()) 
				{
					mMainView.down(true);
				}
				
				int selectItem = mMainView.getSelectItem();
				DiasyNode dias = diasList.get(selectItem);
				ArrayList<DiasyNode> list = DaisyFileReaderUtils.getInstance().getChildNodeList(dias.seq);
				
				if (0 == list.size()) // 当前节点是叶子节点
				{
					mMainView.enter(true);
				} 
				else // 如果当前节点不是叶子节点，则直接进入
				{
					Intent intent2 = new Intent(this,
							DaisyDetailActivity.class);
					intent2.putExtra("name", mMainView.getCurItem());
					intent2.putExtra("seq", dias.seq);
					intent2.putExtra("catalogType", catalog);
					intent2.putExtra("path", path);
					intent2.putExtra("file", remberFile);
					intent2.putExtra("fileinfo", fileInfo);
					intent2.putExtra("diasys", list);
					intent2.putExtra("file_list", fileInfoList);
					intent2.putExtra("isAutoPrePart", true);
					startActivityForResult(intent2,
							EbookConstants.REQUEST_CODE);
				}
			}	//为了实现切换到上一个章节
		}
	}
	
	//查找curNodeSeq的子节点中是否有bookmarkSeq节点
	private boolean isSeachBookmarkNode( int bookmarkSeq, int curNodeSeq )
	{
		ArrayList<DiasyNode> list = DaisyFileReaderUtils.getInstance().getChildNodeList(curNodeSeq);
		for( int i = 0; i < list.size(); i++ )
		{
			if( bookmarkSeq == list.get(i).seq )
			{
				return	true;
			}
		}	//在当前节点中查找是否包含了书签节点
		
		for( int i = 0; i < list.size(); i++ )
		{
			if( isSeachBookmarkNode( bookmarkSeq, list.get(i).seq ) )
			{
				return	true;
			}
		}	//在字节点中查找是否包含了书签节点
		
		return	false;
	}

	private void initViews(String name) {
		mMenuList = new ArrayList<String>();
		initFiles();
		registerReceiver();
		manager = new DatabaseManager(this);
		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
		mMainView = new MainView(this, this, name, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		if( isToBookStart )
		{
			position = 0;
		}	//如果是跳到文件开头，则需要将反显置为第一项。
		mMainView.setSelection(position, isAuto);
	}

	// 初始化显示文件
	private void initFiles() {

		int size = diasList.size();
		for (int i = 0; i < size; i++) {
			DiasyNode node = diasList.get(i);
			mMenuList.add(node.name);
			if (null != remberFile && remberFile.diasyPath.contains(path)) {
				String[] diasys = remberFile.diasyFlag.split("_");
				int remSeq = Integer.valueOf(diasys[1]);
				if (hasNode(node, remSeq) || node.seq == remSeq){
					position = i;
				}
			}
		}
	}

	private void registerReceiver() {
		fileReceiver = new UpdateRemFileReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(EbookConstants.ACTION_UPDATE_FILE);
		registerReceiver(fileReceiver, filter);
	}
	
	private boolean hasNode(DiasyNode node, int remSeq) {
		boolean hasNode = false;
		ArrayList<DiasyNode> nodeList = DaisyFileReaderUtils.getInstance()
				.getChildNodeList(node.seq);
		if (null != nodeList) {
			for (DiasyNode n : nodeList) {
				if (remSeq == n.seq) {
					return true;
				} else {
					hasNode = hasNode(n, remSeq);
					if (hasNode) {
						return true;
					}
				}
			}
		}
		return hasNode;
	}

	@Override
	public void onPause() {
		if (mMainView != null) {
			mMainView.onPause();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		if (isResume) {
			if (mMainView != null) {
				mMainView.onResume();
			}
		}

		isResume = true;

		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mMainView.onKeyDown(keyCode, event, keyCodeList);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mMainView.onKeyUp(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCELED);
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onEnterCompleted(int selectItem, String menu, boolean isAuto) {
		isResume = !isAuto;
		DiasyNode dias = diasList.get(selectItem);
		ArrayList<DiasyNode> diaysList = DaisyFileReaderUtils.getInstance()
				.getChildNodeList(dias.seq);
		int size = diaysList.size();
		if ((0 == size) || isAuto) {
			if( size > 0 )
			{
				ArrayList<DiasySentenceNode> list = DaisyFileReaderUtils.getInstance().getDiasySentenceNodeList(path, dias.seq);
				if( ( null == list ) || ( 0 == list.size() ) )
				{
					Intent intent = new Intent(this, DaisyDetailActivity.class);
					intent.putExtra("name", menu);
					intent.putExtra("seq", dias.seq);
					intent.putExtra("catalogType", catalog);
					intent.putExtra("path", path);
					intent.putExtra("file", remberFile);
					intent.putExtra("fileinfo", fileInfo);
					intent.putExtra("diasys", diaysList);
					intent.putExtra("file_list", fileInfoList);
					intent.putExtra("isAuto", isAuto);
					if( mBookmarkInfo != null )
					{
						intent.putExtra("bookmark", mBookmarkInfo);
					}
					startActivityForResult(intent, EbookConstants.REQUEST_CODE);
					
					return;
				}	//有些父节点本身没有句子，则需要进入子节点列表。
			}
			manager.updateQueryBook(fileInfo, 0);
			Intent intent = new Intent(this, ReadDaisyActivity.class);
			intent.putExtra("name", menu);
			intent.putExtra("path", path);
			intent.putExtra("node", dias);
			intent.putExtra("fileinfo", fileInfo);
			intent.putExtra("file_list", fileInfoList);
			intent.putExtra("isAuto", isAuto);
			if( mBookmarkInfo != null )
			{
				intent.putExtra("bookmark", mBookmarkInfo);
			}
			startActivityForResult(intent, EbookConstants.REQUEST_CODE);
		} else {
			Intent intent = new Intent(this, DaisyDetailActivity.class);
			intent.putExtra("name", menu);
			intent.putExtra("seq", dias.seq);
			intent.putExtra("catalogType", catalog);
			intent.putExtra("path", path);
			intent.putExtra("file", remberFile);
			intent.putExtra("fileinfo", fileInfo);
			intent.putExtra("diasys", diaysList);
			intent.putExtra("file_list", fileInfoList);
			intent.putExtra("isAuto", isAuto);
			if( mBookmarkInfo != null )
			{
				intent.putExtra("bookmark", mBookmarkInfo);
			}
			startActivityForResult(intent, EbookConstants.REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EbookConstants.REQUEST_CODE: // 阅读器返回
			if (RESULT_OK == resultCode) {
				if( null == data )
				{
					break;
				}
				int next = data
						.getIntExtra("next", EbookConstants.TO_NEXT_PART);
				int seq = data.getIntExtra("seq", -1);

				switch (next) {
				case EbookConstants.TO_BOOK_MARK:	// 到一本书的书签
				{
					isResume = false;
					Intent intent = new Intent();
					intent.putExtra("next", EbookConstants.TO_BOOK_MARK);
					intent.putExtra("seq", seq);
					intent.putExtra("line", data.getIntExtra("line", 0));
					intent.putExtra("start", data.getIntExtra("start", 0));
					intent.putExtra("len", data.getIntExtra("len", 0));
					setResult(RESULT_OK, intent);
					finish();
				}
					break;
				case EbookConstants.TO_BOOK_START: // 到一本书的开头
				{
					isResume = false;
					Intent intent = new Intent();
					intent.putExtra("next", EbookConstants.TO_BOOK_START);
					setResult(RESULT_OK, intent);
					finish();
				}
					break;
				case EbookConstants.TO_PRE_PART: // 到上一个部分
					isResume = false;
					if (-1 == seq) {
						int selectItem = mMainView.getSelectItem();
						DiasyNode dias = diasList.get(selectItem);
						ArrayList<DiasySentenceNode> list = DaisyFileReaderUtils.getInstance().getDiasySentenceNodeList(path, dias.seq);
						if( ( null == list ) || ( 0 == list.size() ) )	//此节点只有标题，没有内容。
						{
							//继续执行以下代码。
						}
						else
						{
							mMainView.enter(true);
							break;
						}
					}
					
					{
						if (mMainView.isUp()) {
							mMainView.up(true);
							
							int selectItem = mMainView.getSelectItem();
							DiasyNode dias = diasList.get(selectItem);
							ArrayList<DiasyNode> diaysList = DaisyFileReaderUtils.getInstance()
									.getChildNodeList(dias.seq);
							if (0 == diaysList.size()) // 上一节点是叶子节点
							{
								mMainView.enter(true);
							}
							else
							{
								Intent intent = new Intent(this,
										DaisyDetailActivity.class);
								intent.putExtra("name", mMainView.getCurItem());
								intent.putExtra("seq", dias.seq);
								intent.putExtra("catalogType", catalog);
								intent.putExtra("path", path);
								intent.putExtra("file", remberFile);
								intent.putExtra("fileinfo", fileInfo);
								intent.putExtra("diasys", diaysList);
								intent.putExtra("file_list", fileInfoList);
								intent.putExtra("isAutoPrePart", true);
								startActivityForResult(intent,
										EbookConstants.REQUEST_CODE);
							}
						} else {
							Intent intent = new Intent();
							intent.putExtra("next",
									EbookConstants.TO_PRE_PART);
							setResult(RESULT_OK, intent);
							finish();
						}
					}
					break;
				case EbookConstants.TO_NEXT_PART: // 到下一个部分
					isResume = false;
					if (-1 == seq) {
						if (mMainView.isDown()) {
							mMainView.down(true);
							mMainView.enter(true);
						} else {
							Intent intent = new Intent();
							intent.putExtra("next", EbookConstants.TO_NEXT_PART);
							setResult(RESULT_OK, intent);
							finish();
						}
					} else {
						ArrayList<DiasyNode> diaysList = DaisyFileReaderUtils
								.getInstance().getChildNodeList(seq);

						if (0 == diaysList.size()) // 当前节点是叶子节点
						{
							if (mMainView.isDown()) {
								mMainView.down(true);
								mMainView.enter(true);
							} else {
								Intent intent = new Intent();
								intent.putExtra("next",
										EbookConstants.TO_NEXT_PART);
								setResult(RESULT_OK, intent);
								finish();
							}
						} 
						else // 如果当前节点不是叶子节点，则直接进入
						{
							Intent intent = new Intent(this,
									DaisyDetailActivity.class);
							intent.putExtra("name", mMainView.getCurItem());
							intent.putExtra("seq", seq);
							intent.putExtra("catalogType", catalog);
							intent.putExtra("path", path);
							intent.putExtra("file", remberFile);
							intent.putExtra("fileinfo", fileInfo);
							intent.putExtra("diasys", diaysList);
							intent.putExtra("file_list", fileInfoList);
							intent.putExtra("isAuto", true);
							startActivityForResult(intent,
									EbookConstants.REQUEST_CODE);
						}
					}
					break;
				case EbookConstants.TO_NEXT_BOOK: // 到下一本书
					isResume = false;
					Intent intent = new Intent();
					intent.putExtra("next", EbookConstants.TO_NEXT_BOOK);
					setResult(RESULT_OK, intent);
					finish();
					break;
				default:
					break;
				}
			} // 阅读下一个部分
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if( fileReceiver != null )
		{
			unregisterReceiver(fileReceiver);
		}
	}
	
	private class UpdateRemFileReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(EbookConstants.ACTION_UPDATE_FILE)) {
				remberFile = manager.queryLastBook(EbookConstants.BOOK_RECENT);
		//		flagType = remberFile.flag;
			}
		}
	}
}
