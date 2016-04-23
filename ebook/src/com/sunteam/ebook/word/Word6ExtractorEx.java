package com.sunteam.ebook.word;

import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.util.LittleEndian;
import org.textmining.text.extraction.chp.Word6CHPBinTable;

public class Word6ExtractorEx 
{
	public boolean extractText( OutputStreamWriter out, byte[] paramArrayOfByte ) throws Exception
	{
		int i = LittleEndian.getInt(paramArrayOfByte, 24);
		int j = LittleEndian.getInt(paramArrayOfByte, 28);

		int k = LittleEndian.getInt(paramArrayOfByte, 184);
		int m = LittleEndian.getInt(paramArrayOfByte, 188);

		Word6CHPBinTable localWord6CHPBinTable = new Word6CHPBinTable( paramArrayOfByte, k, m, i );

		List localList = localWord6CHPBinTable.getTextRuns();

		Iterator localIterator = localList.iterator();
		while( localIterator.hasNext() )
		{
			CHPX localCHPX = (CHPX)localIterator.next();
			int n = localCHPX.getStart() + i;
			int i1 = localCHPX.getEnd() + i;

			if( !isDeleted(localCHPX.getGrpprl()) )
			{
				String str = new String(paramArrayOfByte, n, Math.min(i1, j) - n, "Cp1252");
				out.write(str+"\r\n");
				if( i1 >= j )
				{
					break;
				}
			}
		}

		return true;
	}

	private boolean isDeleted( byte[] paramArrayOfByte )
	{
		int i = 0;
		boolean bool = false;
		while( i < paramArrayOfByte.length )
		{
			switch( LittleEndian.getUnsignedByte( paramArrayOfByte, i++ ) )
			{
				case 65:
					bool = paramArrayOfByte[(i++)] != 0;
					break;
				case 66:
			        i++;
			        break;
				case 67:
			        i++;
			        break;
				case 68:
			        i += paramArrayOfByte[i];
			        break;
				case 69:
			        i += 2;
			        break;
				case 70:
			        i += 4;
			        break;
				case 71:
			        i++;
			        break;
				case 72:
			        i += 2;
			        break;
				case 73:
			        i += 3;
			        break;
				case 74:
			        i += paramArrayOfByte[i];
			        break;
				case 75:
			        i++;
			        break;
				case 80:
			        i += 2;
			        break;
				case 81:
			        i += paramArrayOfByte[i];
			        break;
				case 82:
			        i += paramArrayOfByte[i];
			        break;
				case 83:
			        break;
				case 85:
					i++;
					break;
				case 86:
					i++;
					break;
				case 87:
					i++;
					break;
				case 88:
					i++;
					break;
				case 89:
					i++;
					break;
				case 90:
					i++;
					break;
				case 91:
					i++;
					break;
				case 92:
					i++;
					break;
				case 93:
					i += 2;
					break;
				case 94:
					i++;
					break;
				case 95:
					i += 3;
					break;
				case 96:
					i += 2;
					break;
				case 97:
					i += 2;
					break;
				case 98:
					i++;
					break;
				case 99:
					i++;
					break;
				case 100:
					i++;
					break;
				case 101:
					i++;
					break;
				case 102:
					i++;
					break;
				case 103:
					i += paramArrayOfByte[i];
					break;
				case 104:
					i++;
					break;
				case 105:
					i += paramArrayOfByte[i];
					break;
				case 106:
					i += paramArrayOfByte[i];
					break;
				case 107:
					i += 2;
					break;
				case 108:
					i += paramArrayOfByte[i];
					break;
				case 109:
					i += 2;
					break;
				case 110:
					i += 2;
					break;
				case 117:
					i++;
					break;
				case 118:
					i++;
				case 76:
				case 77:
				case 78:
				case 79:
				case 84:
				case 111:
				case 112:
				case 113:
				case 114:
				case 115:
				case 116: 
			}  
		} 
		
		return bool;
	}
}
