package cn.com.cg.ocr.testocr.utils;

/**
 * Discription  { 递推快速计算方差和均值 }
 * author  chenguo7
 * Date  2020/1/16 19:17
 * https://blog.csdn.net/u014485485/article/details/77679669
 */

public class Calculator {
    private double m;
    private double s;
    private int N;


    public void addDataValue(double x)
    {
        N++;
        s=s+1.0*(N-1)/N*(x-m)*(x-m);
        m=m+(x-m)/N;
    }
    public double mean()
    {
        return  m;
    }
    public double var()
    {
        return s/(N-1);
    }
    public double stddev()
    {
        return Math.sqrt(this.var());
    }
}