package com.example.bluetooth.bean;


import net.tsz.afinal.annotation.sqlite.Table;
@Table (name ="tiwen")
public class HistoryTiWen {

 private double tiwen;
private String id ;
private String time ;

public double getTiwen() {
	return tiwen;
}
public void setTiwen(double tiwen) {
	this.tiwen = tiwen;
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
