package com.sunteam.ebook.word;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.hwpf.model.CHPBinTable;
import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.hwpf.model.ComplexFileTable;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;
import org.textmining.text.extraction.FastSavedException;
import org.textmining.text.extraction.PasswordProtectedException;
import org.textmining.text.extraction.sprm.SprmIterator;
import org.textmining.text.extraction.sprm.SprmOperation;

import android.text.TextUtils;

public class WordExtractorEx 
{
	private String foramt( String str )
	{
		if( TextUtils.isEmpty(str) )
		{
			return	"";
		}
		str = str.replaceAll("\r\n", "\n");	// 0x0d0a
		str = str.replaceAll("\r", "\n");	// 0x0d
		str = str.replaceAll("\u0001", "");
		str = str.replaceAll("\u0002", "");
		str = str.replaceAll("\u0003", "");
		str = str.replaceAll("\u0004", "");
		str = str.replaceAll("\u0005", "");
		str = str.replaceAll("\u0006", "");
		str = str.replaceAll("\u0007", " ");
		str = str.replaceAll("\u0008", "");	// \b
		str = str.replaceAll("\u0009", "");
		str = str.replaceAll("\u000b", "");
		str = str.replaceAll("\u000c", "");
		str = str.replaceAll("\u000e", "");
		str = str.replaceAll("\u000f", "");
		str = str.replaceAll("\u0010", "");
		str = str.replaceAll("\u0011", "");
		str = str.replaceAll("\u0012", "");
		str = str.replaceAll("\u0015", "");
		str = str.replaceAll("\u0016", "");
		str = str.replaceAll("\u0017", "");
		str = str.replaceAll("\u0018", "");
		str = str.replaceAll("\u0019", "");
		str = str.replaceAll("\u001a", "");
		str = str.replaceAll("\u001b", "");
		str = str.replaceAll("\u001c", "");
		str = str.replaceAll("\u001d", "");
		str = str.replaceAll("\u001e", "");
		str = str.replaceAll("\u001f", "");
		
		return	str;
	}
	
	private String format2( String str )
	{
		if( TextUtils.isEmpty(str) )
		{
			return	"";
		}
		
		int position = str.indexOf("\u0013");
		if( -1 == position )
		{
			return	str;
		}
		
		String tempStr = str.substring(0, position);
		position = str.indexOf("\u0014", position+1);
		if( -1 == position )
		{
			return	str;
		}
		
		return	(tempStr+format2(str.substring(position+1)));
	}
	
	public boolean extractText( InputStream paramInputStream, OutputStreamWriter out ) throws Exception
	{
	    POIFSFileSystem localPOIFSFileSystem = new POIFSFileSystem(paramInputStream);

	    DocumentEntry localDocumentEntry1 = (DocumentEntry)localPOIFSFileSystem.getRoot().getEntry("WordDocument");

	    DocumentInputStream localDocumentInputStream = localPOIFSFileSystem.createDocumentInputStream("WordDocument");
	    byte[] arrayOfByte1 = new byte[localDocumentEntry1.getSize()];

	    localDocumentInputStream.read(arrayOfByte1);
	    localDocumentInputStream.close();

	    int i = LittleEndian.getShort(arrayOfByte1, 10);
	    if ((i & 0x4) != 0)
	    {
	    	throw new FastSavedException("Fast-saved files are unsupported at this time");
	    }
	    if ((i & 0x100) != 0)
	    {
	    	throw new PasswordProtectedException("This document is password protected");
	    }

	    int j = LittleEndian.getShort(arrayOfByte1, 2);
	    switch (j)
	    {
	    	case 101:
		    case 102:
		    case 103:
		    case 104:
		    	Word6ExtractorEx localWord6Extractor = new Word6ExtractorEx();
		    	return localWord6Extractor.extractText(out, arrayOfByte1);
	    }

	    int k = (i & 0x200) != 0 ? 1 : 0;

	    int m = LittleEndian.getInt(arrayOfByte1, 418);

	    String str1 = null;
	    if (k != 0)
	    {
	      str1 = "1Table";
	    }
	    else
	    {
	      str1 = "0Table";
	    }

	    DocumentEntry localDocumentEntry2 = (DocumentEntry)localPOIFSFileSystem.getRoot().getEntry(str1);
	    byte[] arrayOfByte2 = new byte[localDocumentEntry2.getSize()];

	    localDocumentInputStream = localPOIFSFileSystem.createDocumentInputStream(str1);

	    localDocumentInputStream.read(arrayOfByte2);
	    localDocumentInputStream.close();

	    int n = LittleEndian.getInt(arrayOfByte1, 250);
	    int i1 = LittleEndian.getInt(arrayOfByte1, 254);
	    int i2 = LittleEndian.getInt(arrayOfByte1, 24);
	    CHPBinTable localCHPBinTable = new CHPBinTable(arrayOfByte1, arrayOfByte2, n, i1, i2);

	    ComplexFileTable localComplexFileTable = new ComplexFileTable(arrayOfByte1, arrayOfByte2, m, i2);
	    TextPieceTable localTextPieceTable = localComplexFileTable.getTextPieceTable();
	    List localList1 = localTextPieceTable.getTextPieces();

	    localDocumentInputStream = null;
	    localPOIFSFileSystem = null;
	    localDocumentEntry2 = null;
	    localDocumentEntry1 = null;

	    List localList2 = localCHPBinTable.getTextRuns();
	    Iterator localIterator1 = localList2.iterator();
	    Iterator localIterator2 = localList1.iterator();

	    TextPiece localTextPiece = (TextPiece)localIterator2.next();
	    int i3 = localTextPiece.getStart();
	    int i4 = localTextPiece.getEnd();
	    
	    String tempStr = "";

	    while (localIterator1.hasNext())
	    {
	    	CHPX localCHPX = (CHPX)localIterator1.next();
	    	boolean bool = isDeleted(localCHPX.getGrpprl());
	    	if(!bool)
	    	{
	    		int i5 = localCHPX.getStart();
	    		int i6 = localCHPX.getEnd();

	    		while (i5 >= i4)
	    		{
	    			localTextPiece = (TextPiece)localIterator2.next();
	    			i3 = localTextPiece.getStart();
	    			i4 = localTextPiece.getEnd();
	    		}
	    		String str2;
	    		if (i6 < i4)
	    		{
	    			str2 = localTextPiece.substring(i5 - i3, i6 - i3);
	    			tempStr += foramt(str2);
	    			if( !TextUtils.isEmpty(tempStr) && "\n".equals(tempStr.substring(tempStr.length()-1)) )
	    			{
	    				tempStr = format2(tempStr);
	    				out.write(tempStr);
	    				tempStr = "";
	    			}
	    		}
	    		else if (i6 > i4)
	    		{
	    			while (i6 > i4)
	    			{
	    				str2 = localTextPiece.substring(i5 - i3, i4 - i3);
	    				tempStr += foramt(str2);
	    				if( !TextUtils.isEmpty(tempStr) && "\n".equals(tempStr.substring(tempStr.length()-1)) )
		    			{
		    				tempStr = format2(tempStr);
		    				out.write(tempStr);
		    				tempStr = "";
		    			}
	    				if (localIterator2.hasNext())
	    				{
	    					localTextPiece = (TextPiece)localIterator2.next();
	    					i3 = localTextPiece.getStart();
	    					i5 = i3;
	    					i4 = localTextPiece.getEnd();
	    				}
	    				else
	    				{
	    					return true;
	    				}
	    			}
	    			str2 = localTextPiece.substring(0, i6 - i3);
	    			tempStr += foramt(str2);
	    			if( !TextUtils.isEmpty(tempStr) && "\n".equals(tempStr.substring(tempStr.length()-1)) )
	    			{
	    				tempStr = format2(tempStr);
	    				out.write(tempStr);
	    				tempStr = "";
	    			}
	    		}
	    		else
	    		{
	    			str2 = localTextPiece.substring(i5 - i3, i6 - i3);
	    			if (localIterator2.hasNext())
	    			{
	    				localTextPiece = (TextPiece)localIterator2.next();
	    				i3 = localTextPiece.getStart();
	    				i4 = localTextPiece.getEnd();
	    			}
	    			tempStr += foramt(str2);
	    			if( !TextUtils.isEmpty(tempStr) && "\n".equals(tempStr.substring(tempStr.length()-1)) )
	    			{
	    				tempStr = format2(tempStr);
	    				out.write(tempStr);
	    				tempStr = "";
	    			}
	    		}
	    	}
	    }
	    
	    return true;
	}

	private boolean isDeleted( byte[] paramArrayOfByte )
	{
		SprmIterator localSprmIterator = new SprmIterator(paramArrayOfByte);
		while (localSprmIterator.hasNext())
		{
			SprmOperation localSprmOperation = localSprmIterator.next();

			if( ( localSprmOperation.getOperation() == 0 ) && ( localSprmOperation.getOperand() != 0 ) )
			{
				return true;
			}
		}
		
		return false;
	}
}
