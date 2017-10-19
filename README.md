## 车牌识别介绍
本程序识别车牌主要分为4个步骤

> 1. 预处理
> 2. 车牌定位
> 3. 字符分割
> 4. 字符识别


- 预处理：针对车牌的颜色特征，利用hsv色域进行二值化，并进行膨胀和去噪处理
- 车牌定位：对预处理后的图片进行区域标记，根据各连通量的长宽比以及黑色像素所占比例定位车牌位置
- 字符分割：将定位后的车牌进行去噪处理，然后对车牌进行投影根据车牌内黑像素频率变化分隔字符
- 字符识别：将分割出来的字符做n等分分割，统计个等分块内的黑像素比例，并与特征库比对，识别字符

<img src="https://raw.githubusercontent.com/xhystc/PictureRepository/master/%E8%BD%A6%E7%89%8C%E8%AF%86%E5%88%AB-%E6%B5%8B%E8%AF%95%E6%A0%B7%E4%BE%8B.png" width=60%><br/>
测试用例

<img src="https://raw.githubusercontent.com/xhystc/PictureRepository/master/%E8%BD%A6%E7%89%8C%E8%AF%86%E5%88%AB-%E4%BA%8C%E5%80%BC%E5%8C%96%E6%95%88%E6%9E%9C.png" width=60%><br/>
二值化效果

<img src="https://raw.githubusercontent.com/xhystc/PictureRepository/master/%E8%BD%A6%E7%89%8C%E8%AF%86%E5%88%AB-%E8%BD%A6%E7%89%8C%E5%AE%9A%E4%BD%8D.png" width=40%><br/>
车牌定位

<img src="https://raw.githubusercontent.com/xhystc/PictureRepository/master/%E8%BD%A6%E7%89%8C%E8%AF%86%E5%88%AB-%E8%87%AA%E8%B4%9F%E5%88%86%E5%89%B2.png" width=40%><br/>
字符分割

<img src="https://raw.githubusercontent.com/xhystc/PictureRepository/master/%E8%AF%86%E5%88%AB%E7%BB%93%E6%9E%9C.png" width=40%><br/>
识别结果
