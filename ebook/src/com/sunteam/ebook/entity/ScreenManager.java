package com.sunteam.ebook.entity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
/**
 * 管理功能菜单界面退出
 * @author ljw
 *
 */
public class ScreenManager {
	 private List<Activity> activities = new ArrayList<Activity>();
	private static ScreenManager instance;

	private ScreenManager() {
	}

	public static ScreenManager getScreenManager() {
		if (instance == null) {
			instance = new ScreenManager();
		}
		return instance;
	}

	// 退出栈顶Activity
	public void popActivity(Activity activity) {
		if (activity != null) {
			activity.finish();
			activities.remove(activity);
			activity = null;
		}
	}


	// 将当前Activity推入栈中
	public void pushActivity(Activity activity) {
		activities.add(activity);
	}

	// 退出栈中所有Activity
	public void popAllActivityExceptOne() {
		for (Activity activity : activities) {
            if (activity!=null) {
                activity.finish();
            }
        }
	}
}
