package com.sinergise.java.geometry.io.shp;


/**
 * @author dragan
 */

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.util.io.BinaryOutput.BinaryRandomAccessOutput;
import com.sinergise.java.util.io.BinaryRandomAccessIO;
import com.sinergise.java.util.io.RandomAccessEndianFile;

public class DBFFile implements Closeable {
	private static final String DBF_DATE_FORMAT = "yyyyMMdd";
	protected final SimpleDateFormat dbfDateFormater = new SimpleDateFormat(DBF_DATE_FORMAT);


	BinaryRandomAccessIO file;

	String sFileName; // sFileName
	int nRecordCount; // No. of records
	int nCurrentRecord; // Current read record
	int nFieldCount; // No. of fields
	ArrayList<DBFField> fieldArray = new ArrayList<DBFField>(); // Array with fields
	byte[] recordBuf; // buffer for reading records
	boolean bReadOnly;
	boolean bWriteFlag;
	int codepage;
	int offset;
	short size;

	/**
	 * Constructor for DBFFile.
	 */
	public DBFFile() {
		super();
		recordBuf = null;
		file = null;
		nRecordCount = 0;
		nCurrentRecord = -1;
		nFieldCount = 0;
		bWriteFlag = false;
	}

	public boolean open(File f, boolean readOnly) throws FileNotFoundException {
		return open(RandomAccessEndianFile.create(f, readOnly));
	}
	
	public boolean open(BinaryRandomAccessIO fileToOpen) {
		this.file = fileToOpen;

		int nBufSize = initFields();

		recordBuf = new byte[size + 1];
		recordBuf[0] = ' '; // not deleted
		if (nRecordCount == 0) return true;
		return seek(0);
	}

	@Override
	public void close() {
		if (file != null) {
			flush();
			fieldArray.clear();
			try {
				file.close();
			} catch(IOException e) {}
		}
		file = null;
	}

	public boolean isOpen() {
		return file != null;
	}

	public boolean seek(int nRecord) {
		if (nRecord < 0 || nRecord >= nRecordCount) throw new IllegalArgumentException("Invalid nRecord");

		if (nRecord == nCurrentRecord) return true;

		// new record
		if (bWriteFlag) flush();

		nCurrentRecord = nRecord;
		long pos = (nRecord * size) + offset; // DBF starts with record number 1 but we count as it is starts from 0

		try {
			file.seek(pos);
			file.readFully(recordBuf, 0, size);
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}

		recordBuf[size] = 0;

		return true;
	}

	public void flush() {
		if (!bWriteFlag || bReadOnly || nCurrentRecord == -1) return;

		bWriteFlag = false;

		long pos = (nCurrentRecord * size) + offset;

		try {
			file.seek(pos);
			file.write(recordBuf, 0, size);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	public DBFField getField(int n) {
		if (n < 0 || n >= nFieldCount) throw new IllegalArgumentException("Invalid field number");

		return fieldArray.get(n);
	}

	public boolean isDeleted() {
		return recordBuf[0] != ' ';
	}

	public boolean doCPConversion = false;

	public static final byte CP_CP1250 = (byte)0xC8; // Windows Eastern 
	public static final byte CP_CP852 = (byte)0x64;
	public static final byte CP_CP437 = (byte)0x01; // US ms-dos
	public static final byte CP_CP1252 = (byte)0x03; // Windows ANSI

	private static final Charset CS_WINDOWS1250 = Charset.forName("windows-1250");
	private static final Charset CS_852 = Charset.forName("IBM852");

	private static Charset getCharset(int codepage) {
		if (codepage == CP_CP1250) return CS_WINDOWS1250;
		else if (codepage == CP_CP852) return CS_852;
		return CS_WINDOWS1250;
	}

	public String getValue(int n) throws UnsupportedEncodingException {
		if (n < 0 || n >= nFieldCount) throw new IllegalArgumentException("Invalid field number");

		DBFField fld = getField(n);

		byte[] data = new byte[fld.len];
		System.arraycopy(recordBuf, fld.offset, data, 0, fld.len);

		String retVal;
		if (fld.type == 'C') {
			retVal = new String(data, getCharset(codepage).name());
		} else {
			retVal = new String(data);
		}
		return retVal.trim();
	}
	
	public Date getValueAsDate(int n) throws UnsupportedEncodingException, ParseException {
		return parseDbfDate(getValue(n));
	}


	public static int indexOf(int[] array, int element) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == element) return i;
		}
		return -1;
	}


	public boolean setValue(int n, Boolean value) throws UnsupportedEncodingException {
		if (value == null) return false;
		if (value.equals(Boolean.TRUE)) {
			return setValue(n, "T");
		}
		return setValue(n, "F");
	}

	public boolean setValue(int n, Date date) throws UnsupportedEncodingException {
		if (date == null) return false;
		return setValue(n, dbfDateFormater.format(date));
	}

	public boolean setValue(int n, String s) throws UnsupportedEncodingException {
		if (n < 0 || n >= nFieldCount) throw new IllegalArgumentException("Invalid field index");

		if (s == null) return true;

		if (bReadOnly) return false;

		if (!(n >= 0 && n <= nFieldCount)) return false;

		DBFField fld = getField(n);
		if (fld.type == 'C') {
			byte[] stringBytes;
			stringBytes = s.getBytes(getCharset(codepage).name());
			int len = Math.min(stringBytes.length, fld.len);
			System.arraycopy(stringBytes, 0, recordBuf, fld.offset, len);
		} else {
			int len = s.length();
			if (len > fld.len) len = fld.len;
			for (int i = 0; i < len; i++) {
				recordBuf[fld.offset + i] = (byte)s.charAt(i);
			}
		}

		bWriteFlag = true;

		return true;
	}

	public int initFields() {
		int off = 0;
		try {
			file.seek(4);
			nRecordCount = file.readIntLE();

			offset = file.readShortLE();
			size = file.readShortLE();
			byte[] data = new byte[20];
			file.readFully(data, 0, 20);
			codepage = toInt(data[17]);


			nFieldCount = (offset - 33) / 32;

			off = 1; // field offset

			byte[] fieldName = new byte[10];
			int pos = 32;
			for (int i = 0; i < nFieldCount; i++, pos += 32) {
				DBFField fld = new DBFField();
				fieldArray.add(fld);
				file.seek(pos);
				file.readFully(fieldName, 0, 10);
				// trim zeros
				int strLen = 0;
				for (; strLen < 10; strLen++) {
					if (fieldName[strLen] == 0) break;
				}
				fld.name = new String(fieldName, 0, strLen);

				file.seek(pos + 11);
				byte buf[] = new byte[7];
				file.readFully(buf, 0, 7);
				fld.type = (char)buf[0];
				fld.len = toInt(buf[5]);
				fld.decimals = (short)toInt(buf[6]);
				fld.offset = off;
				off += fld.len;

				//int l = fld.name.length();
			}
		} catch(IOException e) {}

		return off;
	}

	public boolean markRowDeleted(int nRecord) {
		if (nRecord < 0 || nRecord >= nRecordCount) throw new IllegalArgumentException("Invalid record number");

		if (!seek(nRecord)) return false;

		recordBuf[0] = 'D';
		bWriteFlag = true;
		flush();
		return true;
	}

	public boolean appendBlank() {
		byte[] pBuffer = new byte[size];
		for (int i = 0; i < size; i++) {
			pBuffer[i] = ' ';
		}

		long pos = ((long)nRecordCount * size) + offset;

		try {
			file.seek(pos);
			file.write(pBuffer, 0, size);
		} catch(IOException e) {
			e.printStackTrace();
		}
		nRecordCount++;
		updateHeader();

		return true;
	}

	public void updateHeader() {
		byte[] date = new byte[3];

		getDate(date);
		try {
			file.seek(1); // skip version byte from begining of file
			file.write(date, 0, 3);
			file.writeIntLE(nRecordCount);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	static protected void getDate(byte[] yymmdd) {
		Calendar cal = Calendar.getInstance();
		//tm_ptr =  localtime( (time_t *) &time_val ) ;
		yymmdd[0] = (byte)cal.get(Calendar.YEAR);
		yymmdd[1] = (byte)cal.get(Calendar.MONTH);
		yymmdd[2] = (byte)cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @return
	 */
	public int getColumnCount() {
		return nFieldCount;
	}

	/**
	 * @return
	 */
	public int getRecordCount() {
		return nRecordCount;
	}

	public byte[] getRecord() {
		return recordBuf;
	}

	protected void appendRecord(byte[] pBuffer) throws IOException {
		long pos = ((long)nRecordCount * size) + offset;

		file.seek(pos);
		file.write(pBuffer, 0, size);
		nRecordCount++;
	}

	public void appendCurrentRecord() throws IOException {
		appendRecord(getRecord());
	}

	public void prepareEmptyRecord() {
		for (int i = 0; i < recordBuf.length; i++) {
			recordBuf[i] = ' ';
		}
	}

	static public boolean create(BinaryRandomAccessOutput target, DBFField[] info, byte codepage) {
		try {
			int n_flds;
			int i;
			short calc_record_len;


			calc_record_len = 1;

			int nCount = info.length;

			for (n_flds = 0; n_flds < nCount; n_flds++) {
				DBFField fi = info[n_flds];
				if (fi.len > 255) fi.len = (byte)255;

				switch (info[n_flds].type) {
					case 'N':
					case 'F':
					case 'C':
						calc_record_len += info[n_flds].len;
						break;
					case 'M':
					case 'G':
					case 'B':
						calc_record_len += 10;
						break;
					case 'D':
						calc_record_len += 8;
						break;
					case 'L':
						calc_record_len += 1;
						break;
					default:
						break;
				}
			}

			byte[] date = new byte[3];
			getDate(date);

			// WRITE DBF header
			// version 1 byte
			target.write(new byte[]{0x03}, 0, 1);
			// date 3 bytes
			target.write(date, 0, 3);

			//num_recs 1 int
			target.write(new byte[4], 0, 4);

			//header_len 1 short
			target.writeShortLE((short)(32 * (n_flds + 1) + 1));

			//record_len 1 short
			target.writeShortLE(calc_record_len);
			byte[] buf = new byte[20];
			buf[17] = toByte(codepage);
			target.write(buf, 0, 20);

			for (i = 0; i < n_flds; i++) {
				DBFField fi = info[i];
				if (fi.len > 255) fi.len = 255;
				switch (fi.type) {
					case 'C':
						fi.len = (info[i].len & 0xFF);
						fi.decimals = (short)(info[i].len >> 8);
						break;
					case 'M':
					case 'G':
					case 'B':
						fi.len = 10;
						fi.decimals = 0;
						break;
					case 'D':
						fi.len = 8;
						fi.decimals = 0;
						break;
					case 'L':
						fi.len = 1;
						fi.decimals = 0;
						break;
					case 'N':
					case 'F':
						fi.len = (info[i].len & 0xFF);
						fi.decimals = info[i].decimals;
						break;
					default:
						break;
				}
				byte[] origName = fi.name.getBytes(getCharset(codepage).name());
				int nameLen = Math.min(origName.length, 10);
				byte[] name = new byte[11];
				System.arraycopy(origName, 0, name, 0, nameLen);
				target.write(name, 0, 11);
				byte data[] = new byte[1 + 4 + 2 + 14];
				data[0] = (byte)fi.type;
				data[5] = toByte(fi.len);
				data[6] = toByte(fi.decimals);
				target.write(data, 0, data.length);
			}

			target.write(new byte[]{13}, 0, 1);
			return true;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	//TODO: check if this is really necessary; seems that (byte)234 does the same as toByte(234)
	private static byte toByte(int val) {
		val = val & 0xFF;
		if (val > Byte.MAX_VALUE) { return (byte)(-256 + val); }
		return (byte)val;
	}

	public static int toInt(byte b) {
		if (b < 0) { return (b - Byte.MIN_VALUE) + 1 + Byte.MAX_VALUE; }
		return b;
	}
	
	public static Date parseDbfDate(String dbfDateVal) throws ParseException {
		if (StringUtil.isNullOrEmpty(dbfDateVal)) {
			return null;
		}
		return new SimpleDateFormat(DBF_DATE_FORMAT).parse(dbfDateVal);
	}
}