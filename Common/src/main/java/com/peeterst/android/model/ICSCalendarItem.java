package com.peeterst.android.model;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 26/03/12
 * Time: 21:53
 * To use fields for ICS calendar Events
 */
public class ICSCalendarItem extends CalendarItem {


    protected String ID ="_id";
    protected String CALENDAR_ID = "calendar_id";
    protected String TITLE ="title";
    protected String DESCR = "description";
    protected String START_DATE = "dtstart";
    protected String END_DATE = "dtend";
    protected String EVENT_TIMEZONE = "eventTimezone";

    private String eventTimezone;

    public String getEventTimezone() {
        return eventTimezone;
    }

    public void setEventTimezone(String eventTimezone) {
        this.eventTimezone = eventTimezone;
    }
}
