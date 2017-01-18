package com.peeterst.android.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 25/05/13
 * Time: 20:11
 * To change this template use File | Settings | File Templates.
 */
public interface Postable {

    public void post(String data);

    public void post(String clientName,String data);

    public void postWhole(String source,DataChecker checker);

    public String attachClient(String name,Server.UIOutputHandler outputHandler);

    public void changeName(String name);

    public Server.UIOutputHandler findClient(String name);

    public void removeClient(String name);

    public void send(String clientName,String msg) throws IOException;

    public InputStream getEnvPropertiesInputStream() throws IOException;

    public void storeEnvProperties(Properties properties) throws IOException;

    void setPeerId(DataChecker dataChecker);
}
