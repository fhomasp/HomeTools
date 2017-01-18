package com.peeterst.android.model;

import com.peeterst.android.data.persist.AbstractEntity;
import com.peeterst.android.data.persist.Field;
import com.peeterst.android.data.persist.SQLiteFieldType;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 23/01/11
 * Time: 16:13
 * Base class for using datestamp as unique identifier for saving on the filesystem.
 */
public abstract class Persistable extends AbstractEntity {

    @Field(name = "_id",type = SQLiteFieldType.LONG)
    protected long creationDatestamp;

    public long getCreationDatestamp() {
        return creationDatestamp;
    }

    public void setCreationDatestamp(long creationDatestamp) {
        this.creationDatestamp = creationDatestamp;
    }

    public boolean isFieldEqual(Object field1, Object field2){
        if(field1 == null){
            return field2 == null;
        }else {
            if(field1.equals(field2)) return true;
        }
        return false;

    }

}
