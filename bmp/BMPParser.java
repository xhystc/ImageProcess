package com.xhystc.bmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BMPParser
{
	char bfType;
	int bfSize;
	int reserved;
	int bfOfBits;
	
	int biSize;
	int biWidth;
	int biHeight;
	char biPlanes;
	char biBitCount;
	int biCompression;
	int biSizeImage;
	int biXPelsPerMeter;
	int biYPelsPerMeter;
	int biClrUsed;
	int biClrImportant;
	byte[] data;
	byte[] clr;
	
	public void setBiWidth(int value)
	{
		biWidth = value;

	}
	public void setBiHeight(int value)
	{
		biHeight = value;

	}
	public void setData(byte[] value)
	{
		if(value==null)
			biSizeImage=0;
		else
			biSizeImage = value.length;
		data = value;
	}
	public void setClr(byte[] value)
	{
		clr=value;
	}
	
	public int getBiWidth()
	{
		return biWidth;
	}

	public int getBiHeight()
	{
		return biHeight;
	}
	
	public byte[] getData()
	{
		return data;
	}
	public byte[] getClr()
	{
		return clr;
	}
	public int getBiBitCount()
	{
		return biBitCount;
	}
	public int getBiSizeImage()
	{
		return biSizeImage;
	}

	public BMPParser()
	{

	}
	
	public BMPParser(InputStream input) throws Exception
	{
		bfType = readWORD(input);
		
		byte[] type = CharToWORD(bfType);
		if(!new String(type).equals("BM"))
		{
			throw new Exception("not bmp");
		}
		
		
		bfSize = readDWORD(input);
		reserved = readDWORD(input);
		bfOfBits = readDWORD(input);
		
		biSize = readDWORD(input);
		biWidth = readDWORD(input);
		biHeight = readDWORD(input);
		biPlanes = readWORD(input);
		biBitCount = readWORD(input);
		biCompression = readDWORD(input);
		biSizeImage = readDWORD(input);
		biXPelsPerMeter = readDWORD(input);
		biYPelsPerMeter = readDWORD(input);
		biClrUsed = readDWORD(input);
		biClrImportant = readDWORD(input);
		
		
		if(biBitCount == 8 )
		{
			clr = new byte[256*4];
			if(input.read(clr) < 256*4) throw new IOException("data size error");
			data = new byte[biSizeImage];
			if(input.read(data) < biSizeImage) throw new IOException("data size error");
		}
		else if(biBitCount == 24)
		{
			data = new byte[biSizeImage];
			if(input.read(data) < biSizeImage) throw new IOException("data size error");
		}
		else
		{
			throw new IOException("biClrUsed error");
		}
	}

	
	static int readDWORD(InputStream in) throws IOException
	{
		byte[] buffer = new byte[4];
		in.read(buffer,0,4);
		
		int res = DWORDToInt(buffer);
		return res;
	}
	
	static char readWORD(InputStream in) throws IOException
	{
		byte[] buffer = new byte[2];
		in.read(buffer,0,2);
		
		char res =  WORDToChar(buffer);
		return res;
	}
	
	static void writeDWORD(OutputStream out,int value) throws IOException
	{
		out.write(IntToDWORD(value));
	}
	
	static void writeWORD(OutputStream out,char value) throws IOException
	{
		out.write(CharToWORD(value));
	}
	
	static int DWORDToInt(byte[] buffer)
	{
		int res = (buffer[3]&0xff)<<24 | (buffer[2]&0xff)<<16 | (buffer[1]&0xff)<<8 | (buffer[0]&0xff);
		return res;
	}
	
	static char WORDToChar(byte[] buffer)
	{
		char res =  (char) ((buffer[1]&0xff)<<8 | (buffer[0]&0xff));
		return res;
	}
	
	static byte[] CharToWORD(char value)
	{
		byte[] buffer = new byte[2];
		
		buffer[0] = (byte) (value & 0x000000ff);
		buffer[1] = (byte) ((value & 0x0000ff00)>>8);
		return buffer;
	}
	
	static byte[] IntToDWORD(int value)
	{
		byte[] buffer = new byte[4];
		
		buffer[0] = (byte) (value & 0x000000ff);
		buffer[1] = (byte) ((value & 0x0000ff00)>>8);
		buffer[2] = (byte) ((value & 0x00ff0000)>>16);
		buffer[3] = (byte) ((value & 0xff000000)>>24);
		return buffer;
	}
	
	public void save(OutputStream out) throws IOException
	{
		
		writeWORD(out,bfType);
		writeDWORD(out,bfSize);
		writeDWORD(out,reserved);
		writeDWORD(out,bfOfBits);
		
		writeDWORD(out,biSize);
		writeDWORD(out,biWidth);
		writeDWORD(out,biHeight);
		writeWORD(out,biPlanes);
		writeWORD(out,biBitCount);
		writeDWORD(out,biCompression);
		writeDWORD(out,biSizeImage);
		writeDWORD(out,biXPelsPerMeter);
		writeDWORD(out,biYPelsPerMeter);
		writeDWORD(out,biClrUsed);
		writeDWORD(out,biClrImportant);
		
		if(clr!=null)
			out.write(clr);
		out.write(data);
		out.flush();
	}
	
	public byte[] get(int i,int j)
	{
		byte[] ret;
		if(i<0 || i>biHeight-1 || j<0 || j>biWidth-1) return null;
		if(biBitCount==24)
		{
			ret = new byte[3];
			ret[0]=data[(i*biWidth+j)*3];
			ret[1]=data[(i*biWidth+j)*3+1];
			ret[2]=data[(i*biWidth+j)*3+2];
		}
		else
		{
			ret = new byte[1];
			ret[0]=data[i*biWidth+j];
		}
		return ret;
	}
	public void set(int i,int j,byte value) throws Exception
	{

		if(i<0 || i>biHeight-1 || j<0 || j>biWidth-1) return;
		if(biBitCount==24)
		{
			data[(i*biWidth+j)*3]=value;
			data[(i*biWidth+j)*3+1]=value;
			data[(i*biWidth+j)*3+2]=value;
		}
		else
		{
			throw new Exception("???");
		}

	}
	public BMPParser newBmp()
	{
		BMPParser bmp = new BMPParser();
		bmp.bfSize = this.bfSize;
		bmp.reserved = this.reserved;
		bmp.bfOfBits = this.bfOfBits;
		bmp.bfType=this.bfType;
		bmp.biSize = this.biSize;
		bmp.biWidth = 0;
		bmp.biHeight = 0;
		bmp.biPlanes = this.biPlanes;
		bmp.biBitCount = this.biBitCount;
		bmp.biCompression = this.biCompression;
		bmp.biSizeImage = 0;
		bmp.biXPelsPerMeter = this.biXPelsPerMeter;
		bmp.biYPelsPerMeter = this.biYPelsPerMeter;
		bmp.biClrUsed = this.biClrUsed;
		bmp.biClrImportant = this.biClrImportant;
		bmp.setData(null);
		bmp.clr=this.clr;
		return bmp;
	}

	public BMPParser clone()
	{
		BMPParser bmp = this.newBmp();
		bmp.setBiWidth(this.biWidth);
		bmp.setBiHeight(this.biHeight);
		byte[] data = new byte[this.data.length];

		System.arraycopy(this.data,0,data,0,data.length);
		bmp.setData(data);
		return bmp;
	}

}







