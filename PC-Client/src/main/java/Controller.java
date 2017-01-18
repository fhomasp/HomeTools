import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.DisplacementMap;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Label label;

    public TextField txtUserName;

    public Text output;

    public Button submit;

    @FXML
    private ToggleGroup toggleGroup;

    public GridPane rootGridPane;


    public void sayHello(ActionEvent event) {
        label.setText("Hello World!");
    }

    public void close(ActionEvent event){
        if(output.isVisible()){
            output.setVisible(false);
        }else{
            output.setVisible(true);
        }
    }

    public void submit(ActionEvent event){
        Object source = event.getSource();
        System.out.println(txtUserName.getText());
        output.setText(txtUserName.getText());

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        submit.setEffect(new DisplacementMap());
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        final KeyValue kv = new KeyValue(submit.opacityProperty(), 0);
        final KeyFrame kf = new KeyFrame(Duration.millis(600), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        submit.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/save_icon.png"))));

        RadioButton radio1 = new RadioButton("One");
        radio1.setToggleGroup(toggleGroup);
        radio1.setSelected(true);
        rootGridPane.add(radio1,9,1);

        RadioButton radio2 = new RadioButton("Two");
        radio2.setToggleGroup(toggleGroup);
        rootGridPane.add(radio2,10,1);

        RadioButton radio3 = new RadioButton("Three");
        radio3.setToggleGroup(toggleGroup);
        rootGridPane.add(radio3,11,1);

    }
}
