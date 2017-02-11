package com.sunteam.service.tts;

import com.sunteam.service.tts.TtsRecognizerCallback;

interface TtsRecognizerManage {
	void setSpeechLanguage(String lang);

	void updateLocalLexicon(String content);

	void recognizeSpeech();

	// 用来注册回调的对象
	void registerCallback(TtsRecognizerCallback cb);

	void unregisterCallback(TtsRecognizerCallback cb);

}