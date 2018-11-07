package sam.fxml;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

public class Field extends HBox {
	public final TextField tf = new TextField();
	private final Text text = new Text();
	
	public Field() {
		super(5);
		getChildren().addAll(text, tf);
		text.minWidth(200);
		tf.setEditable(false);
		HBox.setHgrow(tf, Priority.ALWAYS);
	}
	
	/**
	 * see :: https://stackoverflow.com/questions/31345260/javafx-custom-component-issue
	 * @param s
	 */
	public void setTitle(String s) {
		text.setText(s);
	}
	public String getTitle() {
		return text.getText();
	}
	public StringProperty titleProperty() {
		return text.textProperty();
	}

	public void setText(String value) {
		tf.setText(value);
	}
}
