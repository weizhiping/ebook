package com.sunteam.ebook.word;

import java.io.InputStream;
import java.util.ArrayList;
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
import org.textmining.text.extraction.WordTextBuffer;
import org.textmining.text.extraction.sprm.SprmIterator;
import org.textmining.text.extraction.sprm.SprmOperation;

public class WordExtractorEx 
{
	public String extractText( InputStream paramInputStream ) throws Exception
	{
		ArrayList localArrayList = new ArrayList();
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
		    	return localWord6Extractor.extractText(arrayOfByte1);
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

	    WordTextBuffer localWordTextBuffer = new WordTextBuffer();

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
	    			localWordTextBuffer.append(str2);
	    		}
	    		else if (i6 > i4)
	    		{
	    			while (i6 > i4)
	    			{
	    				str2 = localTextPiece.substring(i5 - i3, i4 - i3);

	    				localWordTextBuffer.append(str2);
	    				if (localIterator2.hasNext())
	    				{
	    					localTextPiece = (TextPiece)localIterator2.next();
	    					i3 = localTextPiece.getStart();
	    					i5 = i3;
	    					i4 = localTextPiece.getEnd();
	    				}
	    				else
	    				{
	    					return localWordTextBuffer.toString();
	    				}
	    			}
	    			str2 = localTextPiece.substring(0, i6 - i3);
	    			localWordTextBuffer.append(str2);
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
	    			localWordTextBuffer.append(str2);
	    		}
	    	}
	    }
	    
	    return localWordTextBuffer.toString();
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
