package sam.fxml;

import static javafx.application.Platform.runLater;

import java.util.Objects;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Callback;
import sam.anime.dao.ModificationListener;
import sam.anime.dao.UnmodifiableCustomCollection;
import sam.fx.clipboard.FxClipboard;
import sam.fx.popup.FxPopupShop;
import sam.logging.MyLoggerFactory;
import sam.main.App;
import sam.string.TextSearch;

public abstract  class SearchList<E> extends Tab {
	protected final ListView<E> list = new  ListView<>();
	protected TextSearch<E> search;
	private final TextField searchTF = new TextField();
	private UnmodifiableCustomCollection<E> allData;

	protected final ReadOnlyObjectProperty<E> selectedItem = list.getSelectionModel().selectedItemProperty();
	protected final BooleanBinding NOTHING_SELECTED = selectedItem.isNull();
	private boolean init;

	protected final ModificationListener<E> listener = (anime, item, type) -> {
		switch (type) {
			case ADDED:
				if(search.getFilter().test(item))
					list.getItems().add(item);
				break;
			case REMOVED:
				list.getItems().remove(item);
				break;
		}
	};
	public E selectedItem() {
		return selectedItem.get();
	}
	public SearchList(String title) {
		super(title);
	}
	protected void init(String header, UnmodifiableCustomCollection<E> data, Node bottom, Callback<ListView<E>, ListCell<E>> cellFactory) {
		this.allData = data;  
		Text countText = new Text();
		HBox searchBox = new HBox(5, new Text("search"), searchTF, countText);
		bottom = bottom == null ? searchBox : new VBox(5, bottom, searchBox);
		BorderPane.setMargin(bottom, new Insets(5));

		Label top = new Label(header);  

		countText.textProperty().bind(Bindings.size(list.getItems()).asString());
		setContent(new BorderPane(list, top, null, bottom, null));

		BorderPane.setMargin(top, new Insets(5));       
		HBox.setHgrow(searchTF, Priority.ALWAYS);
		searchBox.setAlignment(Pos.CENTER_LEFT);
		getContent().setStyle("-fx-background-color:white");

		list.setPlaceholder(new Text("NOTHING"));
		if(cellFactory != null)
			list.setCellFactory(cellFactory);

		setOnSelectionChanged(e -> init2());
	}
	private void init2() {
		if(init) return;
		init = true;

		MyLoggerFactory.logger(getClass()).fine("INIT");
		search = new TextSearch<>(this::mapToLowercase, 500);

		list.setOnKeyReleased(e -> {
			if(e.isControlDown() && e.getCode() == KeyCode.C) {
				if(selectedItem() == null) return;
				String s = clipboardText(selectedItem());
				FxClipboard.copyToClipboard(s);
				FxPopupShop.showHidePopup("copied: "+s, 1500);
			}
		});

		searchTF.textProperty().addListener((p,o,n) -> search.search(n));
		search.setOnChange(() -> runLater(() -> search.process(list.getItems())));
		allData.addListener(listener);
		reset();
	}
	private void reset() {
		search.setAllData(allData.get());
		list.getItems().setAll(allData.get());
	}

	public E openDialog(String header) {
		Dialog<E> dialog = new Dialog<>();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(App.getStage());
		String backup = searchTF.getText();

		Node node = getContent();
		setContent(null);
		Node bottom = dialogBottom(dialog);

		double d = list.getMaxHeight();
		list.setMaxHeight(200);
		dialog.getDialogPane().setContent(bottom == null ? node : new BorderPane(node, null, null, bottom, null));
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.setResultConverter(b -> b == ButtonType.OK ? list.getSelectionModel().getSelectedItem() : null);

		Label  text = new Label(header);
		text.setPadding(new Insets(10, 5,10, 5));
		text.setWrapText(true);
		text.setMaxWidth(300);
		dialog.getDialogPane().setHeader(text);

		Optional<E> result = dialog.showAndWait();
		dialog.getDialogPane().setContent(null);
		setContent(node);
		if(!Objects.equals(backup, searchTF.getText()))
			searchTF.setText(backup);
		list.setMaxHeight(d);

		return result.orElse(null);
	} 

	protected abstract String mapToLowercase(E e);
	protected abstract String clipboardText(E item);

	protected Node dialogBottom(Dialog<E> dialog) {
		return null;
	}
	protected void checkCurrentItem() {
		if(!search.getFilter().test(selectedItem())) {
			int index = list.getSelectionModel().getSelectedIndex();
			list.getSelectionModel().clearSelection();
			list.getItems().remove(index);
		}
	}
}
