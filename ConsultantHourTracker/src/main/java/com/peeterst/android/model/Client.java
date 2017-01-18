package com.peeterst.android.model;

import com.peeterst.android.data.persist.AbstractEntity;
import com.peeterst.android.data.persist.Field;
import com.peeterst.android.data.persist.Persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.peeterst.android.data.persist.SQLiteFieldType.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 28/05/12
 * Time: 16:44
 * Main client model class
 */
@Persistent(database = "HourTracker",defaultTable = "Clients")
public class Client extends AbstractEntity implements Serializable {

    @Field(name = "_id",type = INTEGER)
    private int id;

    @Field(name = "MonthlyHour",type = INTEGER)
    private int monthlyHour;

    @Field(name = "ClientName", type = TEXT)
    private String name;

    @Field(name = "NoonOffset", type = INTEGER)
    private int noonOffset;

    @Field(name = "Active", type = BOOLEAN)
    private boolean active = false;

    @Field(name= "Start", type = TEXT)
    private String dailyStartTime;

    @Field(name = "end", type = TEXT)
    private String dailyEndTime;

    @Field(name = "WorkDays", type = AGGREGATE, joinTable = "Workdays", aggregatedClass = WorkDay.class,
    aggregatedField = "ClientId")
    private List<WorkDay> workDays;


    public int getMonthlyHour() {
        return monthlyHour;
    }

    public void setMonthlyHour(int monthlyHour) {
        this.monthlyHour = monthlyHour;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoonOffset() {
        return noonOffset;
    }

    public void setNoonOffset(int noonOffset) {
        this.noonOffset = noonOffset;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDailyStartTime() {
        return dailyStartTime;
    }

    public void setDailyStartTime(String dailyStartTime) {
        this.dailyStartTime = dailyStartTime;
    }

    public String getDailyEndTime() {
        return dailyEndTime;
    }

    public void setDailyEndTime(String dailyEndTime) {
        this.dailyEndTime = dailyEndTime;
    }

    public List<WorkDay> getWorkDays() {
        return workDays;
    }

    public void setWorkDays(List<WorkDay> workDays) {
        this.workDays = workDays;
    }

    public void addWorkDay(WorkDay workDay){
        if(this.workDays == null){
            workDays = new ArrayList<WorkDay>();
        }
        workDays.add(workDay);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
