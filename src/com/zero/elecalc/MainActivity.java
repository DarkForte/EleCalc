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
	TextView mat_text, tc_text, pc_text, omega_text;
	EditText name_box, t_box, p_box, v_box, tc_box, pc_box, omega_box;
	Button mode_btn;
	Spinner choose_spinner;
	Button go_button;
	TextView ans_area;
	
	List<Element> ele_list;
	List<Formula> formula_list;
	int f_type;
	ArrayAdapter<Formula> adapter;
	
	boolean manual=false;
	
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
	
	Solution solve_3_equation(double a, double b, double c, double d) //solve cube equation
	{
		
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
			
			//math.pow cannot calc a^(1/3) if a<0  
			double tmp_y1 = Math.abs(y1);
			double tmp_y2 = Math.abs(y2);
			double t1 = Math.pow(tmp_y1, 1.0/3.0);
			double t2 = Math.pow(tmp_y2, 1.0/3.0);
			if(y1<0)
				t1=-t1;
			if(y2<0)
				t2=-t2;
			
			double ans = (-b-t1-t2)/(3*a);
			return new Solution(1, ans);
		}
		else if(delta == 0)
		{
			double k = B/A;
			double x[] = new double[5];
			x[1] = -b/a+k;
			x[2] = -k/2;	
			return new Solution(2, x);
		}
		else
		{
			double T= (2*A*b - 3*a*B) / (2*Math.sqrt(A*A*A) );
			double ceta = Math.acos(T);
			double x[] = new double[5];
			x[1] = (-b - 2*Math.sqrt(A) * Math.cos(ceta/3) ) / (3*a);
			x[2] = (-b + Math.sqrt(A)* ( Math.cos(ceta/3)+Math.sqrt(3.0)*Math.sin(ceta/3) ) )/(3*a);
			x[3] = (-b + Math.sqrt(A)* ( Math.cos(ceta/3)-Math.sqrt(3.0)*Math.sin(ceta/3) ) )/(3*a);
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
			double b = (r * now_e.tc) / (8 * now_e.pc);
			//System.out.println("a: "+a+" b: "+b);
			
			
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
				double A = p;
				double B = -(p*b + r*t);
				double C = a;
				double D = -a*b;
				
				System.out.println("a " + A +" b " + B + " c "+ C +" d " + D);
				Solution ans = solve_3_equation(A,B,C,D);
				return ans;
			}
		}
		else if(f_type == 3)
		{
			double a = 0.42748*(r * r * Math.pow(now_e.tc, 2.5));
			a/=now_e.pc;
			double b = 0.08664*(r * now_e.tc) / now_e.pc;
			System.out.println(a+ " " + b);
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
				double B = -r*t;
				double C = -p * b * b - b * r * t + a / Math.sqrt(t);
				double D = -a*b / Math.sqrt(t);

				Solution ans = solve_3_equation(A, B, C, D);
				return ans;
			}
		}
		else if(f_type == 4)
		{
			double m = 0.48 + 1.574 * now_e.omega - (0.176 * now_e.omega * now_e.omega);
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
				double B = -r*t;
				double C = -p * b * b - b * r * t + a;
				double D = -a * b;
				System.out.println("a" + A +" b " + B + " c "+ C +" d " + D);
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
		String s_type = (type == VT2P) ? "v="+arg+"cm3/mol" : "p=" + arg +"MPa";
		String a_type = (type == VT2P) ? "p="+ans.toString()+"Mpa" : "v=" + ans.toString()+"cm3/mol" ;
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
		
		mat_text = (TextView)findViewById(R.id.materialTextID);
		tc_box = (EditText)findViewById(R.id.tcID);
		tc_text = (TextView)findViewById(R.id.tcTextID);
		pc_box = (EditText)findViewById(R.id.pcID);
		pc_text = (TextView)findViewById(R.id.pcTextID);
		omega_box = (EditText)findViewById(R.id.omegaID);
		omega_text = (TextView)findViewById(R.id.omegaTextID);
		mode_btn = (Button)findViewById(R.id.modeID);
		
		ele_list = new ArrayList<Element>();
		formula_list = new ArrayList<Formula>();
		
		ReadFile();
		InitFormula();
		
		go_button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				String tar_name="", tc_string="", pc_string="", omega_string="";
				if(!manual)
				{
					tar_name = name_box.getText().toString();
				}
				else 
				{
					tc_string = tc_box.getText().toString();
					pc_string = pc_box.getText().toString();
					omega_string = omega_box.getText().toString();
				}
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
				if(manual == true && ( pc_string.length()==0 || tc_string.length()==0))
				{
					Toast.makeText(MainActivity.this,
							"Tc, Pc没有填写全",
							Toast.LENGTH_LONG).show();
					return;
				}
				if(manual == true && omega_string.length() ==0)
				{
					if(f_type >= 4)
					{	Toast.makeText(MainActivity.this,
								"这个方程必须填写omega",
								Toast.LENGTH_LONG).show();
						return;
					}
					else 
					{	
						omega_string = "0";
					}
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
				if(!manual)
				{
					if(f_type != 1)
					{
						if(tar_name.length() == 0)
						{
							Toast.makeText(MainActivity.this, 
									"没有填写物质的名称",
									Toast.LENGTH_LONG).show();
							return;
						}	
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
				else
				{
					double pc = Double.parseDouble(pc_string);
					double tc = Double.parseDouble(tc_string);
					double omega = Double.parseDouble(omega_string);
					now_e = new Element("新物质", tc, pc, omega);
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
		
		mode_btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) 
			{
				if(!manual)
				{
					mat_text.setText("请手动输入新物质的信息");
					name_box.setVisibility(View.GONE);
					tc_text.setVisibility(View.VISIBLE);
					tc_box.setVisibility(View.VISIBLE);
					pc_text.setVisibility(View.VISIBLE);
					pc_box.setVisibility(View.VISIBLE);
					omega_text.setVisibility(View.VISIBLE);
					omega_box.setVisibility(View.VISIBLE);
					mode_btn.setText("检索已有物质");
					manual = true;
				}
				else 
				{
					mat_text.setText("物质的名称：（中文，理想气体方程可不写）");
					tc_text.setVisibility(View.GONE);
					tc_box.setVisibility(View.GONE);
					pc_text.setVisibility(View.GONE);
					pc_box.setVisibility(View.GONE);
					omega_text.setVisibility(View.GONE);
					omega_box.setVisibility(View.GONE);
					name_box.setVisibility(View.VISIBLE);
					mode_btn.setText("手动输入新物质");
					manual = false;
				}
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
