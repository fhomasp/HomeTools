package com.peeterst.android.checklistview;

import com.peeterst.android.filesystem.FSSpace;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 22/02/11
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistEmailInView extends ChecklistInputView {

    public ChecklistEmailInView() {
        this.domain = FSSpace.INPUT;
        this.title = "Checklist Inbox";
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Uri uri = getIntent().getData();
//        String theMIMEType = "hometools/checklist";
//        if (theMIMEType!=null) {
//            Intent theIntent = new Intent(Intent.ACTION_VIEW);
//            theIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK+Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//            theIntent.setDataAndType(uri, theMIMEType);
//            try {
//                startActivity(theIntent);
//            } catch (ActivityNotFoundException anfe) {
//                throw new RuntimeException(anfe);
//            }
//        }
//    }
}
