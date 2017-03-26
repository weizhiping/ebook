package com.sunteam.service.tts;

import com.sunteam.service.tts.TtsServiceCallback;

interface SunteamTtsManage {
	String getParameter(String key);
	boolean isSpeaking();
	void pause();
	void repeat();
	void resume();
	void setParameter(String key, String value);
	void speak(String s, int mode);
	void stop();
	boolean getState();
	void setState(boolean state);
	int getSunteamParameter();
	void setSunteamParameter(int value);

	// 用来注册回调的对象
	void registerCallback(TtsServiceCallback cb);
    void unregisterCallback(TtsServiceCallback cb);
}
