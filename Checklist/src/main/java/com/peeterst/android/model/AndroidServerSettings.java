package com.peeterst.android.model;

import com.peeterst.android.data.persist.Field;
import com.peeterst.android.data.persist.Persistent;
import com.peeterst.android.data.persist.SQLiteFieldType;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 6/07/13
 * Time: 17:42
 */
@Persistent(database = "ChecklistManager",defaultTable = "ServerSettings")
public class AndroidServerSettings {

    @Field(name = "_id",type = SQLiteFieldType.LONG)
    public Long id;

    @Field(name = "name",type = SQLiteFieldType.TEXT)
    public String name;

    @Field(name = "server",type = SQLiteFieldType.TEXT)
    public String server;

    @Field(name = "port",type = SQLiteFieldType.INTEGER)
    public int port;

    @Field(name = "client",type = SQLiteFieldType.BOOLEAN)
    public boolean client;


    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AndroidServerSettings that = (AndroidServerSettings) o;

        if (client != that.client) return false;
        if (port != that.port) return false;
        if (!name.equals(that.name)) return false;
        if (!server.equals(that.server)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + server.hashCode();
        result = 31 * result + port;
        result = 31 * result + (client ? 1 : 0);
        return result;
    }
}
