package com.peeterst.android.data.persist;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 5/06/12
 * Time: 22:44
 *
 */
public abstract class AbstractEntity implements Cloneable {
    @Override
    public abstract Object clone();
}
