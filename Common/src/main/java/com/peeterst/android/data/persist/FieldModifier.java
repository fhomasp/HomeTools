package com.peeterst.android.data.persist;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 3/06/12
 * Time: 22:32
 * To model DB actions such as on delete cascade
 */
public enum FieldModifier {
    NONE,CASCADE,UPDATE,
    AGGREGATE
}
