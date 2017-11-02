package com.xhystc.bmp;

/**
 * Created by 87173 on 2017/4/29.
 */
public class TeZhengPoint
{
    double[][] bi;
    int height;
    int width;
    double blackCountRate;
    int denfen;
    double xieCount;

    public double[][] getBi()
    {
        return bi;
    }

    public void setBi(double[][] bi)
    {
        this.bi = bi;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getDenfen()
    {
        return denfen;
    }

    public void setDenfen(int denfen)
    {
        this.denfen = denfen;
    }

    public double getBlackCountRate()
    {
        return blackCountRate;
    }

    public void setBlackCountRate(double blackCount)
    {
        this.blackCountRate = blackCount;
    }


    public TeZhengPoint(BMPParser bmp, int denfen)
    {
        bi = CharacterParser.denfen(bmp,denfen);
        this.denfen=denfen;

        height=bmp.getBiHeight();
        width=bmp.getBiWidth();
	    blackCountRate= CharacterParser.countBlack(bmp)*1.0/(height*width);
        xieCount = CharacterParser.xieCount(bmp);
    }
    public double getWidthHeightRate()
    {
        return width*1.0/height;
    }

    public double getXieCount()
    {
        return xieCount;
    }

    public void setXieCount(double xieCount)
    {
        this.xieCount = xieCount;
    }
}
