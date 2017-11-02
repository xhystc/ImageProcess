package com.xhystc;

import com.xhystc.bmp.*;

import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Demo
{
	public static void main(String[] args) throws Exception
	{


		BMPParser bmp = new BMPParser(new FileInputStream("bmp\\test3.bmp"));
		BMPParser source = bmp.clone();
		BMP24BitProcessor.binaryzationBaseBlue(bmp,(byte) 0); //二值化

		int[][] template = {{0,0,0,0,0},{1,1,1,1,1},{0,0,0,0,0}};
		BMP24BitProcessor.expand(bmp,template);
		BMP24BitProcessor.expand(bmp,template); //做两次膨胀操作填补空隙

		bmp.save(new FileOutputStream("bmp\\test-b.bmp")); //保存中间结果


		int maxCount=0;
		BMPParser target=null;

		int i=0;
		Map<Integer,List<Point>> getBlockResult = BMP24BitProcessor.getBlock(bmp); //提取连通域
		for(Map.Entry<Integer,List<Point>> en : getBlockResult.entrySet())  //按照车牌长宽比例过滤
		{
			BMPParser b = bmp.newBmp();
			BMP24BitProcessor.fetch(b,en.getValue()); //用像素集合构造位图
			if(b.getBiHeight()*b.getBiWidth()<bmp.getBiWidth()*bmp.getBiHeight()*0.001 || b.getBiWidth()<b.getBiHeight() || b.getBiHeight()*4<b.getBiWidth())
			{
				continue;
			}

			int temp=0;
			BMPParser t = BMP24BitProcessor.cutBMP(source,en.getValue(),0,0); //从原始图片中截取
			if((temp=CharacterParser.countBlack(b))>maxCount) //统计有效像素个数,太小忽略
			{
				target=t;
				maxCount = temp;
			}
			t.save(new FileOutputStream("bmp\\test1-"+i+".bmp"));
			i++;
		}

		if(target==null)
		{
			throw new Exception("未发现车牌");
		}



		bmp.save(new FileOutputStream("bmp\\test-k.bmp"));
		target.save(new FileOutputStream("bmp\\test-t.bmp"));  //保存中间结果
		List<BMPParser> res;

		BMP24BitProcessor.binaryzationBaseWhite(target); //针对白色二值化
		target.save(new FileOutputStream("bmp\\test-b2.bmp"));
		int[][] template2 = {{0,0,0},{1,1,1},{0,0,0}};

		BMP24BitProcessor.expand(target,template2); //对目标图片做一次膨胀


		res = CharacterParser.cutCharacter(target,false); //字符分割
		i=0;

		Map<String,TeZhengPoint> zhengPointMap = CharacterParser.readFeatureLibrary(Paths.get("bmp\\汽车牌照特征库"),3); //读取特征库
		List<CountPair> result = new ArrayList<>();
		for(BMPParser b : res) //对于每个字符
		{

			b.save(new FileOutputStream("bmp\\test2-"+i+".bmp"));
			System.out.println("parse"+i);
			CountPair cp = CharacterParser.parseCharater(b,zhengPointMap,3); //解析字符，并统计相应得分
			result.add(cp);
			i++;
			System.out.println();
		}

		target.save(new FileOutputStream("bmp\\test-result.bmp"));
		i=0;
		for(CountPair<String> cp : result) //打印结果
		{
			System.out.println("target:"+i+" char:"+cp.getI()+" rank:"+cp.getD());
			i++;
		}

		List<CountPair> tempResult = new ArrayList<>(result);//原来的list保留字符顺序信息
		Collections.sort(tempResult);//新list对字符得分排序

		//输出结果
		tempResult = tempResult.subList(tempResult.size()-7,tempResult.size());
		for(CountPair<String> cp : result)
		{
			if(tempResult.contains(cp))
				System.out.print(cp.getI().charAt(0));
		}



	}
}












