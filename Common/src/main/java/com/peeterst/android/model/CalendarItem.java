package com.peeterst.android.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 5/10/11
 * Time: 13:31
 *
 */
public class CalendarItem implements ChangeAware<CalendarItem>{
    private int id;
    private int calendarId;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private boolean dirty = true;

    protected String ID ="_id";
    protected String CALENDAR_ID = "calendar_id";
    protected String TITLE ="title";
    protected String DESCR = "description";
    protected String START_DATE = "dtstart";
    protected String END_DATE = "dtend";
    protected String HAS_ALARM = "hasAlarm";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(int calendarId) {
        this.calendarId = calendarId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

//    public enum Fields {
//        ID("_id"),
//        CALENDAR_ID("calendar_id"),
//        TITLE("title"),
//        DESCR("description"),
//        START_DATE("dtstart"),
//        END_DATE("dtend");
//
//        public String fieldName;
//
//        Fields(String fieldValue){
//            this.fieldName = fieldValue;
//        }
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalendarItem)) return false;

        CalendarItem that = (CalendarItem) o;

        if (calendarId != that.calendarId) return false;
        if (!description.equals(that.description)) return false;
        if (!endDate.equals(that.endDate)) return false;
        if (!startDate.equals(that.startDate)) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;

        return true;
    }

    private boolean isFieldEqual(Object field1, Object field2){
        if(field1 == null){
            return field2 == null;
        }else {
            return field1.equals(field2);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean hasChangedFields(CalendarItem compare){
        if(compare == null) return true;
        if (calendarId != compare.calendarId) return true;
        if(!isFieldEqual(description,compare.getDescription())) return true;
        if(!isFieldEqual(startDate,compare.getStartDate())) return true;
        if(!isFieldEqual(endDate, compare.getEndDate())) return true;
        if(!isFieldEqual(title, compare.getTitle())) return true;

        return false;
    }

    @Override
    public int hashCode() {
        int result = calendarId;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + description.hashCode();
        result = 31 * result + startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        return result;
    }

    public String getID() {
        return ID;
    }

    public String getCALENDAR_ID() {
        return CALENDAR_ID;
    }

    public String getTITLE() {
        return TITLE;
    }

    public String getDESCR() {
        return DESCR;
    }

    public String getSTART_DATE() {
        return START_DATE;
    }

    public String getEND_DATE() {
        return END_DATE;
    }

    public String getHAS_ALARM() {
        return HAS_ALARM;
    }
}
