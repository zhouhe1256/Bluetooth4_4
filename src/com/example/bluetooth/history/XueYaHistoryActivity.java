package com.example.bluetooth.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.tsz.afinal.FinalDb;

import com.ab.activity.AbActivity;
import com.ab.view.chart.BarChart;
import com.ab.view.chart.CategorySeries;
import com.ab.view.chart.ChartFactory;
import com.ab.view.chart.PointStyle;
import com.ab.view.chart.XYMultipleSeriesDataset;
import com.ab.view.chart.XYMultipleSeriesRenderer;
import com.ab.view.chart.XYSeriesRenderer;
import com.example.bluetooth.bean.HistoryTiWen;
import com.example.bluetooth.bean.HistoryXueYa;
import com.example.bluetooth.bean.HistoryXueYang;
import com.example.bluetooth.le.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class XueYaHistoryActivity extends AbActivity {
	
	
	  private  List<HistoryXueYa>  getData() {
			FinalDb db = FinalDb.create(XueYaHistoryActivity.this);
			 List<HistoryXueYa> history = db.findAll(HistoryXueYa.class);
			if(history.size() > 0){
				Collections.reverse(history);
				return history;
			}else {
				return null;
			}
			
		}
	  public void clear_notes(View v){
			//拿到FinalDb对象引用
		    FinalDb db = FinalDb.create(XueYaHistoryActivity.this);
		    db.deleteAll(HistoryXueYa.class);//删除Bean对应的数据表中的所有数据
		    Dialog dialog = new AlertDialog.Builder(this).setIcon(
		    	     android.R.drawable.btn_star).setTitle("删除成功").setMessage(
		    	     "已清除血压和脉率数据").setPositiveButton("确定",new OnClickListener() {
		    	 
		    	      @Override
		    	      public void onClick(DialogInterface dialog, int which) {
		    	    	  dialog.dismiss();
		    			  refresh();
		    	      }
		    	     }).create();
		    dialog.show();
		}
	  /** 
	   * 刷新 
	   */  
	  private void refresh() {  
	      finish();  
	      Intent intent = new Intent(XueYaHistoryActivity.this, XueYaHistoryActivity.class);  
	      startActivity(intent);  
	  }  
  @Override               
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setAbContentView(R.layout.chart);
      
     
      
  	//要显示图形的View
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chart01);
		//说明文字
		String[] titles = new String[] { "高压", "低压" };
		//数据
	    List<double[]> values = new ArrayList<double[]>();
	    //每个数据点的颜色
	    List<int[]> colors = new ArrayList<int[]>();
	    //每个数据点的简要 说明
	    List<String[]> explains = new ArrayList<String[]>();
	    List<HistoryXueYa> history = getData();
	   if (history == null || history.size() <0){
		   return ;
	   }
//	    List<HistoryXueYa> history = new ArrayList<HistoryXueYa>();
//	    for (int i = 0; i < 5; i++) {
//	    	HistoryXueYa historyXueYa = new HistoryXueYa();
//	    	historyXueYa.setHxueya(115);
//	    	historyXueYa.setLxueya(73);
//	    	history.add(historyXueYa);
//		}
	   int size=history.size() ; 
	   if(size > 30 ){
			size = 30;
		}else {
			size=history.size();
		}
	    
	    double[] d =new double[size];
	    double[] d1 = new double[size];
	    int[] a = new int[size] ;
	    int[] a1 = new int[size] ;
	    String[] c = new String[size] ;
	    String[] c1 = new String[size] ;
	    
	    		
	    for (int i = 0; i < size; i++) {
			d[i] =  history.get(i).getHxueya();                                      
			d1[i] = history.get(i).getLxueya();;	
			a[i] = Color.RED;
			a1[i] = Color.RED;
			c[i] = "";
			c1[i] = "";
		}
	 
	    values.add(d);
	    values.add(d1);
	    
	    colors.add(a);
	    colors.add(a1);
	    
	    explains.add(c);
	    explains.add(c1);
	    
	    //柱体或者线条颜色 
	    int[] mSeriescolors = new int[] { Color.rgb(47, 215, 90) ,Color.rgb(255, 202, 106)};
	    //创建渲染器
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    int length = mSeriescolors.length;
	    for (int i = 0; i < length; i++) {
	      //创建SimpleSeriesRenderer单一渲染器
	      XYSeriesRenderer r = new XYSeriesRenderer();
	      //SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	      //设置渲染器颜色
	      r.setColor(mSeriescolors[i]);
	      r.setFillPoints(true);
		  r.setPointStyle(PointStyle.CIRCLE);
		  r.setLineWidth(1);
		  r.setChartValuesTextSize(16);
	      //加入到集合中
	      renderer.addSeriesRenderer(r);
	    }
	    //点的大小
	    renderer.setPointSize(2f);
	    //坐标轴标题文字大小
		renderer.setAxisTitleTextSize(16);
		//图形标题文字大小
		renderer.setChartTitleTextSize(25);
		//轴线上标签文字大小
		renderer.setLabelsTextSize(15);
		//说明文字大小
		renderer.setLegendTextSize(15);
		//图表标题
	    renderer.setChartTitle("血压");
	    //X轴标题
//	    renderer.setXTitle("X轴");
	    //Y轴标题
//	    renderer.setYTitle("Y轴");
	    //X轴最小坐标点
	    renderer.setXAxisMin(0);
	    //X轴最大坐标点
	    renderer.setXAxisMax(31);
	    //Y轴最小坐标点
	    renderer.setYAxisMin(30);
	    //Y轴最大坐标点
	    renderer.setYAxisMax(180);
	    //坐标轴颜色
	    renderer.setAxesColor(Color.rgb(125, 125, 125));
	    renderer.setXLabelsColor(Color.rgb(125, 125, 125));
	    renderer.setYLabelsColor(0,Color.rgb(125, 125, 125));
	    //设置图表上标题与X轴与Y轴的说明文字颜色
	    renderer.setLabelsColor(Color.rgb(125, 125, 125));
	    //renderer.setGridColor(Color.GRAY);
	    //设置字体加粗
		renderer.setTextTypeface("sans_serif", Typeface.BOLD);
		//设置在图表上是否显示值标签
	    renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
	    renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
	    //显示屏幕可见取区的XY分割数
	    renderer.setXLabels(31);
	    renderer.setYLabels(10);
	    //X刻度标签相对X轴位置
	    renderer.setXLabelsAlign(Align.CENTER);
	    //Y刻度标签相对Y轴位置
	    renderer.setYLabelsAlign(Align.RIGHT);
	    
	    renderer.setPanEnabled(true, false);
	    renderer.setZoomEnabled(true);
	    renderer.setZoomButtonsVisible(true);
	    renderer.setZoomRate(10.0f);
	    renderer.setBarSpacing(0.0f);
	    //临界线
	    double[] limit = new double[]{140,90,90,60};
	    renderer.setmYLimitsLine(limit);
	    
	    int[] colorsLimit = new int[] { Color.rgb(255, 0,0),Color.rgb(255, 0,0),Color.rgb(0, 0,255), Color.rgb(0, 0,255) };
	    renderer.setmYLimitsLineColor(colorsLimit);
	  
//	    //标尺开启
//	    renderer.setScaleLineEnabled(true);
//	    //设置标尺提示框高
//	    renderer.setScaleRectHeight(60);
//	    //设置标尺提示框宽
//	    renderer.setScaleRectWidth(150);
//	    //设置标尺提示框背景色
//	    renderer.setScaleRectColor(Color.argb(150, 52, 182, 232));
//	    renderer.setScaleLineColor(Color.argb(175, 150, 150, 150));
//	    renderer.setScaleCircleRadius(35);
	    //第一行文字的大小
	    renderer.setExplainTextSize1(20);
	    //第二行文字的大小
	    renderer.setExplainTextSize2(20);
	    
	    
	    
	    //显示表格线
	    renderer.setShowGrid(true);
	    
	    //如果值是0是否要显示
	    renderer.setDisplayValue0(false);
	    //创建渲染器数据填充器
	    XYMultipleSeriesDataset mXYMultipleSeriesDataset = new XYMultipleSeriesDataset();
	    for (int i = 0; i < length; i++) {
	      CategorySeries series = new CategorySeries(titles[i]);
	      double[] v = values.get(i);
	      int[] c2 = colors.get(i);
	      String[] e = explains.get(i);
	      int seriesLength = v.length;
	      for (int k = 0; k < seriesLength; k++) {
	    	  //设置每个点的颜色
	          series.add(v[k],c2[k],e[k]);
	      }
	      mXYMultipleSeriesDataset.addSeries(series.toXYSeries());
	    }
	    //背景
	    renderer.setApplyBackgroundColor(true);
	    renderer.setBackgroundColor(Color.rgb(255, 255, 255));
	    renderer.setMarginsColor(Color.rgb(255, 255, 255));
	    
	    //线图
	    View chart = ChartFactory.getBarChartView(this,mXYMultipleSeriesDataset,renderer,BarChart.Type.DEFAULT);
      linearLayout.addView(chart);
		
    } 
  
}
