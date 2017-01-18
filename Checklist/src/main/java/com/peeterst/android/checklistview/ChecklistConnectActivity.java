package com.peeterst.android.checklistview;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import com.peeterst.android.HomeTools.R;
import com.peeterst.android.adapter.ContactsAdapter;
import com.peeterst.android.data.ChecklistLocalIO;
import com.peeterst.android.data.ChecklistNetworkIO;
import com.peeterst.android.data.adapter.SQLiteAdapter;
import com.peeterst.android.filesystem.FSSpace;
import com.peeterst.android.model.AndroidServerSettings;
import com.peeterst.android.server.DataChecker;
import com.peeterst.android.server.Postable;
import com.peeterst.android.server.Server;
import com.peeterst.android.util.ChecklistTempUtils;
import com.peeterst.android.util.Formatter;
import com.peeterst.android.xml.ChecklistReadXML;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 2/07/13
 * Time: 20:38
 */
public class ChecklistConnectActivity extends Activity implements Postable {

    private ChecklistLocalIO localIO = ChecklistLocalIO.getInstance(this);

    private ChecklistNetworkIO networkIO = ChecklistNetworkIO.getInstance();
    private ImageButton sendButton;
    private ImageButton stopButton;
    private SQLiteAdapter<AndroidServerSettings> serverSettingsSQLiteAdapter;
    private ContactsAdapter contactsAdapter;
    private TextView noContacts;
    private NotificationManager notificationManager;
    public static int CHECKLIST_CONNECT = 10002;
    private Notification notification;
    private ImageButton connectButton;
    private EditText serverText;
    private EditText portText;
    private EditText peerNameText;
    private ToggleButton clientToggle;
    private ToggleButton serverToggle;
    private EditText idText;

//    private enum Mode {
//        SERVER,CLIENT
//    }

//    private Mode mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            Server.getServer(this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setContentView(R.layout.checklistconnect);
        setTitle(R.string.client_connect_title);

        serverSettingsSQLiteAdapter = new SQLiteAdapter<AndroidServerSettings>(this,AndroidServerSettings.class,false);

        idText = (EditText) findViewById(R.id.networkid);
        idText.setText(Build.MODEL);

        peerNameText = (EditText) findViewById(R.id.clientname);
        peerNameText.setText("Peer1");

        connectButton = (ImageButton) findViewById(R.id.connectbutton);
        final EditText serverTalkText = (EditText) findViewById(R.id.servertalkfield);

        serverText = (EditText) findViewById(R.id.server);
        serverText.setText("192.168.0.247");

        portText = (EditText) findViewById(R.id.port);
        portText.setText("8900");

        sendButton = (ImageButton) findViewById(R.id.sendbutton);
        stopButton = (ImageButton) findViewById(R.id.disconnectbutton);
        stopButton.setEnabled(false);
        noContacts = (TextView) findViewById(R.id.contacts_empty);
        ImageButton saveButton = (ImageButton) findViewById(R.id.contacts_savebutton);

        ListView listView = (ListView) findViewById(R.id.connectcontactslistview);
        List<AndroidServerSettings> contactsList = serverSettingsSQLiteAdapter.findBySelection(null);
        if(contactsList == null || contactsList.isEmpty()){
            contactsAdapter = new ContactsAdapter(new ArrayList<AndroidServerSettings>(),this);
            noContacts.setVisibility(View.VISIBLE);
        }else{
            contactsAdapter = new ContactsAdapter(contactsList,this);
            noContacts.setVisibility(View.INVISIBLE);
        }
        contactsAdapter.sort(null);
        listView.setAdapter(contactsAdapter);


        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serverTalkText.getText() != null && portText.getText().toString() != null) {
                    //todo: name field
                    doLongerTask();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    disconnect();
                } catch (IOException e) {
                    //TODO: error msg
                    e.printStackTrace();
                }
            }
        });

        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(networkIO.mode == ChecklistNetworkIO.Mode.CLIENT) {
                    networkIO.send(serverTalkText.getText().toString());
                }else {
                    for(String clientKey:networkIO.getClientOutlets().keySet()){
                        try {
                            send(clientKey,serverTalkText.getText().toString());
                            post("<Server to " + clientKey + ">\t" + serverTalkText.getText().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                            post("Error from client: "+clientKey+" -- "+e.getMessage());
                        }
                    }
                }
                serverTalkText.setText("");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidServerSettings serverSettings = new AndroidServerSettings();
                serverSettings.setName(idText.getText().toString());
                serverSettings.setName(peerNameText.getText().toString());
                serverSettings.setServer(serverText.getText().toString());
                if(Formatter.isNumeric(portText.getText().toString()))
                serverSettings.setPort(Integer.parseInt(portText.getText().toString()));
                serverSettings.setClient(true);
                if(!contactsAdapter.contains(serverSettings)) {
                    serverSettings.setId(serverSettingsSQLiteAdapter.insert(serverSettings));
                    contactsAdapter.add(serverSettings);
                    contactsAdapter.sort(null);
                    contactsAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(ChecklistConnectActivity.this, getText(R.string.contact_already_in_db), Toast.LENGTH_LONG).show();
                }

                if(contactsAdapter.getCount() > 0){
                    noContacts.setVisibility(View.INVISIBLE);
                }else {
                    noContacts.setVisibility(View.VISIBLE);
                }
            }
        });

        ((RadioGroup)findViewById(R.id.client_server_modegroup)).setOnCheckedChangeListener(toggleListener);

        clientToggle = (ToggleButton) findViewById(R.id.client_mode_button);

        serverToggle = (ToggleButton) findViewById(R.id.server_mode_button);
        initMode();

        if(networkIO.isConnected()){
            toggleConnectionButtons(true);
        }
    }

    public void initMode(){
        if(networkIO.mode == null){
            if(!networkIO.hasNetwork()){
                clientToggle.setEnabled(false);
                clientToggle.setChecked(false);
                serverToggle.setChecked(false);
                serverToggle.setEnabled(false);
                connectButton.setEnabled(false);
                Toast.makeText(ChecklistConnectActivity.this,getText(R.string.no_network), Toast.LENGTH_LONG).show();
                return;
            } else {
                connectButton.setEnabled(true);
            }
            if(networkIO.isConnected()){
                if(networkIO.isClient()){
                    networkIO.mode = ChecklistNetworkIO.Mode.CLIENT;
                }else {
                    networkIO.mode = ChecklistNetworkIO.Mode.SERVER;
                }
                initMode();
            }else {
                networkIO.mode = ChecklistNetworkIO.Mode.CLIENT;
                initMode();
            }
        }else if (networkIO.mode.equals(ChecklistNetworkIO.Mode.CLIENT)){
            clientToggle.setEnabled(false);
            clientToggle.setChecked(true);
            serverToggle.setChecked(false);
            serverToggle.setEnabled(true);
        }else if(networkIO.mode.equals(ChecklistNetworkIO.Mode.SERVER)){
            clientToggle.setEnabled(true);
            clientToggle.setChecked(false);
            serverToggle.setChecked(true);
            serverToggle.setEnabled(false);
        }
        toggleClientServer(networkIO.mode.equals(ChecklistNetworkIO.Mode.CLIENT));
    }

    public void disconnect() throws IOException {
        changeConnectedStatus(false);
        if(networkIO.mode.equals(ChecklistNetworkIO.Mode.CLIENT)) {
            networkIO.disconnect();
        }else {
            networkIO.stopServer();
        }
        notificationManager.cancel(CHECKLIST_CONNECT);
        for(Server.UIOutputHandler client:networkIO.getClientOutlets().values()){
            client.close();
        }
    }

    private void doLongerTask() {
        final ProgressDialog dialog = ProgressDialog.show(ChecklistConnectActivity.this, "Please wait", "Connecting...", true);
        dialog.setCancelable(true);

        final Handler longTaskHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what) {
                    case 0:
                        changeConnectedStatus(true);
                        break;
                    case -1:
                        changeConnectedStatus(false);
                        Toast.makeText(ChecklistConnectActivity.this,getText(R.string.connection_setup_error), Toast.LENGTH_LONG).show();
                        break;
                    case -2:
                        changeConnectedStatus(false);
                        Toast.makeText(ChecklistConnectActivity.this,getText(R.string.connection_setup_timeout), Toast.LENGTH_LONG).show();

                }
            }
        };

        final ChecklistNetworkHandler networkHandler = new ChecklistNetworkHandler();

        new Thread() {
            @Override
            public void run() {
                try{

                    if(networkIO.mode.equals(ChecklistNetworkIO.Mode.CLIENT)) {
                        networkIO.connectClient(idText.getText().toString(), serverText.getText().toString(),
                                portText.getText().toString(), networkHandler);
                    }else {
                        networkIO.connectServer(ChecklistConnectActivity.this,networkHandler,idText.getText().toString(),portText.getText().toString());
                    }

//                    sleep(2000);
                    //Dismiss dialog, and notify handler to done this task
                    dialog.dismiss();
                    longTaskHandler.sendEmptyMessage(0);
                }catch (SocketTimeoutException e){
                    e.printStackTrace();
                    longTaskHandler.sendEmptyMessage(-2);
                    dialog.dismiss();
                } catch (Exception e) {
                    //TODO: error msg
                    e.printStackTrace();
                    longTaskHandler.sendEmptyMessage(-1);
                    dialog.dismiss();
                }

            }
        }.start();
    }

    public void changeConnectedStatus(boolean connected){

        toggleConnectionButtons(connected);
        if(connected){
            if(networkIO.mode.equals(ChecklistNetworkIO.Mode.CLIENT)){
                CharSequence title = getText(R.string.connect_status_title);
                notification = new Notification(R.drawable.connected_icon,title,System.currentTimeMillis());
                Intent intent = new Intent(getBaseContext(),ChecklistConnectActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notification.setLatestEventInfo(this, getText(R.string.connect_status_enabled), title, contentIntent);
                notificationManager.notify(CHECKLIST_CONNECT,notification);
            }else {
                CharSequence title = getText(R.string.connect_status_title);
                notification = new Notification(R.drawable.connected_icon,title,System.currentTimeMillis());
                Intent intent = new Intent(getBaseContext(),ChecklistConnectActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);
                notification.setLatestEventInfo(this, getText(R.string.server_status_enabled), title, contentIntent);
                notificationManager.notify(CHECKLIST_CONNECT,notification);
            }
        }else {
//            notification.setLatestEventInfo(this, getText(R.string.connect_status_disabled), title, contentIntent);

        }

    }

    public void toggleConnectionButtons(boolean connected){
        sendButton.setEnabled(connected);
        stopButton.setEnabled(connected);

        connectButton.setEnabled(!connected);
        clientToggle.setEnabled(!connected);
        serverToggle.setEnabled(!connected);
    }

    public void loadSettings(AndroidServerSettings settings){
        peerNameText.setText(settings.getName());
        portText.setText(""+settings.getPort());
        serverText.setText(settings.getServer());
    }

    public boolean compareSettings(AndroidServerSettings settings){
        if(!peerNameText.getText().toString().equals(settings.getName())){
            return false;
        }
        if(!portText.getText().toString().equals(""+settings.getPort())){
            return false;
        }
        if(!serverText.getText().toString().equals(settings.getServer())){
            return false;
        }
        return true;
    }

    public boolean deleteContact(AndroidServerSettings settings){
        serverSettingsSQLiteAdapter.delete(settings.getId());
        return true;
    }

    public class ChecklistNetworkHandler extends Handler {

        public static final int INPUT_CLOSED = R.string.connect_server_disconnect;
        public static final int SERVING_STOPPED = R.string.connect_serving_stopped;

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            ChecklistConnectActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(msg.obj != null && msg.obj instanceof Integer){
                        switch ((Integer)msg.obj){
                            case INPUT_CLOSED:{
                                Toast.makeText(ChecklistConnectActivity.this,getText((Integer) msg.obj), Toast.LENGTH_LONG).show();
                                try {
                                    disconnect();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                                break;
                            }
                            case SERVING_STOPPED:{
                                Toast.makeText(ChecklistConnectActivity.this,getText((Integer) msg.obj), Toast.LENGTH_LONG).show();
//                                try {
                                    changeConnectedStatus(false);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                    throw new RuntimeException(e);
//                                }
                                break;
                            }
                        }

                    }
                    else if(msg.obj != null && msg.obj instanceof DataChecker){
                        DataChecker dataChecker = (DataChecker) msg.obj;
                        if(dataChecker.isXMLLike()){
                            ChecklistConnectActivity.this.postWhole("server",dataChecker);
                        }else if(dataChecker.isIdGreet()){
                            ChecklistConnectActivity.this.setPeerId(dataChecker);
                        }
                        else {
                            ChecklistConnectActivity.this.post(dataChecker.getData());
                        }
                    }


                }
            });

        }
    }

    static final RadioGroup.OnCheckedChangeListener toggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);
                view.setEnabled(view.getId() != i);
            }
        }
    };

    /**
     * Toggle buttons say android:onClick="onToggle", triggering this
     * @param view
     */
    public void onToggle(View view) {
        ((RadioGroup)view.getParent()).check(view.getId());
        if(view.getId() == R.id.client_mode_button){
            networkIO.mode = ChecklistNetworkIO.Mode.CLIENT;
        }else {
            networkIO.mode = ChecklistNetworkIO.Mode.SERVER;
        }
        toggleClientServer(networkIO.mode.equals(ChecklistNetworkIO.Mode.CLIENT));
    }

    private void toggleClientServer(boolean client){
        this.peerNameText.setEnabled(client);
        this.peerNameText.setFocusable(client);
        this.peerNameText.setFocusableInTouchMode(client);

        if(!client){
            try {
                serverText.setText(Server.getHostAddress());

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
//            this.peerNameText.setInputType(InputType.TYPE_NULL);

        }else {
            serverText.setText("192.168.0.247");//todo: ""
            portText.setText("12345");
//            this.peerNameText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }


    @Override
    public void post(String data) {
        System.out.println(data);
    }

    @Override
    public void post(String clientName, String data) {
        System.out.println("<"+clientName+">  "+data);
    }

    @Override
    public void setPeerId(final DataChecker dataChecker) {
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
                if(dataChecker.isIdGreet()) {
                    ChecklistConnectActivity.this.peerNameText.setText(dataChecker.getData());
                    dataChecker.setIdGreet(false);
                }else {
                    throw new IllegalStateException("Only ID greets may be used here");
                }
           }
       });
    }

    @Override
    public void postWhole(final String source, final DataChecker checker) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(checker.isXMLLike()){
                    try {
                        ChecklistReadXML checklistReadXML = new ChecklistReadXML(checker.getData());

                        //todo: dialog ?
                        localIO.addToSpecificPath(checklistReadXML.getChecklist(),ChecklistConnectActivity.this, FSSpace.INPUT);

                        StringBuilder toastTxt = new StringBuilder();
                        toastTxt.append(getString(R.string.incoming_checklist));
                        toastTxt.append(" ");
                        toastTxt.append(getString(R.string.from));
                        toastTxt.append(" ");
                        toastTxt.append(" \"");
                        toastTxt.append(source);
                        toastTxt.append("\" ");
                        toastTxt.append(":");
//                        toastTxt.append(getString(R.string.toemailout1));
                        toastTxt.append(" \"");
                        toastTxt.append(checklistReadXML.getChecklist().getTitle());
                        toastTxt.append("\" ");
                        toastTxt.append(getString(R.string.toemailin));

                        Toast.makeText(ChecklistConnectActivity.this,toastTxt, Toast.LENGTH_LONG).show();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                        Toast.makeText(ChecklistConnectActivity.this,getText(R.string.checklist_invalid_format), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ChecklistConnectActivity.this,getText(R.string.network_error), Toast.LENGTH_LONG).show();
                    } catch (SAXException e) {
                        e.printStackTrace();
                        Toast.makeText(ChecklistConnectActivity.this,getText(R.string.checklist_invalid_format), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    public String attachClient(String name, Server.UIOutputHandler outputHandler) {
        if(networkIO.getClientOutlets().containsKey(name)){
            outputHandler.setOwnerName(name+"1");
            name = name+"1";
        }
        networkIO.getClientOutlets().put(name, outputHandler);
//        addTab(name,outputHandler);
        post("Client connected: " + name);
        return name;
    }

    @Override
    public void changeName(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Server.UIOutputHandler findClient(String name) {
        if(networkIO.getClientOutlets() == null || networkIO.getClientOutlets().size() == 0){
            return null;
        }else{
            return networkIO.getClientOutlets().get(name);
        }
    }

    @Override
    public void removeClient(String name) {
        this.networkIO.getClientOutlets().remove(name);
        //todo: if graphical view available, close it here
    }

    @Override
    public void send(String clientName, String msg) throws IOException {
        boolean sent = false;
        for(String clientKey:networkIO.getClientOutlets().keySet()){
            if(clientKey.equals(clientName)){

                //todo: dev test code
                if(msg.equalsIgnoreCase("checklist")) {
                    msg = ChecklistTempUtils.createMockChecklist();
                }
                //todo end


                networkIO.getClientOutlets().get(clientKey).send(msg);
                sent = true;
                break;
            }
        }
        if(!sent) {
            throw new IOException("No client to send to");
        }
    }

    @Override
    public InputStream getEnvPropertiesInputStream() throws IOException {
        try{
            InputStream inputStream = new FileInputStream(this.getFilesDir().getPath()+"/"+FSSpace.ENV+"/server.properties");
            return inputStream;
        }catch(FileNotFoundException e){
            storeEnvProperties(new Properties());
            return new FileInputStream(this.getFilesDir().getPath()+"/"+FSSpace.ENV+"/server.properties");
        }

    }

    @Override
    public void storeEnvProperties(Properties properties) throws IOException {
        this.getFilesDir();

        FileOutputStream fos = new FileOutputStream(new File(this.getFilesDir().getPath()+"/"+FSSpace.ENV+"/server.properties"));

        properties.store(fos,null);

    }
}
