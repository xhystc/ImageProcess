package com.xhystc.bmp;

import org.omg.PortableServer.POA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;

/**
 *
 *各种图像算法大集合，其中:
 * newLen=newLen+(4-newLen%4) 是因为bmp每行要求4字节对齐
 * data&0xff 是因为java字节用补码表示
 *
 * */
public class BMP24BitProcessor
{
	public final static int GRAY_TRANSFORM_TYPE_MI = 0;
	public final static int GRAY_TRANSFORM_TYPE_LOG = 1;
	public final static int GRAY_TRANSFORM_TYPE_ZHI = 2;

	/**
	 * 一般灰度化
	 * */
	public static void gray(BMPParser bmp)
	{
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		int len = (3*width+3)/4*4;
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int ptr = i*len+j*3;
				int r = data[ptr] & 0xff;
				int g = data[ptr+1] & 0xff;
				int b = data[ptr+2] & 0xff;
				int gray = (r+g+b)/3;
				data[ptr]=data[ptr+1]=data[ptr+2]=(byte) gray;
			}
		}
	}
	/**
	 * 线性灰度化
	 * */
	static public void grayTransformLinear(BMPParser bmp,double a,double b)
	{
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int ptr = (i*width+j)*3;
				double gray = data[ptr] & 0xff;
				gray*=a;
				gray+=b;
				if(gray>255) gray =255;
				else if(gray<0) gray = 0;
				data[ptr]=data[ptr+1]=data[ptr+2]=(byte) ((int)gray);

			}
		}
	}
	/**
	 * 非线性灰度化
	 * */
	static public void grayTransformNonLinear(BMPParser bmp,int type,double a)
	{

		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int ptr = (i*width+j)*3;
				double gray = data[ptr] & 0xff;
				switch(type)
				{
				case GRAY_TRANSFORM_TYPE_MI:
					gray = (int) Math.pow(gray, a);
					break;
				case GRAY_TRANSFORM_TYPE_LOG:
					gray = (int) Math.log(gray);
					break;
				case GRAY_TRANSFORM_TYPE_ZHI:
					gray = (int) Math.pow(a, gray);
					break;
				default:
					throw new IllegalArgumentException("unsupport type");
					
				}
				if(gray>255) gray =255;
				else if(gray<0) gray = 0;
				data[ptr]=data[ptr+1]=data[ptr+2]=(byte) ((int)gray);
			}
		}
		
	}

	/**
	 * 固定阈值灰度化
	 * */
	static public void grayTransformThreshold(BMPParser bmp,int threshold)
	{
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		int len = (3*width+3)/4*4;

		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int ptr = i*len+j*3;
				int gray = data[ptr] & 0xff;
				if(gray < threshold)
				{
					gray = 0;
				}
				else
					gray = 255;
				data[ptr]=data[ptr+1]=data[ptr+2]=(byte) gray;
			}
		}
	}
	/**
	 * 双固定阈值灰度化
	 * */
	static public void grayTransformThreshold(BMPParser bmp,int thresholda,int thresholdb,boolean type)
	{
		if(thresholda > thresholdb)
			throw new IllegalArgumentException("thresholda > thresholdb");
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int ptr = (i*width+j)*3;
				int gray = data[ptr] & 0xff;
				if(type)
				{
					if(gray < thresholda)
					{
						gray = 0;
					}
					else if(gray > thresholda && gray < thresholdb)
						gray = 255;
					else
						gray = 0;
				}
				else
				{
					if(gray < thresholda)
					{
						gray = 255;
					}
					else if(gray > thresholda && gray < thresholdb)
						gray = 0;
					else
						gray = 255;
				}
				data[ptr]=data[ptr+1]=data[ptr+2]=(byte) gray;

			}
		}
	}

	/**
	 * 图像缩放
	 * */
	static public void zoom(BMPParser bmp,double k)
	{
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		int newWidth = (int) (width*k+0.5);
		int newHeight = (int) (height*k+0.5);

		int newLen = newWidth*3;
		int len = (width *3+3)/4*4;


		if(newLen%4!=0)
		{
			newLen=newLen+(4-newLen%4);
		}


		byte[] newData = new byte[newHeight*newLen];
		if(newWidth<1 || newHeight<1)
		{
			bmp.setBiHeight(newHeight);
			bmp.setBiWidth(newWidth);
			bmp.setData(newData);
			return;
		}
		for(int i=0;i<newData.length;i++)
			newData[i]=(byte) 255;
		
		for(int j=0;j<height;j++)
		{
			for(int i=0;i<width;i++)
			{
				int tempx = (int) (k*i);
				int tempy = (int) (j*k);
				
				for(int x=0;x<k;x++)
				{
					for(int y=0;y<k;y++)
					{
						newData[(tempx+x)*3+(tempy+y)*newLen] = data[j*len+i*3];
						newData[(tempx+x)*3+(tempy+y)*newLen+1] = data[j*len+i*3+1];
						newData[(tempx+x)*3+(tempy+y)*newLen+2] = data[j*len+i*3+2];
					}
				}
				newData[(tempx)*3+(tempy)*newLen] = data[j*len+i*3];
				newData[(tempx)*3+(tempy)*newLen+1] = data[j*len+i*3+1];
				newData[(tempx)*3+(tempy)*newLen+2] = data[j*len+i*3+2];
				
			}
			
		}
		bmp.setBiHeight(newHeight);
		bmp.setBiWidth(newWidth);
		bmp.setData(newData);
	}

	/**
	 * 图像旋转
	 * */
	static public void rotate(BMPParser bmp,double jiaodu)
	{
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		byte[] data = bmp.getData();
		

		int zhongxin_x = (width-1)/2;
		int zhongxin_y = (height-1)/2;
		
		int max_shang;
		int max_xia;
		int max_zuo;
		int max_you;
		
		double hudu = jiaodu*Math.PI/180;
		double cos = Math.cos(hudu);
		double sin = Math.sin(hudu);
		
		
		int temp_ax = 0-zhongxin_x;
		int temp_ay = 0-zhongxin_y;
		int temp_bx = width-1-zhongxin_x;
		int temp_by = 0-zhongxin_y;
		int temp_cx = width-1-zhongxin_x;
		int temp_cy = height-1-zhongxin_y;
		int temp_dx = 0-zhongxin_x;
		int temp_dy = height-1-zhongxin_y;
		
		//System.out.println("c_ax:"+c_ax+" c_ay:"+c_ay+" c_bx:"+c_bx+" c_by:"+c_by+" c_cx:"+c_cx+" c_cy:"+c_cy+" c_dx:"+c_dx+" c_dy:"+c_dy);		
		int c_ax = (int) Math.round(temp_ax*cos + temp_ay*sin);
		int c_ay = (int) Math.round(temp_ay*cos - temp_ax*sin);
		int c_bx = (int) Math.round(temp_bx*cos + temp_by*sin);
		int c_by = (int) Math.round(temp_by*cos - temp_bx*sin);
		int c_cx = (int) Math.round(temp_cx*cos + temp_cy*sin);
		int c_cy = (int) Math.round(temp_cy*cos - temp_cx*sin);
		int c_dx = (int) Math.round(temp_dx*cos + temp_dy*sin);
		int c_dy = (int) Math.round(temp_dy*cos - temp_dx*sin);
		
		System.out.println("c_ax:"+c_ax+" c_ay:"+c_ay+" c_bx:"+c_bx+" c_by:"+c_by+" c_cx:"+c_cx+" c_cy:"+c_cy+" c_dx:"+c_dx+" c_dy:"+c_dy);
		
		max_shang = c_ay;
		max_xia = c_ay;
		max_zuo = c_ax;
		max_you = c_ax;
		
		if(c_by < max_shang)
			max_shang=c_by;
		else if(c_by > max_xia)
			max_xia = c_by;
		if(c_cy < max_shang)
			max_shang=c_cy;
		else if(c_cy > max_xia)
			max_xia = c_cy;
		if(c_dy < max_shang)
			max_shang=c_dy;
		else if(c_dy > max_xia)
			max_xia = c_dy;
		
		if(c_bx < max_zuo)
			max_zuo=c_bx;
		else if(c_bx > max_you)
			max_you = c_by;
		if(c_cx < max_zuo)
			max_zuo=c_cx;
		else if(c_cx > max_you)
			max_you = c_cx;
		if(c_dx < max_zuo)
			max_zuo=c_dy;
		else if(c_dx > max_you)
			max_you = c_dx;
		
	//	max_shang--;
		
		int new_width = Math.abs(max_you-max_zuo);
		int new_height = Math.abs(max_xia-max_shang+1);
		
		if(new_width%4!=0)
		{
			new_width=new_width+(4-new_width%4);
		}
		System.out.println("new_width:"+new_width+" new_height:"+new_height);
		System.out.println("max_zuo:"+max_zuo+" max_you:"+max_you+" max_shang:"+max_shang+" max_xia:"+max_xia);
		byte[] new_data = new byte[(new_height)*(new_width)*3];
		

		for(int i=0;i<new_data.length;i++)
		{
			new_data[i]=(byte) 255;
		}
		
		int test_max_x=0;
		int test_max_y=0;
		int k=0;
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{

				int temp_x = j-zhongxin_x;
				int temp_y = i-zhongxin_y;

				int new_x = (int) Math.round(temp_x*cos + temp_y*sin);
				int new_y = (int) Math.round(temp_y*cos - temp_x*sin);

				
				if(new_x < test_max_x)
					test_max_x = new_x;
				if(new_y < test_max_y)
					test_max_y = new_y;
				
				
				new_x -= max_zuo;
				new_y -= max_shang;
				
				new_data[(new_y*new_width+new_x)*3] = data[(i*width+j)*3];
				new_data[(new_y*new_width+new_x)*3+1] = data[(i*width+j)*3+1];
				new_data[(new_y*new_width+new_x)*3+2] =data[(i*width+j)*3+2];
				
				if(i!=0 && j!=0 && i!=height-1 && j!=width-1)
				{
					if((new_data[((new_y-1)*new_width+new_x)*3] & 0xff)==255 && (new_data[((new_y-1)*new_width+new_x)*3+1]&0xff)==255 && (new_data[((new_y-1)*new_width+new_x)*3]&0xff)==255)
					{
						new_data[((new_y-1)*new_width+new_x)*3]=data[(i*width+j)*3];
						new_data[((new_y-1)*new_width+new_x)*3+1]=data[(i*width+j)*3+1];
						new_data[((new_y-1)*new_width+new_x)*3+2]=data[(i*width+j)*3+2];
					}
					if((new_data[((new_y+1)*new_width+new_x)*3]&0xff)==255 && (new_data[((new_y+1)*new_width+new_x)*3+1]&0xff)==255 && (new_data[((new_y+1)*new_width+new_x)*3]&0xff)==255)
					{
						new_data[((new_y+1)*new_width+new_x)*3]=data[(i*width+j)*3];
						new_data[((new_y+1)*new_width+new_x)*3+1]=data[(i*width+j)*3+1];
						new_data[((new_y+1)*new_width+new_x)*3+2]=data[(i*width+j)*3+2];
					}
					if((new_data[(new_y*new_width+new_x-1)*3]&0xff)==255 && (new_data[(new_y*new_width+new_x-1)*3+1]&0xff)==255 && (new_data[(new_y*new_width+new_x-1)*3]&0xff)==255)
					{
						new_data[(new_y*new_width+new_x-1)*3] = data[(i*width+j)*3];
						new_data[(new_y*new_width+new_x-1)*3+1] = data[(i*width+j)*3+1];
						new_data[(new_y*new_width+new_x-1)*3+2] = data[(i*width+j)*3+2];
					}
					if((new_data[(new_y*new_width+new_x+1)*3]&0xff)==255 && (new_data[(new_y*new_width+new_x+1)*3+1]&0xff)==255 && (new_data[(new_y*new_width+new_x+1)*3]&0xff)==255)
					{
						new_data[(new_y*new_width+new_x+1)*3] = data[(i*width+j)*3];
						new_data[(new_y*new_width+new_x+1)*3+1] = data[(i*width+j)*3+1];
						new_data[(new_y*new_width+new_x+1)*3+2] = data[(i*width+j)*3+2];
					}
				}
				
			}
		}
		
		
		System.out.println("test_max_x:"+test_max_x+" test_max_y:"+test_max_y+" k:"+k);

		
		
		bmp.setBiHeight(new_height);
		bmp.setBiWidth(new_width);
		bmp.setData(new_data);
	}

	/**
	 * 图像镜像
	 * */
	public static void mirror(BMPParser bmp,boolean type)
	{
		byte[] data = bmp.getData();
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		
		
		/*水平镜像*/
		if(type)
		{
			int middle = width/2;
			for(int i=0;i<height;i++)
			{
				for(int j=0;j<middle;j++)
				{
					byte byte1 = data[(i*width+j)*3];
					byte byte2 = data[(i*width+j)*3+1];
					byte byte3 = data[(i*width+j)*3+2];
					
					data[(i*width+j)*3] = data[(i*width+width-j-1)*3];
					data[(i*width+j)*3+1] = data[(i*width+width-j-1)*3+1];
					data[(i*width+j)*3+2] = data[(i*width+width-j-1)*3+2];
					
					data[(i*width+width-j-1)*3] = byte1;
					data[(i*width+width-j-1)*3+1] = byte2;
					data[(i*width+width-j-1)*3+2] = byte3;
				}
			}
		}
		/*垂直镜像*/
		else
		{

			int middle = height/2;
			for(int i=0;i<middle;i++)
			{
				for(int j=0;j<width;j++)
				{
					byte byte1 = data[(i*width+j)*3];
					byte byte2 = data[(i*width+j)*3+1];
					byte byte3 = data[(i*width+j)*3+2];
					
					data[(i*width+j)*3] = data[((height-i-1)*width+j)*3];
					data[(i*width+j)*3+1] = data[((height-i-1)*width+j)*3+1];
					data[(i*width+j)*3+2] = data[((height-i-1)*width+j)*3+2];
					
					data[((height-i-1)*width+j)*3] = byte1;
					data[((height-i-1)*width+j)*3+1] = byte2;
					data[((height-i-1)*width+j)*3+2] = byte3;
				}
			}
		}
	}
	/**
	 * 图像转置
	 * */
	public static void transpose(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		
		byte[] newData = new byte[data.length];
		
		if(height%4!=0)
		{
			height=height+(4-height%4);
		}
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				newData[(j*height+i)*3] = data[(i*width+j)*3];
				newData[(j*height+i)*3+1] = data[(i*width+j)*3+1];
				newData[(j*height+i)*3+2] = data[(i*width+j)*3+2];
			}
		}
		
		bmp.setBiHeight(width);
		bmp.setBiWidth(height);
		bmp.setData(newData);
	}

	/**
	 * 统计图像灰度值分布
	 * */
	public static Map<Integer,Integer> grayValue(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		Map<Integer,Integer> ret = new HashMap<>();
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int temp = data[(i*width+j)*3] & 0xff;
				if(ret.containsKey(temp))
				{
					ret.put(temp, ret.get(temp)+1);
				}
				else
					ret.put(temp, 1);
				
			}
		}
		return ret;
	}

	/**
	 * 灰度值均衡化
	 * */
	public static void grayBalance(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		
		Map<Integer,Integer> grayValue = grayValue(bmp);
		double n = width*height;
		Map<Integer,Integer> valueMap = new HashMap<>();
		
		double count = 0.0;
		for(int i=0;i<=255;i++)
		{
			if(grayValue.get(i) != null)
			{
				count += grayValue.get(i)/n;
				valueMap.put(i, new Double(count*255).intValue());
				//System.out.println("gray:"+i+" newGray:"+count);
			}
		}
		
	
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int ptr = (i*width+j)*3;
				int gray = data[ptr] & 0xff;
				
				
				data[ptr]=data[ptr+1]=data[ptr+2]=valueMap.get(gray).byteValue();
				
				
			}
		}
	}

	/**
	 * 依照模版简单滤波
	 * */
	public static void simpleFilter(BMPParser bmp,double[][] temp)
	{
		if(temp.length != temp[0].length) return;
		int n = temp.length;
		if(n % 2 ==0 || n <=0) return;
		
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		int len = (3*width+3)/4*4;

		n-=1;
		n/=2;
		for(int i=n;i<height-n;i++)
		{
			for(int j=n;j<width-n;j++)
			{
				double count = 0;
				int sum =0;
				for(int k=0;k<n*2+1;k++)
				{
					for(int o=0;o<n*2+1;o++)
					{
						count+=temp[k][o];
						sum+=temp[k][o]*(data[(i+k-n)*len+(j+o-n)*3]&0xff);
					}
				}
				sum = (int)(sum/count);
				data[i*len+j*3] = (byte)sum;
				data[i*len+j*3+1] = (byte)sum;
				data[i*len+j*3+2] = (byte)sum;
			}
		}
	}

	/**
	 * 求单点卷积
	 * */
	private static  int juanji(byte[] data,int[][] temp,int i,int j,int width)
    {
        if(temp.length != temp[0].length) return-1;
        int n = temp.length;
        if(n % 2 ==0 || n <=0) return-1;
	    int len = (3*width+3)/4*4;

        n-=1;
        n/=2;

        int sum =0;
        for(int k=0;k<n*2+1;k++)
        {
            for(int o=0;o<n*2+1;o++)
			{
                sum+=temp[k][o]*(data[(i+k-n)*len+(j+o-n)*3]&0xff);

            }
        }
        return sum/(n*n);
    }

	/**
	 * 高斯平滑
	 * */
	public static double[][] gaussTemplate(int n,double sigema)
	{
		if(n % 2 ==0 || n <=0) return null;
		double[][] temp = new double[n][n];
		n-=1;
		n/=2;
		for(int i=0;i<n*2+1;i++)
		{
			for(int j=0;j<n*2+1;j++)
			{
				int x = Math.abs(i-n);
				int y = Math.abs(j-n);
				temp[i][j] = Math.exp(-(x*x+y*y)/2*sigema);
			}
		}
		return temp;
		
	}

	/**
	 * 中值滤波
	 * */
	public static void middleFilter(BMPParser bmp,int n)
	{
		if(n % 2 ==0 || n <=0) return;
		
		int height = bmp.getBiHeight();
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		int len = (3*width+3)/4*4;
		n-=1;
		n/=2;
		
		for(int i=n;i<height-n;i++)
		{
			for(int j=n;j<width-n;j++)
			{
				List<Integer> l = new LinkedList<>();
				for(int k=0;k<n*2+1;k++)
				{
					for(int o=0;o<n*2+1;o++)
					{
						l.add(data[(i+k-n)*len+(j+o-n)*3]&0xff);
					}
				}
				Collections.sort(l);
				data[i*len+j*3] = (byte)(l.get((n*2+1)*(n*2+1)/2)&0xff);
				data[i*len+j*3+1] = (byte)(l.get((n*2+1)*(n*2+1)/2)&0xff);
				data[i*len+j*3+2] = (byte)(l.get((n*2+1)*(n*2+1)/2)&0xff);
			}
		}
		
	}

	/**
	 * 拉普拉斯锐化
	 * */
	public static void laplaSharping(BMPParser bmp)
	{
	    int[][] template = {{-1,-1,-1},{-1,9,-1},{-1,-1,-1}};
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		byte[] newData = new byte[data.length];
		int len = (3*width+3)/4*4;

		for(int i=0;i<data.length;i++)
		{
			newData[i]=data[i];
		}
		for(int i=1;i<height-1;i++)
		{
			for(int j=1;j<width-1;j++)
			{
				int value = juanji(bmp.getData(),template,i,j,bmp.getBiWidth());

				if(value>255)
					value=255;
				else if(value<0)
					value=0;

				newData[i*len+j*3]=(byte)value;
				newData[i*len+j*3+1]=(byte)value;
				newData[i*len+j*3+2]=(byte)value;

			}
		}
		bmp.setData(newData);
	}

	/**
	 * 高斯锐化
	 * */
	public static void guassLaplaSharping(BMPParser bmp)
	{
		int[][] template = {{-2,-4,-4,-4,-2},{-4,0,8,0,-4},{-4,8,24,8,-4},{-4,0,8,0,-4},{-2,-4,-4,-4,-2}};
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		byte[] newData = new byte[data.length];
		for(int i=0;i<data.length;i++)
		{
			newData[i]=data[i];
		}
		for(int i=2;i<height-2;i++)
		{
			for(int j=2;j<width-2;j++)
			{
				int value = juanji(bmp.getData(),template,i,j,bmp.getBiWidth());

				if(value>255)
					value=255;
				else if(value<0)
					value=0;

				newData[(i*width+j)*3]=(byte)value;
				newData[(i*width+j)*3+1]=(byte)value;
				newData[(i*width+j)*3+2]=(byte)value;

			}
		}
		bmp.setData(newData);
	}

	/**
	 * sobel锐化
	 * */
	public static void sobelSharping(BMPParser bmp)
    {
        byte[] data = bmp.getData();
        int width = bmp.getBiWidth();
        int height = bmp.getBiHeight();
	    int len = (width *3+3)/4*4;
        byte[] newData1 = new byte[data.length];
		byte[] newData2 = new byte[data.length];
        int[][] gx = {{-1,0,1},{-2,0,2},{-1,0,1}};
        int[][] gy = {{1,2,1},{0,0,0},{-1,-2,-1}};

        for(int i=0;i<data.length;i++)
        {
            newData1[i]=data[i];
			newData2[i]=data[i];
        }

        for(int i=1;i<height-1;i++)
        {
            for(int j=1;j<width-1;j++)
			{

				int fgx2 = juanji(data, gx, i, j, width);
				int fgy2 = juanji(data, gy, i, j, width);
				newData1[i * len + j*3] = (byte) Math.min(255, Math.abs(fgx2));
				newData2[i * len + j*3] = (byte) Math.min(255, Math.abs(fgy2));
			}
        }
		for(int i=0;i<height;i++)
		{
			for (int j = 0; j < width; j++)
			{
				data[i*len+j*3] = (byte) Math.max(newData1[i * len + j*3] ,newData2[i * len + j*3] );
				data[i*len+j*3+1] = (byte) Math.max(newData1[i * len + j*3] ,newData2[i * len + j*3] );
				data[i*len+j*3+2] = (byte) Math.max(newData1[i * len + j*3] ,newData2[i * len + j*3] );
			}
		}

    }

	/**
	 * 轮廓提取
	 * */
	public static void contour(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		int len = (width *3+3)/4*4;
		byte[] newData = new byte[data.length];
		for(int i=0;i<newData.length;i++)
		{
			newData[i]=(byte)255;
		}

		for(int i=1;i<height-1;i++)
		{
			for(int j=1;j<width-1;j++)
			{
				int value1 = data[(i+1)*len+j*3]&0xff;
				int value2 = data[(i-1)*len+j*3]&0xff;
				int value3 = data[(i+1)*len+(j+1)*3]&0xff;
				int value4 = data[(i+1)*len+(j-1)*3]&0xff;
				int value5 = data[(i)*len+(j+1)*3]&0xff;
				int value6 = data[(i)*len+(j-1)*3]&0xff;
				int value7 = data[(i-1)*len+(j-1)*3]&0xff;
				int value8 = data[(i-1)*len+(j+1)*3]&0xff;
				int value9 = data[(i)*len+(j)*3]&0xff;

				if(value9!=0)
					continue;

				if(value1==0&&value2==0&&value3==0&&value4==0&&value5==0&&value6==0&&value7==0&&value8==0&&value9==0)
				{
					newData[i*len+j*3] = (byte) 255;
					newData[i*len+j*3+1]= (byte) 255;
					newData[i*len+j*3+2] = (byte) 255;
				}
				else
				{
					newData[i*len+j*3] = (byte) value9;
					newData[i*len+j*3+1] = (byte) value9;
					newData[i*len+j*3+2] = (byte) value9;
				}

			}
		}
		bmp.setData(newData);
	}

	/**
	 * 提取连通分量
	 * */
	public static Map<Integer,List<Point>> getBlock(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();

		int len = (width *3+3)/4*4;

		int[][] tag = new int[width][height];
		int max=0;
		Map<Integer,List<Point>> map = new HashMap<>();

		int count=0;
		for(int i=0;i<width;i++)
		{
			for (int j = 0; j < height; j++)
			{
				if((data[j*len+i*3]&0xff)==0 && (data[j*len+i*3+1]&0xff)==0 && (data[j*len+i*3+2]&0xff)==0)
				{
					tag[i][j]=1;
					count++;
				}
				else
					tag[i][j]=0;
			}
		}
		System.out.println("count:"+count);
		map.put(1,new ArrayList<>());
		for(int i=1;i<width-1;i++)
		{
			for(int j=1;j<height-1;j++)
			{
				if(tag[i][j]==0) continue;
				if(tag[i][j-1]>0 && (tag[i-1][j]==tag[i][j-1] || tag[i-1][j]==0))
				{
					tag[i][j]=tag[i][j-1];
					map.get(tag[i][j]).add(new Point(i,j));
				}
				else if(tag[i-1][j]>0 &&  tag[i][j-1]==0)
				{
					tag[i][j]=tag[i-1][j];
					map.get(tag[i][j]).add(new Point(i,j));
				}
				else if(tag[i][j-1]!=tag[i-1][j] && tag[i][j-1]>0 && tag[i-1][j]>0)
				{
					int temp = tag[i-1][j];
					map.get(tag[i][j-1]).addAll(map.get(temp));
					for(int k=0;k<width;k++)
						for(int m=0;m<height;m++)
							if(tag[k][m]==temp)
								tag[k][m]=tag[i][j-1];

					map.remove(temp);
				//	System.out.println("temp:"+temp);
					tag[i][j]=tag[i][j-1];
					map.get(tag[i][j]).add(new Point(i,j));
				}
				else
				{
					max+=1;
					map.put(max,new LinkedList<>());
					tag[i][j]=max;
					map.get(max).add(new Point(i,j));
				//	System.out.println("new:"+max);
				}

			}

		}
		return map;
	}


	/**
	 * 根据像素集构造bmp
	 * */
	public static void fetch(BMPParser bmp,List<Point> set)
	{
		int left=20000;
		int right=-1;
		int down=-1;
		int up=200000;
		for(Point p : set)
		{
			if(p.getx()<left)
				left = p.getx();
			if(p.getx()>right)
				right=p.getx();
			if(p.gety()<up)
				up=p.gety();
			if(p.gety()>down)
				down=p.gety();
		}

		int new_width = right-left+1;
		int new_height = down-up+1;
		int newLen = new_width*3;

		if(newLen%4!=0)
		{
			newLen=newLen+(4-newLen%4);
		}


		byte[] new_data = new byte[new_height*newLen];

		for(int i=0;i<new_data.length;i++)
			new_data[i]=(byte)255;
		for(Point p : set)
		{
			new_data[(p.gety()-up)*newLen+(p.getx()-left)*3]=0;
			new_data[(p.gety()-up)*newLen+(p.getx()-left)*3+1]=0;
			new_data[(p.gety()-up)*newLen+(p.getx()-left)*3+2]=0;
		}
		bmp.setData(new_data);
		bmp.setBiWidth(new_width);
		bmp.setBiHeight(new_height);
	}


	/**
	 * 腐蚀模版判断
	 * */
	public static boolean corrosionMatchTemplate(BMPParser bmp,int i,int j,int[][] template)
	{
		int tempLen = template.length/2;
		boolean ret = false;
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;

		for(int k=0;k<template.length;k++)
		{
			for(int m=0;m<template.length;m++)
			{
				if(template[k][m]>0) //模板上大于零的点需要判断
				{
					if(data[(i+k-tempLen)*len+(j+m-tempLen)*3]!=0 || data[(i+k-tempLen)*len+(j+m-tempLen)*3+1]!=0 || data[(i+k-tempLen)*len+(j+m-tempLen)*3+2]!=0)
						return true;
				}
			}
		}
		return ret;

	}


	/**
	 * 图像腐蚀
	 * */
	public static void corrosion(BMPParser bmp,int[][] template)
	{
		int tempLen = template.length/2;
		boolean ret = false;
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();

		byte[] newDate = new byte[data.length];
		for(int i=0;i<data.length;i++)
			newDate[i]=(byte) 255;

		for(int i=tempLen;i<height-tempLen;i++)//每一个像素
		{
			for(int j=tempLen;j<width-tempLen;j++)
			{
				if(data[i*len+j*3]==0 && data[i*len+j*3+1]==0 && data[i*len+j*3+2]==0) //是黑点
				{
					if(!corrosionMatchTemplate(bmp,i,j,template)) //判断是否需要删除
					{
						newDate[i*len+j*3] = 0;
						newDate[i*len+j*3+1] = 0;
						newDate[i*len+j*3+2] = 0;
					}

				}
			}
		}
		bmp.setData(newDate);
	}

	/**
	 * 膨胀模版判断
	 * */
	public static boolean expandMatchTemplate(BMPParser bmp,int i,int j,int[][] template)
	{
		int tempLen = template.length/2;
		boolean ret = false;
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;

		for(int k=0;k<template.length;k++)
		{
			for(int m=0;m<template.length;m++)
			{
				if(template[k][m]>0)
				{
					if(data[(i+k-tempLen)*len+(j+m-tempLen)*3]==0 && data[(i+k-tempLen)*len+(j+m-tempLen)*3+1]==0 && data[(i+k-tempLen)*len+(j+m-tempLen)*3+2]==0)
						return true;
				}
			}
		}
		return ret;
	}

	/**
	 * 图像膨胀
	 * */
	public static void expand(BMPParser bmp,int[][] template)
	{
		int tempLen = template.length/2;
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();

		byte[] newDate = new byte[data.length];
		for(int i=0;i<data.length;i++)
			newDate[i]=(byte) 255;

		for(int i=tempLen;i<height-tempLen;i++)
		{
			for(int j=tempLen;j<width-tempLen;j++)
			{
				if(data[i*len+j*3]!=0 && data[i*len+j*3+1]!=0 && data[i*len+j*3+2]!=0) //是否是背景
				{
					if(expandMatchTemplate(bmp,i,j,template))
					{
						newDate[i*len+j*3] = 0;
						newDate[i*len+j*3+1] = 0;
						newDate[i*len+j*3+2] = 0;
					}
				}
				else
				{
					newDate[i*len+j*3] = 0;
					newDate[i*len+j*3+1] = 0;
					newDate[i*len+j*3+2] = 0;
				}
			}
		}
		bmp.setData(newDate);

	}


	/**
	 * 裁剪bmp
	 * */
	public static BMPParser cutBMP(BMPParser bmp,int startX,int endX,int startY,int endY)
	{
		BMPParser newBMP = bmp.newBmp();

		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();

		int newHeight = endY-startY;
		int newWidth = endX-startX;
		int newLen = (3*newWidth+3)/4*4;
		byte[] newData = new byte[newLen*newHeight];
		for(int i=0;i< newData.length;i++)
			newData[i]=(byte)255;


		for(int i=startY;i<endY;i++)
		{
			for(int j=startX;j<endX;j++)
			{
				newData[(i-startY)*newLen+3*(j-startX)] = data[i*len+3*j];
				newData[(i-startY)*newLen+3*(j-startX)+1] = data[i*len+3*j+1];
				newData[(i-startY)*newLen+3*(j-startX)+2] = data[i*len+3*j+2];
			}
		}

		newBMP.setData(newData);
		newBMP.setBiHeight(newHeight);
		newBMP.setBiWidth(newWidth);
		return newBMP;
	}
	public static BMPParser cutBMP(BMPParser bmp, List<Point> set,int widthk,int heightk)
	{
		int left=20000;
		int right=-1;
		int down=-1;
		int up=200000;
		for(Point p : set)
		{
			if(p.getx()<left)
				left = p.getx();
			if(p.getx()>right)
				right=p.getx();
			if(p.gety()<up)
				up=p.gety();
			if(p.gety()>down)
				down=p.gety();
		}

		return cutBMP(bmp,left-widthk,right+widthk,up-heightk,down+heightk);
	}

	/**
	 * 依照蓝色二值化
	 * */
	public static void binaryzationBaseBlue(BMPParser bmp,byte value)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();

		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int b = data[i*len+j*3]&0xff;
				int g = data[i*len+j*3+1]&0xff;
				int r = data[i*len+j*3+2]&0xff;
				if(isBlue(r,g,b))
				{
					data[i*len+j*3]=value;
					data[i*len+j*3+1]=value;
					data[i*len+j*3+2]=value;
				}

			}
		}
	}

	/**
	 * 依照白色二值化
	 * */
	public static void binaryzationBaseWhite(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();

		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				int b = data[i*len+j*3]&0xff;
				int g = data[i*len+j*3+1]&0xff;
				int r = data[i*len+j*3+2]&0xff;
				int gray = (g+r+b)/3;
				if(!isWhite(r,g,b))
				{
					data[i*len+j*3]=(byte)255;
					data[i*len+j*3+1]=(byte)255;
					data[i*len+j*3+2]=(byte)255;
				}
				else
				{
					data[i*len+j*3]=0;
					data[i*len+j*3+1]=0;
					data[i*len+j*3+2]=0;
				}
			}

		}

	}


	public static boolean isWhite(int r,int g,int b)
	{
		double v=0;
		double h=0;
		double s=0;

		v = Math.max(r,Math.max(g,b));
		double temp = ( v-Math.min(r,Math.min(g,b)) );
		if(v!=0)
			s = temp/v;

		if(v==r)
			h = 60*(g-b)/temp;
		else if(v==g)
			h = 120+60*(b-r)/temp;
		else if(v==b)
			h = 240+60*(r-g)/temp;

		if(h<0)
			h+=360;
		v/=255;
		return s<0.4 && v>0.45;
	}
	public static boolean isBlue(int r,int g,int b)
	{
		double v=0;
		double h=0;
		double s=0;

		v = Math.max(r,Math.max(g,b));
		double temp = ( v-Math.min(r,Math.min(g,b)) );
		if(v!=0)
			s = temp/v;

		if(v==r)
			h = 60*(g-b)/temp;
		else if(v==g)
			h = 120+60*(b-r)/temp;
		else if(v==b)
			h = 240+60*(r-g)/temp;

		if(h<0)
			h+=360;

		v/=255;

		return h<220&&h>200 && s>0.5 && v>0.2;
	}
}



























