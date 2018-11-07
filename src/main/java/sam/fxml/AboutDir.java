package sam.fxml;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import sam.anime.db2.DirOrFile;

public class AboutDir extends Tab {
	private final ListView<DirOrFile> list = new ListView<>();
	private final Label title = new Label();
	private final BorderPane root = new BorderPane(list);

	public AboutDir() {
		super("About Dir");
		root.setTop(title);
		setContent(root);
		setDisable(true);
		list.setPlaceholder(new Text("NOTHING"));
	}
	
	public void init() {
		// contents has not been decided
	}
}
