package sam.fxml;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import sam.anime.dao.Anime;
import sam.anime.dao.CustomSet;
import sam.anime.dao.ModificationListener;

public class ListVBox<T> extends VBox {
	protected final ListView<T> listView = new ListView<>();
	private final SimpleStringProperty title = new SimpleStringProperty(); 
	private final Label titleLabel = new Label();
	private CustomSet<T> source;
	private Anime anime;
	
	protected final ReadOnlyObjectProperty<T> selectedItem = listView.getSelectionModel().selectedItemProperty();
	protected final BooleanBinding NOTHING_SELECTED = selectedItem.isNull();
	protected final  ModificationListener<T> listener = (anime, item, type) -> {
		switch (type) {
			case ADDED:
				listView.getItems().add(item);
				break;
			case REMOVED:
				listView.getItems().remove(item);
				break;
		}
	};

	public T selectedItem() {
		return selectedItem.get();
	}

	public ListVBox() {
		super(5);
	}
	protected void init(Button[] buttons, Callback<ListView<T>, ListCell<T>> cellfactory) {
		titleLabel.setWrapText(true);
		setStyle("-fx-background-color:white");
		VBox.setMargin(titleLabel, new Insets(5));
		
		titleLabel.textProperty().bind(Bindings.concat("(", Bindings.size(this.listView.getItems()),") | ", title));

		getChildren().addAll(titleLabel, listView);

		if(buttons != null) {
			HBox hbox = new HBox(5, buttons);
			hbox.setAlignment(Pos.CENTER_RIGHT);
			hbox.setPadding(new Insets(5) );
			getChildren().add(hbox);
		}

		listView.setPrefHeight(100);
		listView.setPlaceholder(new Text("NOTHING"));
		if(cellfactory != null)
			listView.setCellFactory(cellfactory);
	}
	
	public final StringProperty textProperty() {
		return title;
	}
	public final void setText(String value) {
		title.set(value);
	}
	public final String getText() {
		return title.get();
	}
	public void setSource(Anime anime, CustomSet<T> source) {
		if(this.source != null)
			this.source.removeListener(listener);
		
		this.source = source;
		this.anime = anime;

		listView.getSelectionModel().clearSelection();
		if(source != null) {
			listView.getItems().setAll(source.get());
			source.addListener(listener);
		} else
			listView.getItems().clear();
	}
	public Anime getAnime() {
		return anime;
	}
	public CustomSet<T> getSource() {
		return source;
	}
}
