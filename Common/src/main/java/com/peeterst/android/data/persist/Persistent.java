package com.peeterst.android.data.persist;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 28/05/12
 * Time: 17:12
 * main persistence annotation to define DB and ...
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Persistent {
    String defaultTable();
    String database();
}
