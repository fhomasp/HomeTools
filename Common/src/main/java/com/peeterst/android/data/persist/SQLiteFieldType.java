package com.peeterst.android.data.persist;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 29/05/12
 * Time: 12:34
 * To change this template use File | Settings | File Templates.
 */
public enum SQLiteFieldType {
    NULL("NULL"),INTEGER("INTEGER"),REAL("REAL"),TEXT("TEXT"),BLOB("BLOB"),BOOLEAN("INTEGER"),DATE("INTEGER"),AGGREGATE("N/A"),LONG("LONG");

    private String fieldValue;

    SQLiteFieldType(String fieldValue){
        this.fieldValue = fieldValue;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
