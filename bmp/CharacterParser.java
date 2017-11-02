package com.xhystc.bmp;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

public class CharacterParser
{

	public static double[][] denfen(BMPParser bmp,int denfen)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		double[][] res = new double[denfen][denfen];
		double sum = (width*height)/(denfen*denfen);

		int perWidth = width/denfen;
		int perHeight = height/denfen;

		for(int i=0;i<denfen;i++)
		{
			for(int j=0;j<denfen;j++)
			{
				int temp = countBlack(bmp,j*perWidth,(j+1)*perWidth-1,i*perHeight,(i+1)*perHeight-1);
				res[i][j] = temp/sum;
			}
		}
		return res;

	}



	static int cutLineRowDetection(BMPParser bmp,int row)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();

		int state = 0;
		int count=0;

		for(int i =0;i<width;i++)
		{
			if((data[row*len+i*3]&0xff)==0)
			{
				state=1;
			}
			else
			{
				if(state==1)
				{
					state=0;
					count++;
				}
			}
		}

		return count;
	}
	static int cutLineColDetection(BMPParser bmp,int col,int start,int end)
	{
		byte[] data = bmp.getData();
		int width = bmp.getBiWidth();
		int len = (3*width+3)/4*4;
		int height = bmp.getBiHeight();



		int state = 0;
		int count=0;

		for(int i =start;i<end+1;i++)
		{
			if((data[i*len+col*3]&0xff)==0 && state==0)
			{
				state=1;
			}
			else
			{
				if(state==1)
				{
					state=0;
					count++;
				}
			}
		}

		return count;
	}

	static List<Integer>fetchCharacterRowBlock(BMPParser bmp)
	{
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();

		int state=0;
		List<Integer> ret = new ArrayList<>();

		for(int i=0;i<height;i++)
		{
			if(cutLineRowDetection(bmp,i)>7)
			{
				if(state==0)
				{
					ret.add(i);
					state=1;
				}
			}
			else
			{
				if(state==1)
				{
					ret.add(i);
					state=0;
				}


			}
		}
		return ret;
	}
	static List<Integer>fetchCharacterColBlock(BMPParser bmp,int start,int end)
	{
		int width = bmp.getBiWidth();

		int state=0;
		List<Integer> ret = new ArrayList<>();

		int count;
		for(int i=0;i<width;i++)
		{
			if((count=cutLineColDetection(bmp,i,start,end))>1)
			{
				if( state==0)
				{
					state=1;
					ret.add(i);
					System.out.println("col start:"+count);
				}
			}
			else
			{
				if(state==1)
				{
					state=0;
					ret.add(i);
					System.out.println("col end:"+count);
				}

			}
		}
		return ret;
	}

	public static List<BMPParser> cutCharacter(BMPParser bmp,boolean debug)
	{
		int width = bmp.getBiWidth();
		byte[] data = bmp.getData();
		int height = bmp.getBiHeight();
		int len = (3*width+3)/4*4;

		double lessWidth = 0.08*bmp.getBiWidth();
		List<BMPParser> ret = new LinkedList<>();

		int startY=0;
		int endY=0;
		List<Integer> rowCut = fetchCharacterRowBlock(bmp);


		for(int i=rowCut.size()-1;i>=0;i--)
		{

			if(i!=0 && rowCut.get(i)-rowCut.get(i-1)>endY-startY)
			{
				endY=rowCut.get(i);
				startY = rowCut.get(i-1);
			}
			//		System.out.println("i:"+rowCut.get(i)+" dis:"+(start-end));
		}

		if(debug)
		{
			for(int i=0;i<width;i++)
			{
				data[startY*len+i*3] = 100;
				data[startY*len+i*3+1] = 100;
				data[startY*len+i*3+2] = 0;
			}
			for(int i=0;i<width;i++)
			{
				data[endY*len+i*3] = 0;
				data[endY*len+i*3+1] = 100;
				data[endY*len+i*3+2] = 100;
			}
		}
		List<Integer> colCut = fetchCharacterColBlock(bmp,startY,endY);

		int state=0;

		int startX =0;
		for(int i=0;i<colCut.size();i++)
		{

			if(state==0)
			{


				if(debug)
				{
					for(int k=startY;k<endY;k++)
					{
						data[k*len+colCut.get(i)*3] = 100;
						data[k*len+colCut.get(i)*3+1] = 100;
						data[k*len+colCut.get(i)*3+2] = 0;
					}
				}
				startX=colCut.get(i);
				state=1;
			}
			else
			{
				if(debug)
				{
					for(int k=startY;k<endY;k++)
					{
						data[k*len+colCut.get(i)*3] = 0;
						data[k*len+colCut.get(i)*3+1] = 100;
						data[k*len+colCut.get(i)*3+2] = 100;
					}
				}
				ret.add(BMP24BitProcessor.cutBMP(bmp,startX,colCut.get(i),startY,endY));
				state=0;
			}
		}
		return ret;
	}
	public static double countBiLv(double[][] b1,double[][] b2)
	{
		double res=1;
		int k = b1.length;
		for(int i=0;i<k;i++)
		{
			for(int j=0;j<k;j++)
			{

				double temp=0;
				temp = 1-Math.abs(b1[i][j]-b2[i][j]);
				res*=temp;
			}
		}
		return res;
	}
	public static CountPair coutAllBiLv(double[][] b, Map<Integer,TeZhengPoint> tezheng, BMPParser bmp)
	{

		double res=0;
		int max=0;
		for(Map.Entry<Integer,TeZhengPoint> en : tezheng.entrySet())
		{
			double temp;
			temp = countBiLv(b,en.getValue().getBi());
			//	System.out.println("temp特征:"+en.getKey()+" res:"+temp);
			double rate1 = compareHeightWidthRate(bmp,en.getValue());
			double rate2 = compareXieRate(bmp,en.getValue());
			rate1*=rate2;
			if(temp*rate2*rate2>res)
			{
				res=temp*rate1*rate2;
				max=en.getKey();

			}
		}

		return new CountPair(max,res);
	}


	private static double compareHeightWidthRate(BMPParser bmp,TeZhengPoint tp)
	{
		double rate1 = tp.getWidthHeightRate();
		double rate2 = bmp.getBiWidth()*1.0/bmp.getBiHeight();

		if(rate1>rate2)
			rate1=rate2/rate1;
		else
			rate1=rate1/rate2;

		return rate1;
	}
	private static double compareXieRate(BMPParser bmp,TeZhengPoint tp)
	{
		double rate1 = tp.getXieCount();
		double rate2 = xieCount(bmp);
		if(rate1>rate2)
			rate1=rate2/rate1;
		else
			rate1=rate1/rate2;

		return rate1;
	}

	public static CountPair<String> parseCharater(BMPParser bmp,Map<String,TeZhengPoint> tezheng,int denfen)
	{
		double[][] res = denfen(bmp,denfen);
		double maxd=0.0;
		String max="";

		if(bmp.getBiHeight()>bmp.getBiWidth()*3 )
		{
			if(countBlack(bmp)*1.0/(bmp.getBiHeight()*bmp.getBiWidth()) >0.9  && bmp.getBiWidth()>5)
				return new CountPair<>("1",1);
			else
				return new CountPair<>("1",0);
		}



		for(Map.Entry<String,TeZhengPoint> en : tezheng.entrySet())
		{
			double temp = countBiLv(res,en.getValue().getBi());
			double xie = xieCount(bmp);
			double xieRate = 1-Math.abs(xie-en.getValue().getXieCount());
			double count = countBlack(bmp)*1.0/(bmp.getBiHeight()*bmp.getBiWidth());
			double countRate =  1-Math.abs(count-en.getValue().getBlackCountRate());


			if(temp*xieRate*countRate>maxd)
			{
				maxd=temp*xieRate*countRate;
				max=en.getKey();
			}
			System.out.println("match:"+en.getKey()+" rank:"+temp*xieRate*countRate);
		}

		return new CountPair(max,maxd);
	}

	public static CountPair parseInt2(BMPParser bmp, List<Map<Integer,TeZhengPoint>> tezheng, int denfen)
	{

		double[][] res = denfen(bmp,denfen);
		double maxd=0.0;
		int max=0;
		int i=0;
		Map<Integer,Double> count = new HashMap<>();
		for(Map<Integer,TeZhengPoint> map : tezheng)
		{
			CountPair temp = coutAllBiLv(res,map,bmp);
			TeZhengPoint tp = map.get(temp.getI());

			System.out.println("特征:"+temp.getI()+" value:"+temp.getD());

			if(maxd<temp.getD())
			{
				max = (Integer) temp.getI();
				maxd = temp.getD();
			}
		}
		return new CountPair(max,maxd);
	}


	public static int countBlack(BMPParser bmp)
	{
		return countBlack(bmp,0,bmp.getBiWidth(),0,bmp.getBiHeight());
	}
	public static int countBlack(BMPParser bmp,int left,int right,int up,int down)
	{
		byte[] data = bmp.getData();
		if(data.length<=0) return 0;
		int width = bmp.getBiWidth();

		int len = (width *3+3)/4*4;
		int count=0;
		for(int i=up;i<down;i++)
		{
			for (int j = left; j < right; j++)
			{
				if((data[(i*len+j*3)]&0xff)==0 )
				{
					count++;
				}
			}
		}
		return count;
	}



	public static double xieCount(BMPParser bmp)
	{
		byte[] data = bmp.getData();
		//	if(data.length<=0) return 0.0;
		int width = bmp.getBiWidth();
		int height = bmp.getBiHeight();
		int len = (3*width+3)/4*4;
		int k = width/height;
		int count=0;

		for(int i=0;i<height;i++)
		{
			int j = k*i;
			if((data[i*len+j*3]&0xff)==0 && (data[i*len+j*3+1]&0xff)==0 &&(data[i*len+j*3+2]&0xff)==0 )
			{
				count++;
			}
		}
		double rate = Math.sqrt(width*width+height*height);
		return count/rate;
	}

	public static Map<String,TeZhengPoint> readFeatureLibrary(Path path, int n) throws Exception
	{
		File[] files = path.toFile().listFiles();
		Map<String,TeZhengPoint> ret = new HashMap<>();

		for(File file : files)
		{
			if(!file.isDirectory() && file.getName().endsWith(".bmp"))
			{
				System.out.println("get lib:"+file.getName());
				BMPParser bmp = new BMPParser(new FileInputStream(file));
				if(bmp.getBiWidth()<n*5 || bmp.getBiHeight()<n*5)
					continue;
				System.out.println("width:"+bmp.getBiWidth()+" height:"+bmp.getBiHeight()+" data:"+bmp.getData().length);
				ret.put(file.getName(),new TeZhengPoint(bmp,n));
			}
		}
		return ret;
	}


}
