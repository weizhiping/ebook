package com.sunteam.service.tts;

interface TtsServiceCallback {
	//客户端给服务器端回调的函数, 前提是当客户端获取的IBinder接口的时候,要去注册回调函数, 只有这样, 服务器端才知道该调用哪些函数
	void onInit(int code); // 初始化

	void onSpeakBegin(); // 发音开始

	void onSpeakPaused(); // 暂停合成

	void onSpeakResumed(); // 恢复合成

	void onBufferProgress(int percent, int beginPos, int endPos, String info); // 合成进度

	void onSpeakProgress(int percent, int beginPos, int endPos); // 发音进度

	void onCompleted(String error); // 发音结束
}
