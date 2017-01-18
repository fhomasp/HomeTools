package com.peeterst.android;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import com.peeterst.android.adapter.ClientListAdapter;
import com.peeterst.android.data.adapter.SQLiteAdapter;
import com.peeterst.android.hourtracker.R;
import com.peeterst.android.model.Client;
import net.sf.javaocr.demos.android.utils.image.ImageProcessor;
import net.sf.javaocr.demos.android.utils.image.IntegralImageSlicer;
import net.sf.javaocr.demos.android.utils.image.SauvolaImageProcessor;
import net.sourceforge.javaocr.Image;
import net.sourceforge.javaocr.cluster.FeatureExtractor;
import net.sourceforge.javaocr.matcher.FreeSpacesMatcher;
import net.sourceforge.javaocr.matcher.Match;
import net.sourceforge.javaocr.matcher.MatcherUtil;
import net.sourceforge.javaocr.matcher.MetricMatcher;
import net.sourceforge.javaocr.ocr.PixelImage;
import net.sourceforge.javaocr.plugin.cluster.extractor.FreeSpacesExtractor;
import net.sourceforge.javaocr.plugin.moment.HuMoments;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10/06/12
 * Time: 23:36
 * Main activity
 */
public class ConsultantHourTrackerMain extends ListActivity {

    private SQLiteAdapter<Client> clientSQLiteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        clientSQLiteAdapter = new SQLiteAdapter<Client>(this,Client.class);
        addTestClients();
        List<Client> activeClients = clientSQLiteAdapter.findBySelection("Active = 1");

        ClientListAdapter listAdapter = new ClientListAdapter(this,R.layout.clientrow,activeClients);
        setListAdapter(listAdapter);

        //TODO: testing possible functionality that's supposed to be elsewhere
        InputStream in = getClass().getResourceAsStream("/2012-05-31 14.23.25r.jpg");
        byte[] buf = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (int readNum; (readNum = in.read(buf)) != -1;) {
                baos.write(buf, 0, readNum);
                //no doubt here is 0
                /*Writes len bytes from the specified byte array starting at offset
                off to this byte array output stream.*/
//                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        byte[] imageFrame = baos.toByteArray();

        ImageProcessor imageProcessor = new SauvolaImageProcessor(1024,768,30,20,15,15);        //??
        IntegralImageSlicer slicer = new IntegralImageSlicer(new PixelImage(1024,768));  //2560,1920
        PixelImage pixelImage = imageProcessor.prepareImage(imageFrame,1024,768); // TODO: ??

        List<List<Image>> glyphs = slicer.sliceUp(pixelImage);

        FeatureExtractor extractor = new HuMoments();
        FreeSpacesExtractor freeSpaceExtractor = new FreeSpacesExtractor();
        MetricMatcher metricMatcher = new MetricMatcher();
        FreeSpacesMatcher freeSpacesMatcher = new FreeSpacesMatcher();


        for(List<Image> row:glyphs){
            if(row != null && row.size() > 0){
                StringBuilder sb = new StringBuilder();
                for(Image glyph:row){
                    if(glyph != null){
                        final double[] features = extractor.extract(glyph);
                        final double[] freeSpaces = freeSpaceExtractor.extract(glyph);

                        // cluster mathcer
                        List<Match> matches = metricMatcher.classify(features);
                        // perform matching of free spaces
                        List<Match> freeSpaceMatches = freeSpacesMatcher.classify(freeSpaces);

                        // ... and bayes it

                        List<Match> mergedMatches = MatcherUtil.merge(matches, freeSpaceMatches);
                        if (!mergedMatches.isEmpty()) {

                            for(Match match:mergedMatches){
                                Character character = match.getChr();
                                sb.append(character);
                                sb.append(" ");
                            }



                        } else {
                           System.err.println(".. Empty match ..");
                        }

                    }
                }
                System.err.println(sb.toString());
            }
        }



    }


    private void addTestClients(){
        for(int i = 0; i < 3; i++ ){
            Client client = new Client();
            client.setName("client "+i);
            client.setActive(true);
            clientSQLiteAdapter.insert(client);
        }
        Client nonActive = new Client();
        nonActive.setName("inactive");
        nonActive.setActive(false);
        clientSQLiteAdapter.insert(nonActive);
    }


}
