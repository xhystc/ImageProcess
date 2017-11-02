package com.xhystc.bmp;

/**
 * Created by 87173 on 2017/4/27.
 */
public class CountPair<T> implements Comparable<CountPair>
{
    T i=null;
    double d = 0.0;

    public T getI()
    {
        return i;
    }

    public void setI(T i)
    {
        this.i = i;
    }

    public double getD()
    {
        return d;
    }

    public void setD(double d)
    {
        this.d = d;
    }

    public CountPair(T i,double d)
    {
        this.i=i;
        this.d=d;
    }
    public CountPair()
    {
        this.i=null;
        this.d=0;
    }
    @Override
    public int hashCode()
    {
        return 7*(int)d+11*i.hashCode();
    }

    @Override
    public boolean equals(Object o )
    {
        if(o==this)
            return true;
        if(o!=null && o.getClass()==this.getClass())
        {
            CountPair p = (CountPair)o;
            return p.d==this.d&&p.i==this.i;
        }
        return false;
    }
    public int compareTo(CountPair cp)
    {
        if(cp.getD()>this.getD()) return -1;
        else if(cp.getD()<this.getD()) return 1;
        return 0;
    }


}
