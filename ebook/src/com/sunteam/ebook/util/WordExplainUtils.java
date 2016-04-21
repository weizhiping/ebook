package com.sunteam.ebook.util;

import java.io.InputStream;

import android.content.Context;

import com.sunteam.ebook.R;

/**
 * 得到单词解释的工具类。
 * 
 * @author wzp
 */
public class WordExplainUtils 
{
	private static final int ST_INDEX_OFF = 4;			//索引项占用字节数
	private static final int ST_INDEX_ITEM_BYTES = 4;	//索引项占用字节数
	
	private byte[] mEnExplain = null;	//英文解释
	private byte[] mCnExplain = null;	//中文解释
	
	public void init( Context context )
	{
		try
		{
			InputStream is = context.getResources().openRawResource(R.raw.word_explain_eng);  
	        mEnExplain = new byte[is.available()];
			is.read(mEnExplain);  
	        is.close();		//把英文文解释读入内存
	        
	        is = context.getResources().openRawResource(R.raw.word_explain_chi);  
			mCnExplain = new byte[is.available()];
			is.read(mCnExplain);  
	        is.close();		//把中文解释读入内存
	        
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/*=====================================================================================================================
	 * Function:	获取中英文单词对应的例词。
	 * Input:	type 文件类型，0 表示简体中文；1 表示英文
	 *		word 单词编码，中文表示一个GB18030编码，英文则是一个ASCII编码
	 * Output:	pData 例词数据，例词之间以空格分隔，以 0 作结束符；
	 *		该缓冲区大小可定义为 ST_EXPLAIN_MAX_LEN，否则可能会有内存溢出!
	 * Return:	SUNTEAM_RET_OK 正确；SUNTEAM_RET_ERR 错误
	 * Author:	Jerry
	 * Date:	2016-04-21
	 * Note:	数据格式如下:
	 *		单词总数	4字节
	 *		单词 1 偏移	4字节
	 *		...
	 *		单词 n 偏移	4字节
	 *		单词 n+1 偏移	4字节
	 *		单词 1 + 0	( 单词数据: 编码=例词1 例词2 ... 0 )
	 *		...
	 *		单词 n + 0
	 *
	 *		汉字数据格式如下:
	 *		阿=阿谀逢迎 阿根廷 阿昌族 阿拉伯 阿谀奉承 阿姨
	 *		...
	 *		座=专座 讲座 星座 叫座 加座 散座 让座 底座 桥座 满座 座钟 专座 座谈 座儿
	 *
	 *		英文数据格式如下:
	 *		a=apple
	 *		...
	 *		z:zero
	 *
	 *		所有主题词都按其编码进行了从小到大的排序；一个单词的所有例词之间用空格分隔。
	 *=====================================================================================================================*/
	public byte[] getWordExplain( int type, char word )
	{
		int flag;
		char curWord;
		int total, low, high, mid = 0;
		byte[] buffer = null;
		int[] pIndex = null;			// 把索引全部读到内存

		if( ( null == mEnExplain ) || ( null == mCnExplain ) )
		{
			return	null;
		}

		if( 0 == type )
		{
			buffer = mCnExplain;
		}
		else if ( 1 == type )
		{
			buffer = mEnExplain;
		}
		else
		{
			return	null;
		}

		total = PublicUtils.byte2int( buffer, 0 );
		pIndex = new int[total + 1];

		for( int i = 0; i < total + 1; i++ )
		{
			pIndex[i] = PublicUtils.byte2int( buffer, ST_INDEX_OFF+i*ST_INDEX_ITEM_BYTES );
		}

		// 通过索引定位
		low = 0;
		high = total - 1;
	   	flag = 0;
		while( low <= high) 
		{
			mid = (low + high) / 2;
			curWord = PublicUtils.byte2char(buffer, pIndex[mid]);
			if( word < curWord )
			{
				high = mid - 1;
			}
			else if( word > curWord )
			{
				low = mid + 1;
			}
			else
			{
				flag = 1;
				break;
			}
		}

	   	if( 0 == flag ) 
	   	{
			return	null;
	   	}

		// 读取例词数据
		int size = pIndex[mid + 1] - pIndex [mid];
		
		byte[] explain = new byte[size];
		for( int i = pIndex[mid], j = 0; i < pIndex[mid]+size; i++, j++ )
		{
			explain[j] = buffer[i];
		}

		return	explain;
	}
}	
