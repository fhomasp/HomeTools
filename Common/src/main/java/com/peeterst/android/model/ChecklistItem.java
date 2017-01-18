package com.peeterst.android.model;

import com.peeterst.android.data.persist.Field;
import com.peeterst.android.data.persist.FieldModifier;
import com.peeterst.android.data.persist.Persistent;

import java.util.Date;

import static com.peeterst.android.data.persist.SQLiteFieldType.*;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 28/12/10
 * Time: 11:36
 *
 */
@Persistent(database = "ChecklistManager",defaultTable = "ChecklistItem")
public class ChecklistItem extends Persistable implements ChangeAware<ChecklistItem>{

    @Field(name = "bulletName",type = TEXT)
    private String bulletName;

    @Field(name = "taken",type = BOOLEAN)
    private boolean taken;

    @Field(name = "checklistId", type = LONG, joinTable = "Checklist", foreignKey = true, onDelete = FieldModifier.CASCADE)
    private long checklistId;

    public ChecklistItem(String bullet){
        this.bulletName = bullet;
        this.taken = false;
        super.creationDatestamp = new Date().getTime();
    }

    public ChecklistItem() {
    }

    public String getBulletName() {
        return bulletName;
    }

    public void setBulletName(String bulletName) {
        this.bulletName = bulletName;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
//        System.out.println(bulletName+" --> taken set: "+taken);
        this.taken = taken;
    }

    public long getChecklistId() {
        return checklistId;
    }

    public void setChecklistId(long checklistId) {
        this.checklistId = checklistId;
    }

    public boolean hasChangedFields(ChecklistItem checklistItem) {

        return !isFieldEqual(this.getBulletName(), checklistItem.getBulletName()) || !isFieldEqual(this.taken, checklistItem.isTaken());

    }

    @Override
    public Object clone() {
        ChecklistItem copy = new ChecklistItem();
        copy.setChecklistId(this.checklistId);
        copy.setBulletName(this.bulletName);
        copy.setTaken(this.taken);
        copy.setCreationDatestamp(this.creationDatestamp);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(": ").append(creationDatestamp).append(" // ").append(bulletName).append(" //ChecklistId: ").append(checklistId);
        return sb.toString();
    }
}
