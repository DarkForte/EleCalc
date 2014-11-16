package com.zero.elecalc;

public class Element 
{
	public String name;
	public double tc;
	public double pc;
	public double omega;
	
	Element(){}
	Element(String _n, double t, double p, double o)
	{
		name=_n;
		tc=t;
		pc=p;
		omega=o;
	}
	public void set(String _n, double t, double p, double o)
	{
		name=_n;
		tc=t;
		pc=p;
		omega=o;
	}
}
