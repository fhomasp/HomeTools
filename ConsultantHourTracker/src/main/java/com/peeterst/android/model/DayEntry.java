package com.peeterst.android.model;

import com.peeterst.android.data.persist.*;

import java.io.Serializable;

import static com.peeterst.android.data.persist.FieldModifier.*;
import static com.peeterst.android.data.persist.SQLiteFieldType.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 3/06/12
 * Time: 22:44
 *
 */
@Persistent(database = "HourTracker",defaultTable = "DayEntries")
public class DayEntry extends AbstractEntity implements Serializable {

    @Field(name = "_id", type = INTEGER)
    private int id;

    @Field(name = "WorkdayID",type = INTEGER, joinTable = "Workdays", onDelete = CASCADE, foreignKey = true)
    private int workDayId;

    @Field(name = "Start", type = DATE)
    private long start;

    @Field(name = "End", type = DATE)
    private long end;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorkDayId() {
        return workDayId;
    }

    public void setWorkDayId(int workDayId) {
        this.workDayId = workDayId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
