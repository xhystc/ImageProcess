package com.xhystc.bmp;

public class Point 
{
	int x;
	int y;
	
	public void setx(int value)
	{
		x=value;
	}
	public void sety(int value)
	{
		y=value;
	}
	
	public int getx()
	{
		return x;
	}
	public int gety()
	{
		return y;
	}

	public Point(int x,int y)
	{
		this.x=x;
		this.y=y;
	}
	public Point()
	{
		this.x=0;
		this.y=0;
	}

	@Override
	public int hashCode()
	{
		return 7*x+11*y;
	}

	@Override
	public boolean equals(Object o )
	{

		if(o==this )
			return true;
		if(o!=null && o.getClass()==this.getClass())
		{
			Point p = (Point)o;
			return p.x==this.x&&p.y==this.y;
		}
		return false;
	}


}
