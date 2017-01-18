package com.peeterst.android.data.persist;

import android.database.sqlite.SQLiteCursor;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.peeterst.android.data.persist.FieldModifier.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 28/05/12
 * Time: 17:17
 * Represents a field in db
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
    String name();
    SQLiteFieldType type();
    String joinTable() default "";
    FieldModifier onDelete() default NONE;
    boolean foreignKey() default false;
    String aggregatedField() default "";
    Class<?> aggregatedClass() default Object.class;
}
