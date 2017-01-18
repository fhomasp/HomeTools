package com.peeterst.android.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 30/06/13
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public class ServerConfigService {

    private static ServerConfigService instance;
    private Postable postable;

    private ServerConfigService(){

    }

    public static ServerConfigService getInstance() throws IOException {
        if(instance == null){
            instance = new ServerConfigService();
//            Server.determineServerDefaultsAndUseConfig(); //init the server props if not available
        }
        return instance;
    }



    public Properties getEnvProperties() throws IOException {
        Properties properties = new Properties();

        properties.load(postable.getEnvPropertiesInputStream());

        return properties;
    }

    public void storeEnvProperties(Properties properties) throws IOException {
        postable.storeEnvProperties(properties);
    }

    public Postable getPostable() {
        return postable;
    }

    public void setPostable(Postable postable) {
        this.postable = postable;
    }
}
