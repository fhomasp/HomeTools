package com.peeterst.android.data.adapter;

import android.content.ContentValues;
import android.content.Context;
import com.peeterst.android.data.persist.Field;
import com.peeterst.android.data.persist.SQLiteFieldType;
import com.peeterst.android.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10/11/12
 * Time: 16:53
 * TODO: Aggregates in aggegrates is broken ATM
 */
public class AggregatedSQLiteAdapter <T> extends SQLiteAdapter<T> {

    private SQLiteAdapter parent;

//    public AggregatedSQLiteAdapter(Context c, Class<T> persistentClass) {
//        super(c, persistentClass);
//    }

    public AggregatedSQLiteAdapter(Context c, Class<T> persistentClass, SQLiteAdapter parent, boolean cacheful) {
        super(c, persistentClass, cacheful);
        this.parent = parent;
    }

    @Override
    public List<T> findInCacheByField(Object value, Field field) throws NoSuchFieldException, IllegalAccessException {

//        List<Field> aggregateParentFields = new ArrayList<Field>();
//
//        for(Field parentField:parent.getAnnotatedFields().values()){
//            if(parentField.type().equals(SQLiteFieldType.AGGREGATE)){
//                aggregateParentFields.add(parentField);
//            }
//        }

        List<T> matches = new ArrayList<T>(40);

        for(Object main:parent.getCache()){

            for(String javaField:(Set<String>)parent.getAnnotatedFields().keySet()){
                //            java.lang.reflect.Field rField = persistentClass.getField(javaField);
                Object parentValue = ReflectionUtil.getFieldValue(main, javaField);
                Field dbField = (Field) parent.getAnnotatedFields().get(javaField);
                if(dbField.type().equals(SQLiteFieldType.AGGREGATE)){
                    if(parentValue != null && parentValue instanceof Collection<?>){
                        Collection bunch = (Collection) parentValue;
                        for (Object aggr : bunch) {
                            for(Field aggrField:getAnnotatedFields().values()){
                                if(aggrField.equals(field)){
                                    Object extracted = extractValue(aggr,aggrField);
                                    if(extracted != null && extracted.equals(value)){
                                        matches.add((T)aggr);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return matches;

    }

    /**
     * Intended for aggregates
     * @param insert
     * @param bunch
     */
    protected boolean insertOrUpdateBatch(boolean insert,Collection<T> bunch,boolean transactionExists){

        clearBrolPool();
        boolean changed = false;
        try{
            if(!transactionExists){
                openToWrite();
                sqLiteDatabase.execSQL("PRAGMA synchronous=OFF");   //todo: source transactions given from caller
                sqLiteDatabase.setLockingEnabled(false);
                sqLiteDatabase.beginTransaction();
            }
            for(T t:bunch){
                if(insert){
                    ContentValues contentValues = populateContent(t);

                    long result = sqLiteDatabase.insertOrThrow(persistent.defaultTable(), null, contentValues);
                    changed = true;
                    //todo @ insert & update: handle nested aggregation
                }else{
                    Long id = (Long)extractValue(t,idField);
                    T cached = findInCacheById(id);
                    if(cached != null) {
                        if (cacheComparedChange(t)) {
                            ContentValues contentValues = populateContent(t);
                            String where = "_id = " + extractValue(t, idField);
                            int result = sqLiteDatabase.update(persistent.defaultTable(), contentValues, where, null);
                            //refresh cache ...
//                            sqdf
                            if (result == 0) {
                                //TODO: dangerous: but used for insert/update unknown problem.
                                sqLiteDatabase.insertOrThrow(persistent.defaultTable(), null, contentValues);
                            }
                            changed = true;
                        }
                    }else{
                        ContentValues contentValues = populateContent(t);

                        long result = sqLiteDatabase.insertOrThrow(persistent.defaultTable(), null, contentValues);
                        changed = true;
                    }
                }
            }

            if(!transactionExists) {
                sqLiteDatabase.setTransactionSuccessful();
            }

        }catch (Exception e){

            throw new RuntimeException(e);

        }finally {
            if(!transactionExists){
                sqLiteDatabase.endTransaction();
                sqLiteDatabase.setLockingEnabled(true);
                sqLiteDatabase.execSQL("PRAGMA synchronous=ON");
                close();
            }
        }
        return changed;
    }

    protected boolean cacheComparedChange(Object entity) throws NoSuchFieldException, IllegalAccessException {
        T generified = (T) entity;


        Long id = (Long)extractValue(generified,idField);
        T cached = findInCacheById(id);

        if(cached == null && entity == null){
            throw new IllegalStateException("Cache object and entity object is null during aggregated update");
        }else if(cached == null){
            throw new IllegalStateException("Cache object is null during aggregated update: "+ entity.toString());
        }else if(entity == null){
            throw new IllegalStateException("Entity object is null during aggregated update: "+cached.toString());
        }

        boolean changed = false;
        for(Field field: getAnnotatedFields().values()){
            if(field.type() != SQLiteFieldType.AGGREGATE){
                Object cachedMember = extractValue(cached,field);
                Object entityMember = extractValue(generified,field);

                if(cachedMember == null && entityMember == null){

                }else if(cachedMember == null && entityMember != null){
                    return true;
                }else if(cachedMember != null && entityMember == null){
                    return true;
                }else if(cachedMember != null && entityMember != null){
                    if(!cachedMember.equals(entityMember)){
                        return true;
                    }
                }

            }
        }


        return false;
    }




}
