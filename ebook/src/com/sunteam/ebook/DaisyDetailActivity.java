package com.sunteam.ebook;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.sunteam.common.utils.dialog.PromptListener;
import com.sunteam.ebook.adapter.MainListAdapter.OnEnterListener;
import com.sunteam.ebook.entity.DiasyNode;
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
	private int catalog;// 1为txt文档，2为word文档,3为disay
	private FileInfo remberFile;
	private FileInfo fileInfo;
	private ArrayList<FileInfo> fileInfoList = null;
	private String path;
	private int seq;
	private int position;
	private boolean isAuto = false; // 是否自动进入阅读界面
	private boolean isResume = true;

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
		seq = intent.getIntExtra("seq", -1);
		remberFile = (FileInfo) getIntent().getSerializableExtra("file");
		fileInfo = (FileInfo) getIntent().getSerializableExtra("fileinfo");
		diasList = (ArrayList<DiasyNode>) intent.getSerializableExtra("diasys");
		fileInfoList = (ArrayList<FileInfo>) getIntent().getSerializableExtra(
				"file_list");
		isAuto = intent.getBooleanExtra("isAuto", false);
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
			if (isAuto) {
				mMainView.enter(isAuto);
			}
		}
	}

	private void initViews(String name) {
		mMenuList = new ArrayList<String>();
		initFiles();
		mFlContainer = (FrameLayout) this.findViewById(R.id.ebook_fl_container);
		mMainView = new MainView(this, this, name, mMenuList);
		mFlContainer.removeAllViews();
		mFlContainer.addView(mMainView.getView());
		mMainView.setSelection(position);
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
				if (hasNode(node, remSeq) || node.seq == remSeq)
					position = i;
			}
		}
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
			setResult(RESULT_OK);
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
			Intent intent = new Intent(this, ReadDaisyActivity.class);
			intent.putExtra("name", menu);
			intent.putExtra("path", path);
			intent.putExtra("node", dias);
			intent.putExtra("fileinfo", fileInfo);
			intent.putExtra("file_list", fileInfoList);
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
			startActivity(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EbookConstants.REQUEST_CODE: // 阅读器返回
			if (RESULT_OK == resultCode) {
				int next = data
						.getIntExtra("next", EbookConstants.TO_NEXT_PART);
				int seq = data.getIntExtra("seq", -1);

				switch (next) {
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
					boolean isEnter = data.getBooleanExtra("isEnter", false);

					if (isEnter) {
						mMainView.enter(true);
						break;
					}

					if (mMainView.isUp()) {
						mMainView.up(true);
						mMainView.enter(true);
					} else {
						Intent intent = new Intent();
						intent.putExtra("next", EbookConstants.TO_PRE_PART);
						intent.putExtra("isEnter", true);
						setResult(RESULT_OK, intent);
						finish();
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
						} else // 如果当前节点不是叶子节点，则直接进入
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
}
