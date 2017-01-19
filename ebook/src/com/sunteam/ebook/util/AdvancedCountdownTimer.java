package com.sunteam.ebook.util;

import android.os.Handler;

/**
 * 自定义倒计时类。
 * 
 * @author wzp
 */
import android.os.Message;

public abstract class AdvancedCountdownTimer 
{
	private final long mCountdownInterval;
	private long mTotalTime;
	private long mRemainTime;
	private long mStartTime;

	public AdvancedCountdownTimer(long millisInFuture, long countDownInterval) 
	{
		mTotalTime = millisInFuture;
		mCountdownInterval = countDownInterval;
		mRemainTime = millisInFuture;
	}

	public final void seek(int value) 
	{
		synchronized (AdvancedCountdownTimer.this) 
		{
			mRemainTime = ((100 - value) * mTotalTime) / 100;
		}
	}

	public final void cancel() 
	{
		mHandler.removeMessages(MSG_RUN);
		mHandler.removeMessages(MSG_PAUSE);
		mHandler.removeMessages(MSG_RESUME);
	}

	public final void resume() 
	{
		mStartTime = System.currentTimeMillis();
		mHandler.removeMessages(MSG_PAUSE);
		mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_RESUME));
	}

	public final void pause() 
	{
		mHandler.removeMessages(MSG_RUN);
		mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_PAUSE));
	}

	public synchronized final AdvancedCountdownTimer start() 
	{
		if (mRemainTime <= 0) 
		{
			onFinish();
			return this;
		}
		mStartTime = System.currentTimeMillis();
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN), mCountdownInterval);
		
		return this;
	}

	public abstract void onTick(long millisUntilFinished, int percent);
	public abstract void onFinish();
	private static final int MSG_RUN = 1;
	private static final int MSG_PAUSE = 2;
	private static final int MSG_RESUME = 3;

	private Handler mHandler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			synchronized (AdvancedCountdownTimer.this) 
			{
				if (msg.what == MSG_RUN)
				{
					mRemainTime = mRemainTime - mCountdownInterval;
					mStartTime = System.currentTimeMillis();

					if (mRemainTime <= 0) 
					{
						onFinish();
					} 
					else if (mRemainTime < mCountdownInterval) 
					{
						sendMessageDelayed(obtainMessage(MSG_RUN), mRemainTime);
					} 
					else 
					{
						onTick(mRemainTime, new Long(100 * (mTotalTime - mRemainTime) / mTotalTime) .intValue());
						sendMessageDelayed(obtainMessage(MSG_RUN), mCountdownInterval);
					}
				} 
				else if (msg.what == MSG_PAUSE) 
				{
					long time = System.currentTimeMillis()+50;
					mRemainTime = mRemainTime - (time-mStartTime);
				}
				else if (msg.what == MSG_RESUME) 
				{
					if (mRemainTime <= 0) 
					{
						onFinish();
					} 
					else if (mRemainTime < mCountdownInterval) 
					{
						sendMessageDelayed(obtainMessage(MSG_RUN), mRemainTime);
					} 
					else 
					{
						onTick(mRemainTime, new Long(100 * (mTotalTime - mRemainTime) / mTotalTime) .intValue());
						sendMessageDelayed(obtainMessage(MSG_RUN), mCountdownInterval);
					}
				}
			}
		}
	};
}
