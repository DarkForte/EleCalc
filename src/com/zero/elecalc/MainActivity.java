package com.zero.elecalc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.zero.client.Place;

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
	
	void ReadFile()
	{
		InputStream inputstream = getResources().openRawResource(R.raw.data);
		Scanner cin = new Scanner(inputstream);
		
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
		
		final int PT2V=1, VT2P=2;
		
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
				
				if(t_string == null && f_type != 1)
				{
					Toast.makeText(MainActivity.this, 
							"T必须要填写",
							Toast.LENGTH_LONG).show();
					return;
				}
				if(p_string == null && v_string == null)
				{
					Toast.makeText(MainActivity.this, 
							"P和V不能全空着",
							Toast.LENGTH_LONG).show();
					return;
				}
				if(p_string != null && v_string != null)
				{
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
				if(v_string == null)
				{
					arg = Double.parseDouble(p_string);
					type = PT2V;
				}
				else
				{
					arg = Double.parseDouble(v_string);
					type =VT2P;
				}
				
				Element now_e = search(tar_name);
				if(now_e==null)
				{
					Toast.makeText(MainActivity.this, 
							tar_name+" 这种物质没有找到",
							Toast.LENGTH_LONG).show();
					return;
				}
				
				double ans = solve(now_e, arg, type, f_type);
				ShowAnswer(now_e, arg, type, f_type, ans);
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
