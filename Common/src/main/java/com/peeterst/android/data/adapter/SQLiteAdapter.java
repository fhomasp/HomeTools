package com.peeterst.android.data.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.peeterst.android.data.persist.*;
import com.peeterst.android.util.ReflectionUtil;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * This class is getting awfully "iffy"
 * TODO: transactions
 * @param <T>
 */
public class SQLiteAdapter<T> extends GenericTypeAdapter<T> {

    public static final int MYDATABASE_VERSION = 1;
    public static final String SELECTION = "SELECTION";
    public static final String ARGS = "ARGS";
    public static final String format = "hh:mm:ss.SSS";
    public String id;

    public boolean cacheful = true;

    private List<T> cache;

    private SQLiteHelper sqLiteHelper;
    protected SQLiteDatabase sqLiteDatabase;

    private Context context;
    private final Map<String,Field> annotatedFields;
    protected final Persistent persistent;
    private final Class<T> persistentClass;
    protected Field idField = null;
    private AggregatedSQLiteAdapter aggregatedAdapter;
    private static List brolPool = new ArrayList(60);
    private SimpleDateFormat sdf;

    @SuppressWarnings("unchecked")
    public SQLiteAdapter(Context c,Class<T> persistentClass ){

        this.cache = new ArrayList<T>(60);
        this.persistentClass = persistentClass;

        persistent = persistentClass.getAnnotation(Persistent.class);
        java.lang.reflect.Field[] fields = persistentClass.getDeclaredFields();
        annotatedFields = new HashMap<String, Field>();

        boolean idFound = false;
        for(java.lang.reflect.Field field: fields){
            Field annotatedField = field.getAnnotation(Field.class);
            if(annotatedField == null){
                continue;
//                try {
//                    annotatedField = persistentClass.getSuperclass().getField(field.getName()).getAnnotation(Field.class);
//                } catch (NoSuchFieldException e) {
//                    throw new RuntimeException(e);
//                }
            }
            annotatedFields.put(field.getName(), annotatedField);
            if(annotatedField.name().equals("_id")){
                idFound = true;
                idField = annotatedField;
            }
            if(annotatedField.type().equals(SQLiteFieldType.AGGREGATE)){
                if(aggregatedAdapter == null){
                    aggregatedAdapter = new AggregatedSQLiteAdapter(c, annotatedField.aggregatedClass(),this,false);
                }
            }
        }

        boolean superId = addSuperFields(persistentClass);

        if(!idFound && !superId){
            throw new IllegalStateException("Required '_id' field not found");
        }

        context = c;

        openToWrite();
        sqLiteHelper.onCreate(sqLiteDatabase);
        close();

    }


    public SQLiteAdapter(Context c,Class<T> persistentClass,boolean cacheful ){
        this.cacheful = cacheful;
        if(cacheful) {
            this.cache = new ArrayList<T>(60);
        }
        this.persistentClass = persistentClass;

        persistent = persistentClass.getAnnotation(Persistent.class);
        java.lang.reflect.Field[] fields = persistentClass.getDeclaredFields();
        annotatedFields = new HashMap<String, Field>();

        boolean idFound = false;
        for(java.lang.reflect.Field field: fields){
            Field annotatedField = field.getAnnotation(Field.class);
            if(annotatedField == null){
                continue;
//                try {
//                    annotatedField = persistentClass.getSuperclass().getField(field.getName()).getAnnotation(Field.class);
//                } catch (NoSuchFieldException e) {
//                    throw new RuntimeException(e);
//                }
            }
            annotatedFields.put(field.getName(), annotatedField);
            if(annotatedField.name().equals("_id")){
                idFound = true;
                idField = annotatedField;
            }
            if(annotatedField.type().equals(SQLiteFieldType.AGGREGATE)){
                if(aggregatedAdapter == null){
                    aggregatedAdapter = new AggregatedSQLiteAdapter(c, annotatedField.aggregatedClass(),this,false);
                }
            }
        }

        boolean superId = addSuperFields(persistentClass);

        if(!idFound && !superId){
            throw new IllegalStateException("Required '_id' field not found");
        }

        context = c;

        openToWrite();
        sqLiteHelper.onCreate(sqLiteDatabase);
        close();

    }

    public List<T> getCache(){
        return cache;
    }


    /**
     *
     * @return true if "_id" was found
     */
    private boolean addSuperFields(Class clazz){

        Class sClass = clazz.getSuperclass();
        if(sClass != null){
            java.lang.reflect.Field[] fields = sClass.getDeclaredFields();
            boolean idFound = false;
            for(java.lang.reflect.Field field: fields){
                Field annotatedField = field.getAnnotation(Field.class);
                if(annotatedField == null){
                    continue; //only annotated fields
                }
                annotatedFields.put(field.getName(), annotatedField);
                if(annotatedField.name().equals("_id")){
                    idFound = true;
                    idField = annotatedField;
                }
            }
            boolean superId = false;
            superId = addSuperFields(sClass);
            if(idFound == true && superId == true){
                throw new IllegalStateException("Multiple '_id' fields found in class hierarchy");
            }else if(idFound != true && superId == true){
                return superId;
            }else if(idFound && !superId){
                return idFound;
            }
            return idFound;

        }else {
            return false;
        }
    }

    public SQLiteAdapter openToRead() throws android.database.SQLException {
        if(sqLiteHelper == null) {
            sqLiteHelper = new SQLiteHelper(context, persistent.database(), null, MYDATABASE_VERSION);
        }
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public SQLiteAdapter openToWrite() throws android.database.SQLException {
        if(this.sqLiteHelper == null) {
            sqLiteHelper = new SQLiteHelper(context, persistent.database(), null, MYDATABASE_VERSION);
        }
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        sqLiteHelper.close();
    }

    public void persist(List<T> entities){

        openToWrite();

        sqLiteDatabase.execSQL("PRAGMA synchronous=OFF");   //todo: source transactions given from caller
        sqLiteDatabase.setLockingEnabled(false);
        sqLiteDatabase.beginTransaction();

        try{

            for(T t:entities){

                ContentValues contentValues = populateContent(t);

                if(findInCacheById((Long) extractValue(t, idField)) == null){

                    long result = sqLiteDatabase.insert(persistent.defaultTable(), null, contentValues);
                    copyAndCache(t);
                    handleAggregation(t,result,true,true);
                }else {
                    long result = sqLiteDatabase.update(persistent.defaultTable(), contentValues, null, null);
                    copyAndCache(t);
                    handleAggregation(t,result,false,true);
                }


            }

            sqLiteDatabase.setTransactionSuccessful();

        }catch(Exception e){

            throw new RuntimeException(e);

        }

        finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.setLockingEnabled(true);
            sqLiteDatabase.execSQL("PRAGMA synchronous=ON");
            close();
        }


    }

    public long insert(T entity){
        if(sdf == null) {
            sdf = new SimpleDateFormat(format);
        }
        System.err.println("Starting insert..." + sdf.format(new Date()));
        openToWrite();
        ContentValues contentValues = populateContent(entity);
        long result = 0;
        try{
//            sqLiteDatabase.execSQL("PRAGMA synchronous=OFF");   //todo: source transactions given from caller
//            sqLiteDatabase.setLockingEnabled(false);
//            sqLiteDatabase.beginTransaction();

        System.err.println("inserting..." + sdf.format(new Date()));
        result = sqLiteDatabase.insertOrThrow(persistent.defaultTable(), null, contentValues);
        //todo: result returns nr rows?  dangerous if not error when _id is unknown and handled by the DB


        }catch(Exception e){
            throw new RuntimeException(e);
        }finally {

//            sqLiteDatabase.endTransaction();
//            sqLiteDatabase.setLockingEnabled(true);
//            sqLiteDatabase.execSQL("PRAGMA synchronous=ON");
            close();

        }

        try {
            System.err.println("insert handling aggregation..." + sdf.format(new Date()));
            handleAggregation(entity, result, true, false); //todo: set to true
            System.err.println("insert caching..." + sdf.format(new Date()));
            copyAndCache(entity);
            System.err.println("insert aggregating finished..." + sdf.format(new Date()));

        }catch(NoSuchFieldException e){
            throw new IllegalStateException("Field configuration does not appear to be correct: " + entity,e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Field configuration does not appear to be correct: " + entity,e);
        }
        System.err.println("insert returning..." + sdf.format(new Date()));
        return result;
    }

    public int deleteAll(){
        openToWrite();
        int result = sqLiteDatabase.delete(persistent.defaultTable(), null, null);
        if(cache != null) {
            cache.clear();
        }
        close();
        return result;
    }

    public int delete(int id){
        openToWrite();
        String where = "_id = "+id;
        int result = sqLiteDatabase.delete(persistent.defaultTable(), where, null);
        close();
        if(cache != null) {
            removeFromCache(id);
        }
        return result;
    }

    public long delete(long id){
        openToWrite();
        String where = "_id = "+id;
        int result = sqLiteDatabase.delete(persistent.defaultTable(), where, null);
        close();
        if(cache != null) {
            removeFromCache(id);
        }
        return result;
    }

    private void removeFromCache(long id) {
        try{

            T remover = null;

            for(T t:cache){
                Long cacheId = (Long) extractValue(t,idField);
                if(cacheId.equals(id)){
                    remover = t;
                }
            }

            if(remover != null) {
                cache.remove(remover);
                addToBrolPool(remover);
            }

        }catch (IllegalAccessException e ){
            throw new RuntimeException(e);
        }catch (NoSuchFieldException e ){
            throw new RuntimeException(e);
        }
    }

    public int update(T entity){
        ContentValues contentValues = populateContent(entity);
        int result = 0;
        boolean updated = false;
        try {

            try {
                openToWrite();
//                sqLiteDatabase.execSQL("PRAGMA synchronous=OFF");   //todo: source transactions given from caller
//                sqLiteDatabase.setLockingEnabled(false);
//                sqLiteDatabase.beginTransaction();


                String where = "_id = " + extractValue(entity,idField);
                 //todo: cachedcomparedchange
                if(cacheComparedChange(entity)){
                    result = sqLiteDatabase.update(persistent.defaultTable(),contentValues,where,null);
                    if(result == 0){
                        //TODO: dangerous: but used for insert/update unknown problem.
                        sqLiteDatabase.insert(persistent.defaultTable(),null,contentValues);
                    }
                    updated = true;
                }
//                sqLiteDatabase.setTransactionSuccessful();

            }catch (Exception e){

                throw new RuntimeException(e);

            }finally {

//                sqLiteDatabase.endTransaction();
//                sqLiteDatabase.setLockingEnabled(true);
//                sqLiteDatabase.execSQL("PRAGMA synchronous=ON");
                close();

            }

            boolean aggrChanged = handleAggregation(entity,(Long)extractValue((T)entity,idField),false,false); //TODO: get foreign id from existing obj and add?
            if(aggrChanged || updated) {
                copyAndCache(entity);
            }
            return result;

        }catch(NoSuchFieldException e){
            throw new IllegalStateException("Field configuration does not appear to be correct: " + entity,e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Field configuration does not appear to be correct: " + entity,e);
        }



    }

    protected ContentValues populateContent(T entity){
        ContentValues contentValues = new ContentValues();

        try {
            for(String javaField:annotatedFields.keySet()){
                //            java.lang.reflect.Field rField = persistentClass.getField(javaField);
                Object value = ReflectionUtil.getFieldValue(entity,javaField);
                //            Object value = rField.get(entity);
                Field dbField = annotatedFields.get(javaField);
//                if(dbField.name().equals("_id"))continue;
                String name = dbField.name();
                if(dbField.type().equals(SQLiteFieldType.INTEGER)){
                    contentValues.put(name,(Integer)value);
                }else if(dbField.type().equals(SQLiteFieldType.TEXT) || dbField.type().equals(SQLiteFieldType.BLOB)){
                    contentValues.put(name,(String)value);
                }else if(dbField.type().equals(SQLiteFieldType.REAL)){
                    contentValues.put(name,(Float)value);
                }else if(dbField.type().equals(SQLiteFieldType.BOOLEAN)){
                    contentValues.put(name,(Boolean)value);

                }else if(dbField.type().equals(SQLiteFieldType.NULL)) {
                    //                contentValues.put(name,null);
                    //niks !?
                }else if(dbField.type().equals(SQLiteFieldType.AGGREGATE)){
//                    if(value instanceof Collection<?>){
//                        Collection bunch = (Collection) value;
//                        if(bunch.size() > 0){
//                            SQLiteAdapter aggregatedAdapter = new SQLiteAdapter(context,dbField.aggregatedClass());
//
//                            for(Object aggregate:bunch){
//                                aggregatedAdapter.insert((Serializable)aggregate);
//                            }
//
//                        }
//                    }
                }else if(dbField.type().equals(SQLiteFieldType.DATE)){
                    contentValues.put(name,(Long)value);
                }else if(dbField.type().equals(SQLiteFieldType.LONG)){
                    contentValues.put(name,(Long)value);
                }
                else{
                    throw new IllegalStateException("None of the expected types are found!");
                }
            }

        }catch(NoSuchFieldException e){
            throw new IllegalStateException("Field configuration does not appear to be correct: " + entity,e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Field configuration does not appear to be correct: " + entity,e);
        }

        return contentValues;
    }

    private boolean handleAggregation(T entity,long foreignId,boolean insert,boolean transactionExists) throws NoSuchFieldException, IllegalAccessException {
//        clearBrolPool();
//        System.gc();
        boolean changed = false;
        for(String javaField:annotatedFields.keySet()){
            //            java.lang.reflect.Field rField = persistentClass.getField(javaField);
            Object value = ReflectionUtil.getFieldValue(entity,javaField);
            Field dbField = annotatedFields.get(javaField);
            if(dbField.type().equals(SQLiteFieldType.AGGREGATE)){
                if(value instanceof Collection<?>){
                    Collection bunch = (Collection) value;
//                    if(aggregatedAdapter == null) {
//                        aggregatedAdapter = new SQLiteAdapter(context, dbField.aggregatedClass());
//                    }

                    Field foreignKeyField = null;
                    for(Object aggregatedField: aggregatedAdapter.getAnnotatedFields().values()){
                        if(((Field)aggregatedField).foreignKey()){
                            foreignKeyField = (Field) aggregatedField;
                        }
                    }

                    if(foreignKeyField != null){
//                        List fetchedAggregates = aggregatedAdapter.findBySelection(foreignKeyField.name()+" = "+foreignId);

//                        for(T t:cache){
//                            ex
//                        }

                        if(bunch.size() > 0){

//                            aggregatedAdapter.sqLiteHelper.getWritableDatabase();

//                            for(Object aggregate:bunch){
//
//                                Map<String,Field> annotations = aggregatedAdapter.getAnnotatedFields();
//
//                                for(String field:annotations.keySet()){
//                                    if(annotations.get(field).name().equals("_id")){
//
//                                    }else if(annotations.get(field).foreignKey() && foreignId != 0){
//                                        if(!ReflectionUtil.getFieldValue(aggregate,field).equals(foreignId)){
//                                            ReflectionUtil.setFieldValue(aggregate,field,foreignId);
//                                        }
//                                    }
////                                    else if(annotations.get(field).foreignKey() && foreignId == 0){ //todo: create different aggregation types
////                                        aggregatedAdapter.delete((Long)extractValue((T)aggregate,aggregatedAdapter.idField));
////                                    }//nen db fetch! expensive...  Use new @field or so to determine delete)
//                                }
//
//                                if(!insert){   //TODO: foreign id is added to checklistitem too soon??  needs to be done here to know whether to update/insert
//                                    if(aggregatedAdapter.cacheComparedChange(aggregate)) {
//                                        aggregatedAdapter.update( aggregate); //todo: compare with cache system
//                                    }else{
//                                        System.err.println("Aggregate not updated: "+aggregate);
//                                    }
//                                }else {
//                                    aggregatedAdapter.insert( aggregate);
//                                }
//                            }

                            //update/insert part
                            changed = aggregatedAdapter.insertOrUpdateBatch(insert, (Collection<T>) bunch,transactionExists);
                            if(changed) {
                                copyAndCache(entity);
                            }


                            //delete part
                            List fetchedAggregates = aggregatedAdapter.findInCacheByField(foreignId, foreignKeyField);
                            clearBrolPool();

//                            try{
//                                sqLiteDatabase.execSQL("PRAGMA synchronous=OFF");   //todo: source transactions given from caller
//                                sqLiteDatabase.setLockingEnabled(false);
//                                sqLiteDatabase.beginTransaction();

                                List<Long> toDelete = new ArrayList<Long>();
                                if(fetchedAggregates != null) {
                                    for (Object dbEntry : fetchedAggregates) {
                                        Long dbId = (Long) aggregatedAdapter.extractValue( dbEntry, aggregatedAdapter.idField);
                                        boolean found = false;

                                        for (Object aggregate : bunch) {
                                            Long bunchId = (Long) aggregatedAdapter.extractValue( aggregate, aggregatedAdapter.idField);
                                            if (bunchId.equals(dbId)) {
                                                found = true;
                                            }
                                        }
                                        if (!found) {
                                            toDelete.add(dbId);
                                        }
                                    }
                                }

                                for(Long deleteId:toDelete){
                                    Long deleteRes = aggregatedAdapter.delete(deleteId);
                                    System.err.println("Deleted result of "+deleteId+": "+deleteRes);
                                    changed = true;
                                }
//                            }catch (Exception e){
//                                throw new RuntimeException(e);
//                            }finally {
//                                sqLiteDatabase.endTransaction();
//                                sqLiteDatabase.setLockingEnabled(true);
//                                sqLiteDatabase.execSQL("PRAGMA synchronous=ON");
//                                close();
//                            }
                        }
//                        trans
                    }
                }
            }
        }
        return changed;
    }


    /**
     *
     * @return
     */
    public Cursor queueAll(){
        openToRead();
        String[] columns = getColumns();
        int i=0;


        Cursor cursor = sqLiteDatabase.query(persistent.defaultTable(), columns,
                null, null, null, null, null);
//        close();
        return cursor;
    }

    public T findById(int id){
        Cursor cursor = null;
        try {

            T cached = findInCacheById(id);
            if(cached != null){
                return cached;
            }

            String[] columns = getColumns();
            cursor = cursorBySelection("_id = "+" "+id,columns);

        T instance = null;
        if(cursor.moveToFirst()){
            instance = persistentClass.newInstance();
            fillTarget(instance, cursor);
            copyAndCache(instance);
            return instance;
        }

        }catch(IllegalAccessException ia){
            return null;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }finally {
            close();
            if(cursor != null) cursor.close();
        }

        return null;
    }

    public T findById(long id){
        if(sdf == null) {
            sdf = new SimpleDateFormat(format);
        }
        System.err.println("Starting findbyid..." + sdf.format(new Date()));

        Cursor cursor = null;
        try {
            T cached = findInCacheById(id); //todo: tbph copy o_O
            if(cached != null){
                System.err.println("Returning cached instance..." + sdf.format(new Date()));
                addToBrolPool(cached);
                return cached;
            }
            String[] columns = getColumns();

            System.err.println("Findbyid cursoring..." + sdf.format(new Date()));
            cursor = cursorBySelection("_id = "+" "+id,columns);

            T instance = null;
            if(cursor.moveToFirst()){
                System.err.println("Instantiating findbyid instance..." + sdf.format(new Date()));
                instance = persistentClass.newInstance();
                fillTarget(instance, cursor);
                System.err.println("findbyid caches..." + sdf.format(new Date()));
                copyAndCache(instance);
                addToBrolPool(cursor);
                System.err.println("returning findbyid db entry..." + sdf.format(new Date()));
                return instance;
            }else{
                System.err.println("Returning null..."+sdf.format(new Date()));
                addToBrolPool(cursor);
            }

        }catch(IllegalAccessException ia){
            return null;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }finally {
            close();
            if(cursor != null) cursor.close();
        }

        return null;
    }

    public List<T> findBySelection(String selection){      //Todo: aggregatedadapter override
        if(sdf == null) {
            sdf = new SimpleDateFormat(format);
        }
        System.err.println("Starting findbyselection..."+sdf.format(new Date()));

        if(cacheful && selection == null && (cache != null && cache.size() > 0)){
            List<T> copiedCache = getCopiedCache();
            System.err.println("returning cached finds..."+sdf.format(new Date()));
            return copiedCache;
        }
        System.err.println("Cursoring..."+sdf.format(new Date()));
        Cursor cursor = cursorBySelection(selection,getColumns());
        try {
            System.err.println("Instantiating cursor..."+sdf.format(new Date()));
        if(cursor.moveToFirst()){
            List<T> values = new ArrayList<T>();
            do {
                T instance = persistentClass.newInstance();
                fillTarget(instance, cursor);
                values.add(instance);
            }while (cursor.moveToNext());

            System.err.println("Caching objects..." + sdf.format(new Date()));
            clearBrolPool();
            addToBrolPool(cursor);
            for(T t:values){
                copyAndCache(t);
            }
            System.err.println("Cache done returning..." + sdf.format(new Date()));

            return values;
        }
        }catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e) ;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }finally {
            close();
            cursor.close();
        }
        return null;
    }

    public T findInCacheById(long id) throws NoSuchFieldException, IllegalAccessException {

        List<T> hit = findInCacheByField(id,idField);

        if(hit != null && hit.size() == 1){
            return hit.get(0);
        }
        return null;
    }

    public List<T> findInCacheByField(Object value,Field field) throws NoSuchFieldException, IllegalAccessException {

        List<T> hits = new ArrayList<T>();
        for(T t:cache){
            Object valueCache = extractValue(t,field);
            if(valueCache != null && valueCache.equals(value)){
                hits.add(t);
            }
        }
        return hits;
    }

    private List<T> getCopiedCache(){
        if(cache != null && cache.size() > 0){
            List<T> copies = new ArrayList<T>();
            System.err.println(this+" copies "+cache.size()+" objects");
            for(T t:cache){
                AbstractEntity cloneT = (AbstractEntity) t;
                copies.add((T)cloneT.clone());
            }
            //todo: check approach
            addToBrolPool(copies);
            return copies;
        }
        return null;
    }

    public Cursor cursorBySelection(String selection,String[]columns){
        try {
            openToRead();
            Cursor cursor = sqLiteDatabase.query(persistent.defaultTable(),columns,selection,null,
                    null,null,null);

//        close();
            return cursor;
        }catch (SQLiteException e){
            close();
            throw new RuntimeException(e);
        }

    }

    private T fillTarget(T target,Cursor data) throws NoSuchFieldException, IllegalAccessException {
        long id = 0;

        for(String key:annotatedFields.keySet()){
            Field field = annotatedFields.get(key);

            int columnId = data.getColumnIndex(field.name());
            if(field.type().equals(SQLiteFieldType.INTEGER)){
                ReflectionUtil.setFieldValue(target, key, data.getInt(columnId));
                if(field.name().equals("_id")){
                    id = data.getInt(columnId);
                }
            }else if(field.type().equals(SQLiteFieldType.TEXT) || field.type().equals(SQLiteFieldType.BLOB)) {
                ReflectionUtil.setFieldValue(target, key, data.getString(columnId));
            }else if(field.type().equals(SQLiteFieldType.REAL)){
                ReflectionUtil.setFieldValue(target, key, data.getFloat(columnId));
            }else if(field.type().equals(SQLiteFieldType.BOOLEAN)){
                int value = data.getInt(columnId);
                if(value == 0){
                    ReflectionUtil.setFieldValue(target,key,false);
                }else{
                    ReflectionUtil.setFieldValue(target,key,true);
                }
            }else if(field.type().equals(SQLiteFieldType.DATE)){
                ReflectionUtil.setFieldValue(target, key, data.getLong(columnId));
            }else if(field.type().equals(SQLiteFieldType.LONG)){
                ReflectionUtil.setFieldValue(target, key, data.getLong(columnId));
                if(field.name().equals("_id")){
                    id = data.getLong(columnId);
                }
            }
        }

        //2nd loop
        for(String key:annotatedFields.keySet()){
            Field field = annotatedFields.get(key);
            if(field.type().equals(SQLiteFieldType.AGGREGATE)){
                //get the id (loop 2)
//                if(aggregatedAdapter == null) {
//                    aggregatedAdapter = new SQLiteAdapter(context, field.aggregatedClass());
//                }
                List fetchedAggregators = aggregatedAdapter.findBySelection(field.aggregatedField() + " = " + id);//todo: we may be able to cache
                ReflectionUtil.setFieldValue(target,key,fetchedAggregators);
            }
        }

        return target;
    }

    /**
     * Beware to only use this to iterate over database fields.  Aggregated fields are ignored because they exist elsewhere
     * @return
     */
    private String[] getColumns(){
        String[] columns = new String[annotatedFields.size()];
        int i=0;
        for(Field field:annotatedFields.values()){
            if(field.type().equals(SQLiteFieldType.AGGREGATE)) continue;
            columns[i] = field.name();
            i++;
        }
        return columns;
    }

    protected Object extractValue(Object entity,Field field) throws NoSuchFieldException, IllegalAccessException {
        if(entity == null || !entity.getClass().equals(persistentClass)){
            return null; //multiple aggregates could get us in trouble
        }
        String modelField = null;
        for(String name:annotatedFields.keySet()){
            if(annotatedFields.get(name).name().equals(field.name())){
                modelField = name;
                break;
            }
        }
        if(modelField == null)return null;

        return ReflectionUtil.getFieldValue(entity,modelField);
    }

    public Map<String, Field> getAnnotatedFields() {
        return annotatedFields;
    }

    protected void copyAndCache(T t) throws NoSuchFieldException, IllegalAccessException {
        if(cacheful){
            T removeFromCache = null;
            for(T cached:cache){

                Long cachedId = (Long) extractValue(cached,idField);
                Long id = (Long) extractValue(t,idField);

                if(cachedId.equals(id)){
                    removeFromCache = cached;
                    break;
                }

            }
            System.err.println(this+" copies 1 object");
            AbstractEntity cloneable = (AbstractEntity) t;
            T copy = (T)cloneable.clone();
            cache.add(copy);
            addToBrolPool(copy);
            addToBrolPool(t);
            if(removeFromCache != null){
                addToBrolPool(removeFromCache);
                cache.remove(removeFromCache);
            }
//        if(brolPool.size() > 200){
//            Runnable runner = new Runnable() {
//                @Override
//                public void run() {
//                    brolPool.clear();
//                    System.err.println("Clearing the garbage pool");
//                    System.gc();
//                }
//            };
//            runner.run();
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }

        }
    }

    public void addToBrolPool(Object brol){
//        brolPool.add(brol);
    }

    public void clearBrolPool(){
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                if(brolPool != null && brolPool.size() > 0){
                    System.err.println("Clearing brolpool, size: " + brolPool.size());
                    brolPool.clear();
                    System.gc();
                }
            }
        };
        runner.run();
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    public class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String scriptCreateDatabase = "create Table if not exists " + persistent.defaultTable() + " (";

            String idLine = "";
            String columnLine = "";


            for(Field field:annotatedFields.values()){
                if(!field.type().equals(SQLiteFieldType.AGGREGATE)){  //aggregated fields are fetched elsewhere

                    if(field.name().equals("_id")) {
                        idLine = field.name()+" INTEGER PRIMARY KEY AUTOINCREMENT";
                    }else {
                        if(field.foreignKey() && field.type().equals(SQLiteFieldType.INTEGER)){
                            columnLine += ", " + field.name() + " " + field.type() + " REFERENCES " + field.joinTable();
                            if(!field.onDelete().equals(FieldModifier.NONE)){ //there's additional constraints
                                columnLine += " ON DELETE " + field.onDelete().name();
                            }

                        }else {
                            columnLine += ", " + field.name() + " " + field.type();
                        }
                    }
                }
            }

            scriptCreateDatabase += idLine + columnLine + ");";
            db.execSQL(scriptCreateDatabase);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }

    }

}
