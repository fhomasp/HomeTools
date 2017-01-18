package com.peeterst.android.util;

import com.peeterst.android.model.ChangeAware;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 9/10/11
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class ChangeChecker<T extends ChangeAware> {

    public boolean hasChanged(T target){
        Class targetClass = target.getClass();

        Field[] fields = targetClass.getDeclaredFields();





        return false;
    }

}
