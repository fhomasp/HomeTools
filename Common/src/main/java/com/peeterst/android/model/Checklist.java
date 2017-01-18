package com.peeterst.android.model;

import com.peeterst.android.data.persist.Field;
import com.peeterst.android.data.persist.Persistent;
import com.peeterst.android.data.persist.SQLiteFieldType;

import java.util.*;

import static com.peeterst.android.data.persist.SQLiteFieldType.AGGREGATE;
import static com.peeterst.android.data.persist.SQLiteFieldType.BOOLEAN;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 27/12/10
 * Time: 13:54
 * This is the model class
 */
@Persistent(database = "ChecklistManager",defaultTable = "Checklist")
public class Checklist extends Persistable implements ChangeAware<Checklist>{

    public static final String KEY_TITLE = "title";
    public static final String KEY_CREATIONDATE = "creationDate";
    public static final String KEY_TARGETDATE = "targetDate";

    @Field(name = "title",type = SQLiteFieldType.TEXT)
    private String title;

    @Field(name = "targetDatestamp",type = SQLiteFieldType.LONG)
    private long targetDatestamp;

    @Field(name = "checklistItems", type = AGGREGATE, joinTable = "ChecklistItem", aggregatedClass = ChecklistItem.class,
            aggregatedField = "checklistId")
    private List<ChecklistItem> items;

    @Field(name = "checked", type = BOOLEAN)
    private boolean checked;

    private CalendarItem calendarItem;


    //needed for the Persistable stuff
    public Checklist() {
    }

    public Checklist(String title, long targetDatestamp) {
        super.creationDatestamp = new Date().getTime();
        this.title = title;
        this.targetDatestamp = targetDatestamp;
        items = new ArrayList<ChecklistItem>();
        checked = true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if(this.calendarItem != null){
            calendarItem.setTitle(title);
            calendarItem.setDirty(true);
        }
    }

    public long getCreationDatestamp() {
        return creationDatestamp;
    }

    public List<ChecklistItem> getItems() {
        if(items == null){
            items = new ArrayList<ChecklistItem>();
        }
        return items;
    }

    public void setItems(List<ChecklistItem> items) {
        this.items = items;
    }

    public long getTargetDatestamp() {
        return targetDatestamp;
    }

    public void setTargetDatestamp(long targetDatestamp) {
        this.targetDatestamp = targetDatestamp;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void addItem(ChecklistItem item){
        item.setChecklistId(this.creationDatestamp);
        this.items.add(item);
    }

    //todo: if louch change
    public boolean removeItem(ChecklistItem item){
//        item.setChecklistId(0);
        if(this.items.contains(item)){
            this.items.remove(item);
            return true;
        }else {
            Iterator<ChecklistItem> itemIterator = items.iterator();
            while(itemIterator.hasNext()){
                ChecklistItem target = itemIterator.next();
                if(target.getCreationDatestamp() == item.getCreationDatestamp()){
                    itemIterator.remove();
                    System.out.println("Item removed using its creationdatestamp: "+
                            target.getBulletName());
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public String toString() {
        return "Checklist{" +
                "title='" + title + '\'' +
                ", creationDatestamp=" + creationDatestamp +
                ", targetDatestamp=" + targetDatestamp +
                ", items=" + items +
                ", checked=" + checked +
                '}';
    }

    /**
     * CAUTION!!!
     * @param creationDatestamp
     */
    public void setCreationDatestamp(long creationDatestamp) {
        this.creationDatestamp = creationDatestamp;
    }

    public CalendarItem getCalendarItem() {
        return calendarItem;
    }

    public void setCalendarItem(CalendarItem calendarItem) {
        this.calendarItem = calendarItem;
        if(calendarItem != null) calendarItem.setDirty(true);
    }

    public boolean hasChangedFields(Checklist checklist) {
        if(this == checklist) {
            return false;
        }
        if(checklist == null){
            return true;
        }

        if(!isFieldEqual(this.getTitle(),checklist.getTitle())){
            return true;
        }
        if(!isFieldEqual(this.getTargetDatestamp(),checklist.getTargetDatestamp())){
            return true;
        }
        if(this.getCalendarItem() != null ){
            if(this.getCalendarItem().hasChangedFields(checklist.getCalendarItem())){
               return true;
            }

        }else if(this.getCalendarItem() == null && checklist.getCalendarItem() != null){
            return true;
        }

        if(this.getItems() != null){
            if(checklist.getItems() == null){
                return true;
            }else {
                if(this.getItems().size() != checklist.getItems().size()){
                    return true;
                }else {
                    for(int i = 0; i < this.getItems().size(); i++){
                        ChecklistItem local = getItems().get(i);
                        ChecklistItem target = checklist.getItems().get(i);
                        if(local.hasChangedFields(target)){
                            return true;
                        }
                    }
                }
            }
        }


        return false;
    }

    public void updateCalendarItemToChecklistFields(){
        CalendarItem calendarItem = this.getCalendarItem();  //is a debug hook
        if(calendarItem != null){
            if(!calendarItem.getDescription().equals(String.valueOf(this.getCreationDatestamp()))) {
                calendarItem.setDescription(String.valueOf(this.getCreationDatestamp()));
            }

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(this.getTargetDatestamp()));
            if(!calendarItem.getStartDate().equals(calendar.getTime())) {
                calendarItem.setStartDate(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendarItem.setEndDate(calendar.getTime());
            }

        }
    }

    @Override
    public Object clone() {
        try{
            Checklist checklist = new Checklist();
            checklist.setCreationDatestamp(this.getCreationDatestamp());
            checklist.setCalendarItem(this.calendarItem);
            checklist.setChecked(this.checked);
            checklist.setTargetDatestamp(this.targetDatestamp);
            checklist.setTitle(this.title);

            List<ChecklistItem> copiedItems = new ArrayList<ChecklistItem>();
            if(items != null) {
                for (ChecklistItem item : items) {
                    copiedItems.add((ChecklistItem) item.clone());
                }
            }
            checklist.setItems(copiedItems);
            return checklist;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
