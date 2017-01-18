package com.peeterst.android.model;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 9/10/11
 * Time: 14:54
 *
 */
public interface ChangeAware<T>{


    boolean hasChangedFields(T t);

}
