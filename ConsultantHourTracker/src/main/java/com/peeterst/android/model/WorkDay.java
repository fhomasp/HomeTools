package com.peeterst.android.model;

import com.peeterst.android.data.persist.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.peeterst.android.data.persist.SQLiteFieldType.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 3/06/12
 * Time: 22:23
 * model of a workday entry in the DB
 */

@Persistent(database = "HourTracker",defaultTable = "Workdays")
public class WorkDay extends AbstractEntity implements Serializable {

    @Field(name = "_id", type = INTEGER)
    private int id;

    @Field(name = "ClientId", type = INTEGER, joinTable = "Clients", foreignKey = true, onDelete = FieldModifier.CASCADE)
    private int clientId;

    @Field(name = "Entries", type = AGGREGATE, joinTable = "DayEntries", aggregatedField = "WorkdayID",
            aggregatedClass = DayEntry.class)
    private List<DayEntry> dayEntries= new ArrayList<DayEntry>();

    public WorkDay(){
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public List<DayEntry> getDayEntries() {
        return dayEntries;
    }

    public void setDayEntries(List<DayEntry> dayEntries) {
        this.dayEntries = dayEntries;
    }

    public void toggleProgress(){
        DayEntry dayEntry;
        if(dayEntries.size() % 2 == 0){
            dayEntry = new DayEntry();
            dayEntry.setStart(new Date().getTime());
            dayEntries.add(dayEntry);
        }else if(dayEntries.size() > 0) {
            dayEntry = dayEntries.get(dayEntries.size()-1);
            dayEntry.setEnd(new Date().getTime());
        }else {
            dayEntry = new DayEntry();
            dayEntry.setStart(new Date().getTime());
            dayEntries.add(dayEntry);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
