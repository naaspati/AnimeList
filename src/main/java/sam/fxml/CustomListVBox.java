package sam.fxml;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import sam.anime.dao.Anime;
import sam.anime.dao.CustomSet;
import sam.fx.popup.FxPopupShop;

public abstract class CustomListVBox<E> extends ListVBox<E> implements EventHandler<ActionEvent> {
	protected final  Button add = button("ADD", false);
	protected final  Button delete = button("REMOVE", true);
	
	protected void init(Callback<ListView<E>, ListCell<E>> cellfactory) {
		super.init(new Button[] {add, delete}, cellfactory);
	}
	protected Button button(String text, boolean disableWhenNothingSelected) {
		Button b = new Button(text);
		b.setOnAction(this);
		if(disableWhenNothingSelected)
			b.disableProperty().bind(NOTHING_SELECTED);
		return b;
	}
	
	@Override
	public void setSource(Anime anime, CustomSet<E> source) {
		super.setSource(anime,source);
		add.setDisable(source == null);
	}
	@Override
	public void handle(ActionEvent event) {
		if(event.getSource() == add)
			addAction();
		else 
			deleteAction();
		
	}
	
	private void deleteAction() {
		if(getSource() == null) return;
		List<E> list = listView.getSelectionModel().getSelectedItems();
		if(list.isEmpty())
			return;
		
		for (E e : list)
			getSource().remove(e);
		
		list = new ArrayList<>(list);
		listView.getSelectionModel().clearSelection();
		listView.getItems().removeAll(list);
	}
	protected void addAction() {
		if(getSource() == null) return;
		
		E e = getNewValue();
		
		if(e == null)
			FxPopupShop.showHidePopup("cancelled", 1500);
		else {
			getSource().add(e);
			listView.getItems().add(e);
		}
	}
	protected abstract E getNewValue();
}
