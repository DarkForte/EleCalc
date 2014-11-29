package com.zero.elecalc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.os.Bundle;
import android.app.Activity;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

class Formula
{
	String name;
	int num;
	Formula(){}
	public Formula(String _n, int _num)
	{
		name=_n;
		num=_num;
	}
	public String toString()
	{
		return name;
	}
	
};

class Solution
{
	int sol_num;
	double x[];
	Solution(){}
	Solution(int sol, double s_x[])
	{
		x = new double[5];
		sol_num = sol;
		int i;
		for(i=1; i<=sol; i++)
			x[i] = s_x[i];
	}
	Solution(int sol, double s_x)
	{
		x = new double[5];
		sol_num = sol;
		x[1] = s_x;
	}
	public String toString()
	{
		if(sol_num==1)
			return x[1]+"";
		else
		{
			String ret = "";
			int i;
			for(i=1; i<=sol_num; i++)
			{
				if(x[i] >0)
					ret += x[i] + " ";
			}
			return ret;
		}
	}
}

public class MainActivity extends Activity 
{
	EditText name_box, t_box, p_box, v_box;
	Spinner choose_spinner;
	Button go_button;
	TextView ans_area;
	
	List<Element> ele_list;
	List<Formula> formula_list;
	int f_type;
	ArrayAdapter<Formula> adapter;
	
	final int PT2V=1, VT2P=2;
	final double r=8.314; //R is defined in Android SDK so use r
	
	void ReadFile()
	{
		InputStream inputstream = null;
		try
		{
			inputstream = getResources().openRawResource(R.raw.data);
		}
		catch(NullPointerException e)
		{
			Toast.makeText(MainActivity.this, 
					"没有找到文件",
					Toast.LENGTH_LONG).show();
			return;
		}
		Scanner cin = new Scanner(inputstream, "GBK");
		//InputStreamReader isReader = new InputStreamReader(inputstream, "gbk");
		//BufferedReader br = new BufferedReader(isReader);
		
		while (cin.hasNext() )
		{
			String st;
			double t;
			double p;
			double o;
			st = cin.next();
			t = cin.nextDouble();
			p = cin.nextDouble();
			o = cin.nextDouble();
			
			Element tmp = new Element(st, t, p, o);
			ele_list.add(tmp);
			//System.out.println(st);
		}
		cin.close();
		return;
	}
	
	void InitFormula()
	{
		formula_list.add(new Formula("1.理想气体方程",1));
		formula_list.add(new Formula("2.VDW方程",2));
		formula_list.add(new Formula("3.RK方程",3));
		formula_list.add(new Formula("4.SRK方程",4));
		formula_list.add(new Formula("5.PR方程",5));
		formula_list.add(new Formula("6.三参数对比态维里方程",6));
		
		adapter = new ArrayAdapter<Formula>
					(this, android.R.layout.simple_spinner_item,formula_list);
		choose_spinner.setAdapter(adapter);
	}
	
	Element search(String tar_string)
	{
		int i;
		for(i=0; i<ele_list.size(); i++)
		{
			Element now = (Element) ele_list.get(i);
			if(tar_string.equals(now.name))
				return now;
		}
		return null;
	}
	
	/*double df_3(double a, double b, double c, double x)
	{
		return 3*a*x*x + 2*b*x + c;
	}

	double f_3(double a, double b, double c, double d, double x)
	{
		return a*x*x*x + b*x*x + c*x + d;
	}*/
	
	Solution solve_3_equation(double a, double b, double c, double d) //solve cube equation
	{
		/*//newton iteration
		double x0 = 0;
		double eps = 1e-6;
		while(true)
		{
			double y = f_3(a,b,c,d,x0);
			if(Math.abs(y)<eps)
				return x0;
			double dy = df_3(a,b,c,x0);
			x0 = x0-y/dy;
		}*/
		
		//shengjin formula
		double A = b*b - 3*a*c;
		double B = b*c - 9*a*d;
		double C = c*c - 3*b*d;
		
		if(A==B && A==0)
			return new Solution(1, -b/(3*a) );
		
		double delta = B*B - 4*A*C;
		if(delta > 0)
		{
			double y1 = A*b + 3*a*(-B+Math.sqrt(delta))/2;
			double y2 = A*b + 3*a*(-B-Math.sqrt(delta))/2;
			double ans = (-b-Math.pow(y1, 1.0/3.0)+Math.pow(y2, 1.0/3.0))/(3*a);
			return new Solution(1, ans);
		}
		else if(delta == 0)
		{
			double k = B/A;
			if(-b/a + k > 0)
				return new Solution(1, -b/a + k );
			else
				return new Solution(1, -k/2);
		}
		else
		{
			double T= (2*A*b - 3*a*B) / (2*Math.pow(A,1.5) );
			double ceta = Math.acos(T);
			double x[] = new double[5];
			x[1] = (-b-2*Math.sqrt(A)*Math.cos(ceta/3))/(3*a);
			x[2] = (-b+Math.sqrt(A)* ( Math.cos(ceta/3)+Math.sqrt(3)*Math.sin(ceta/3) ) )/(3*a);
			x[3] = (-b+Math.sqrt(A)* ( Math.cos(ceta/3)-Math.sqrt(3)*Math.sin(ceta/3) ) )/(3*a);
			return new Solution(3, x);
		}
	}
	
	double calc_a(Element now_e, double t, double m, int f_type) // used to calc a in f 4/5
	{
		double arg = (f_type == 4) ? 0.42748 : 0.45724;
		double ans = arg * (r*r * now_e.tc * now_e.tc) / now_e.pc;
		double tmp = ( 1 + m * ( 1 - Math.sqrt(t/now_e.tc) ) );
		tmp *= tmp;
		return ans*tmp;
	}
	
	Solution solve(Element now_e, double arg, double t, int type, int f_type)
	{
		if(f_type == 1)
		{
			return new Solution(1, r * t / arg); //pv = rt
		}
		else if(f_type == 2)
		{
			double a = 27* (r * now_e.tc) * (r * now_e.tc) / (64 * now_e.pc);
			double b = (r * now_e.tc) / (8 * r * now_e.pc);
			
			if(type == VT2P)
			{
				double v = arg;
				double ans = r * t / (v - b);
				ans -= a/(v*v);
				return new Solution(1,ans);
			}
			else
			{
				double p = arg;
				Solution ans = solve_3_equation(p, -(b+r*t), a, a*b);
				return ans;
			}
		}
		else if(f_type == 3)
		{
			double a = 0.42748*(r * r * Math.pow(now_e.tc, 2.5));
			a/=now_e.pc;
			double b = 0.08664*(r * now_e.tc) / now_e.pc;
			//System.out.println(a+ " " + b);
			if(type == VT2P)
			{
				double v = arg;
				double ans = r * t / (v-b);
				//System.out.println(ans);
				ans -= a/(Math.sqrt(t) * v * (v+b));
				return new Solution(1,ans);
			}
			else
			{
				double p = arg;
				double A = p;
				double B = r*t;
				double C = -p * b * b - b * r * t + a / Math.sqrt(t);
				double D = -a / Math.sqrt(t) * b;
				Solution ans = solve_3_equation(A, B, C, D);
				return ans;
			}
		}
		else if(f_type == 4)
		{
			double m = 0.48 + 1.574 * now_e.omega -0.176 * now_e.omega * now_e.omega;
			double a = calc_a(now_e, t, m, f_type);
			double b = 0.08664*(r*now_e.tc / now_e.pc);
			
			if(type == VT2P)
			{
				double v = arg;
				double ans = (r*t) / (v-b);
				ans -= a / ( v * (v+b) );
				return new Solution(1,ans);
			}
			else
			{
				double p = arg;
				double A = p;
				double B = r*t;
				double C = -p * b * b - b * r * t + a;
				double D = -a * b;
				Solution ans = solve_3_equation(A, B, C, D);
				return ans;
			}
		}
		else if(f_type == 5)
		{
			double m = 0.37646 + 1.54226 * now_e.omega -0.26992 * now_e.omega * now_e.omega;
			double a = calc_a(now_e, t, m, f_type);
			double b = 0.0778*(r*now_e.tc / now_e.pc);
			
			if(type == VT2P)
			{
				double v = arg;
				double ans = (r*t) / (v-b);
				ans -= a / ( v * (v+b) + b*(v-b) );
				return new Solution(1,ans);
			}
			else
			{
				double p = arg;
				double A = p;
				double B = b*p - r*t;
				double C = -3 *p * b*b - 2 * b * r * t + a;
				double D = p*b*b*b - a*b + r*t* b*b;
				Solution ans = solve_3_equation(A, B, C, D);
				return ans;
			}
		}
		else // f_type =6
		{
			double b0 = 0.422 / Math.pow(t / now_e.tc, 1.6);
			b0 = 0.083 - b0;
			//System.out.println(b0);
			double b1 = 0.172 / Math.pow(t / now_e.tc, 4.2);
			b1 = 0.139 - b1;
			double b = b0 + now_e.omega *b1;
			b*=r * now_e.tc / now_e.pc;
			
			if(type == VT2P)
			{
				double v = arg;
				return new Solution(1,r*t / (v-b));
			}
			else
			{
				double p = arg;
				return new Solution(1, (r*t + b*p) / p );
			}
		}
	}
	
	void ShowAnswer(Element now_e, double arg, double t, int type, int f_type, Solution ans)
	{
		String buffer;
		String ele_info;
		if(f_type == 1)
			ele_info = "";
		else
			ele_info = now_e.name + " tc=" +now_e.tc + " pc=" +now_e.pc + " omega="+now_e.omega;
		String s_type = (type == VT2P) ? "v="+arg : "p=" + arg ;
		String a_type = (type == VT2P) ? "p="+ans.toString() : "v=" + ans.toString() ;
		buffer = ele_info + "\n" + s_type + " " + a_type;
		ans_area.setText(buffer);
		return;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		name_box = (EditText)findViewById(R.id.materialID);
		t_box = (EditText)findViewById(R.id.tID);
		p_box = (EditText)findViewById(R.id.pID);
		v_box = (EditText)findViewById(R.id.vID);
		choose_spinner = (Spinner)findViewById(R.id.spinnerID);
		go_button = (Button)findViewById(R.id.calcID);
		ans_area = (TextView)findViewById(R.id.answerID);
		
		ele_list = new ArrayList<Element>();
		formula_list = new ArrayList<Formula>();
		
		ReadFile();
		InitFormula();
		
		go_button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				String tar_name = name_box.getText().toString();
				String t_string = t_box.getText().toString();
				String p_string = p_box.getText().toString();
				String v_string = v_box.getText().toString();
				
				if(t_string.length() == 0)
				{
					Toast.makeText(MainActivity.this, 
							"T必须要填写",
							Toast.LENGTH_LONG).show();
					return;
				}
				if(p_string.length() == 0 && v_string.length() == 0)
				{
					Toast.makeText(MainActivity.this, 
							"P和V不能全空着",
							Toast.LENGTH_LONG).show();
					return;
				}
				if( p_string.length()>0 && v_string.length()>0)
				{
				//	System.out.println(p_string);
				//	System.out.println(v_string);
					Toast.makeText(MainActivity.this, 
							"P和V都填了是想闹哪样？",
							Toast.LENGTH_LONG).show();
					return;
				}
				if(f_type==-1)
				{
					Toast.makeText(MainActivity.this, 
							"请选择一个方程",
							Toast.LENGTH_LONG).show();
					return;
				}
				
				int type;
				double t = Double.parseDouble(t_string);
				double arg;
				if(v_string.length() == 0)
				{
					arg = Double.parseDouble(p_string);
					type = PT2V;
				}
				else
				{
					arg = Double.parseDouble(v_string);
					type = VT2P;
				}
				
				Element now_e = null;
				if(f_type != 1)
				{
					if(tar_name.length() == 0)
					{
						Toast.makeText(MainActivity.this, 
								"没有填写物质的名称",
								Toast.LENGTH_LONG).show();
						return;
					}	
					now_e = search(tar_name);
					if(now_e==null)
					{
						Toast.makeText(MainActivity.this, 
								tar_name+" 这种物质没有找到",
								Toast.LENGTH_LONG).show();
						return;
					}
				}
				Solution ans = solve(now_e, arg, t, type, f_type);
				ShowAnswer(now_e, arg, t, type, f_type, ans); //show all info
			}
		});
		
		choose_spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				
				f_type = ( (Formula)parent.getItemAtPosition(position) ) . num;	
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				f_type = -1;
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
