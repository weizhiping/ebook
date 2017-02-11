package com.sunteam.service.tts;

interface TtsRecognizerCallback {
 	// 语法构建成功回调
	void onBuildFinish();

 	// 更新词典
	void onLexiconUpdated(String lexiconId, String error);

	void onVolumeChanged(int volume, in byte[] data);

	// 语音识别成功回调
	void onResult(String result, boolean isLast);

	void onBeginOfSpeech();

	void onEndOfSpeech();

	void onError(String error);

}
