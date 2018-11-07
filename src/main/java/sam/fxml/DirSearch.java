package sam.fxml;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sam.anime.dao.AnimeDao;
import sam.anime.dao.AnimeTitle;
import sam.anime.dao.DirSubpath;
import sam.fx.helpers.FxCell;
import sam.fx.helpers.FxUtils;
import sam.fx.popup.FxPopupShop;
import sam.string.TextSearch;

public class DirSearch extends SearchList<DirSubpath> {
	private Button button(String text, BooleanBinding disable, EventHandler<ActionEvent> action) {
		Button b = new Button(text);
		if(disable != null) b.disableProperty().bind(disable);
		if(action != null) b.setOnAction(action);
		return b;
	}
	
	private Button openAnime;
	private Text malId;
	private Label path ;
	private AnimeDao dao;
	private static final Predicate<String> IS_NUMBER = Pattern.compile("\\d+").asPredicate();
	private ChoiceBox<String> choice;
	private AnimeSearch animeSearch;

	public DirSearch() {
		super("Dir Search");
	}
	public void init(AnimeDao dao, AnimeTab animeTab, AnimeSearch animeSearch) {
		this.dao = dao;
		this.animeSearch = animeSearch;
		
		init("Dir Search", dao.getAllDirs(), bottom(animeTab), FxCell.listCell(d -> d.subpath));
	}

	private Node bottom(AnimeTab animeTab) {
		malId = new Text();
		path = new Label();
		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); 
		
		path.setStyle("-fx-font-size:0.8em;-fx-font-family:Consolas");
		path.setWrapText(true);
		HBox.setHgrow(path, Priority.NEVER);
		openAnime = animeTab.createOpenButton(NOTHING_SELECTED.or(Bindings.createBooleanBinding(() -> malId.getText() != null && IS_NUMBER.test(malId.getText()), malId.textProperty()).not()), () -> selectedItem().getMalId());
		Button hide = button("HIDE",NOTHING_SELECTED.or(Bindings.createBooleanBinding(() -> selectedItem() == null || selectedItem().getMalId() < -1, selectedItem)), e -> apply(dir -> dao.hideDir(dir)));
		
		choice = new ChoiceBox<>(FXCollections.observableArrayList("ALL","with MAL_ID", "without MAL_ID", "HIDDEN"));
		choice.getSelectionModel().select(0);
		choice.getSelectionModel().selectedIndexProperty()
		.addListener((p, o, num) -> {
			int n = num.intValue();
			if(n == 0)
				search.setPreFilter(TextSearch.trueAll());
			else if(n == 1)
				search.setPreFilter(s -> s.getMalId() >= 0);
			else if(n == 2)
				search.setPreFilter(s -> s.getMalId() == -1);
			else if(n == 3)
				search.setPreFilter(s -> s.getMalId() < -1);
		});

		HBox buttons = new HBox(5,choice, FxUtils.longPaneHbox(), button("Set mal id",NOTHING_SELECTED, this::setMalID), openAnime, hide);
		buttons.setPadding(new Insets(5));
		buttons.setAlignment(Pos.CENTER_RIGHT);

		selectedItem.addListener((p, o, n)  -> update(n));
		return new VBox(5, new HBox(1, malId,new Separator(Orientation.VERTICAL), path), buttons);
	}
	public void update() {
		update(selectedItem());
	}
	
	private void update(DirSubpath n) {
		if(n == null) {
			malId.setText(null);
			path.setText(null);
		} else {
			malId.setText(String.valueOf(n.getMalId()));
			path.setText(n.subpath);
		}
	}
	private void setMalID(ActionEvent e) {
		AnimeTitle temp = animeSearch.openDialog("Set Anime to Dir:\n"+selectedItem().subpath);

		if(temp == null)
			FxPopupShop.showHidePopup("cancelled", 1500);
		else {
			apply(d -> dao.addDir(temp, d));
			FxPopupShop.showHidePopup("added dir", 2500);
			update();
		}
	}
	
	private void apply(Consumer<DirSubpath> action) {
		List<DirSubpath> data = this.list.getSelectionModel().getSelectedItems();
		if(data.isEmpty()) return;
		if(data.size() == 1) {
			action.accept(data.get(0));
			checkCurrentItem();
		} else {
			Predicate<DirSubpath> filter = search.getFilter();
			data = new ArrayList<>(data);
			this.list.getSelectionModel().clearSelection();
			
			for (DirSubpath d : data) {
				action.accept(d);
				if(!filter.test(d)) this.list.getItems().remove(d);
			}
		}
	}
	@Override
	protected void checkCurrentItem() {
		super.checkCurrentItem();
		update();
	}
	@Override
	protected String mapToLowercase(DirSubpath e) {
		return e.lowercased;
	}
	@Override
	protected String clipboardText(DirSubpath item) {
		return item.subpath;
	}
	public void stop() {
		//TODO  save choice
	}
}
