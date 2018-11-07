package sam.fxml;

import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import sam.main.App;

public abstract class StringList<E> extends CustomListVBox<E> {
	private String title;
	
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	protected E getNewValue() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(App.getStage());

		dialog.setTitle("Enter");
		dialog.setHeaderText("insert new "+title+" for: \n"+getAnime().getTitle());

		String s = dialog.showAndWait().orElse(null);
		return create(s);
	}
	protected abstract E create(String source) ;
}
