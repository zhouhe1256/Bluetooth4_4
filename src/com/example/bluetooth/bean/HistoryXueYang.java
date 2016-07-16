package com.example.bluetooth.bean;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;
@Table (name ="xueyang")
public class HistoryXueYang {

 private double xueyang;
private String id ;
private String time ;
public double getXueyang() {
	return xueyang;
}
public void setXueyang(double xueyang) {
	this.xueyang = xueyang;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getTime() {
	return time;
}
public void setTime(String time) {
	this.time = time;
}

}
