package com.peeterst.android.server;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 26/05/13
 * Time: 20:07
 *  Wrapper for handling i/o
 */
public class DataChecker {

    private String data = null;
    private boolean isXMLLike = false;
    private InputStream inputStream;
    private int streamSize;
    private Object dataObject;
    private boolean isIdGreet;

//    public DataChecker(final BufferedReader reader) throws IOException {
////        StringBuilder builder = new StringBuilder();
////        while(true) {
////            builder.append(reader.readLine());
////            if(!reader.ready()){
////                data = builder.toString();
////                this.isXMLLike = isXMLLike(data);
////            }
////
////        }
//
//        this.data = reader.readLine();
//        boolean rdy = reader.ready();
//        this.isXMLLike = isXMLLike(data);
//    }
//
//    public DataChecker(InputStream inputStream) throws IOException {
//        this.inputStream = new ObjectInputStream(inputStream);
//
//    }

    public DataChecker(InputStream inputStream,int size) throws IOException {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream));
        this.streamSize = size;

    }

    public void processObject() throws IOException, ClassNotFoundException {
        if( ! (this.inputStream instanceof ObjectInputStream)){
            throw new RuntimeException("Expected ObjectInputStream");
        }else {

            Object object = ((ObjectInputStream)inputStream).readObject() ;
            if(object instanceof String){
                this.data = (String) object;
                this.dataObject = object;
            }else{
                this.dataObject = object;
            }
        }


    }

    public String processClientHeader() throws IOException {
        String headerLine = ((DataInputStream)inputStream).readUTF();
        String name = Header.getNameFromHeaderLine(headerLine);
        if(name == null){
            throw new IOException("Invalid header data");
        }
        return name;
    }

    public void processUTF() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;
        String line = null;
        while(!done){
            try {
                line = ((DataInputStream)inputStream).readUTF();
            }catch (EOFException e){
                //no utf?
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                line = br.readLine();
            }
            if(line != null) {
                stringBuilder.append(line);
            }
            if(inputStream.available() == 0){
                done = true;
            }
        }

//        if (streamSize > 0) {
//            byte[] buff = new byte[streamSize];
//            int ret_read = inputStream.read(buff);
//            stringBuilder.append(new String(buff, 0, ret_read));
//        }


        data = stringBuilder.toString();
        this.isXMLLike = isXMLLike(data);
        this.isIdGreet = checkIdGreet(data);
    }

    private boolean checkIdGreet(String data) {
        String name = Header.getNameFromHeaderLine(data);
        if(name != null){
            this.data = name;
        }
        return name != null;
    }

    public void processBuffered() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;
        String line = null;
        while(!done){
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            line = br.readLine();
            stringBuilder.append(line);
            if(inputStream.available() == 0){
                done = true;
            }
        }

//        if (streamSize > 0) {
//            byte[] buff = new byte[streamSize];
//            int ret_read = inputStream.read(buff);
//            stringBuilder.append(new String(buff, 0, ret_read));
//        }


        data = stringBuilder.toString();
        this.isXMLLike = isXMLLike(data);
    }

    /**
     * return true if the String passed in is something like XML
     *
     *
     * @param inXMLStr a string that might be XML
     * @return true of the string is XML, false otherwise
     */
    public static boolean isXMLLike(String inXMLStr) {

        if(inXMLStr == null || inXMLStr.equals("")){
            return false;
        }

        boolean retBool = false;
        Pattern pattern;
        Matcher matcher;

        // REGULAR EXPRESSION TO SEE IF IT AT LEAST STARTS AND ENDS
        // WITH THE SAME ELEMENT
        final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";



        // IF WE HAVE A STRING
        if (inXMLStr != null && inXMLStr.trim().length() > 0) {

            // IF WE EVEN RESEMBLE XML
            if (inXMLStr.trim().startsWith("<")) {

                pattern = Pattern.compile(XML_PATTERN_STR,
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

                // RETURN TRUE IF IT HAS PASSED BOTH TESTS
                matcher = pattern.matcher(inXMLStr);
                retBool = matcher.matches();
                if(retBool){
                    List<String> tempMatches = new ArrayList<String>();
                    for(int i = 0; i < matcher.groupCount();i++){
                        tempMatches.add(matcher.group(i));

                    }
                    System.out.println(tempMatches);
                }
            }
            // ELSE WE ARE FALSE
        }

        return retBool;
    }

    public boolean isIdGreet() {
        return isIdGreet;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isXMLLike() {
        return isXMLLike;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setIdGreet(boolean idGreet) {
        this.isIdGreet = idGreet;
    }
}
