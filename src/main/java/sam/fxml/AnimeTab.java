package sam.fxml;

import static sam.myutils.MyUtilsCheck.isEmpty;
import static sam.myutils.MyUtilsCheck.isEmptyTrimmed;
import static sam.myutils.MyUtilsCheck.isNotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import sam.anime.dao.Anime;
import sam.anime.dao.AnimeDao;
import sam.anime.dao.IdParseResult;
import sam.collection.CollectionUtils;
import sam.collection.Iterables;
import sam.fx.alert.FxAlert;
import sam.fx.helpers.FxFxml;
import sam.fx.popup.FxPopupShop;
import sam.fx.popup.FxPopupShop.PopupWrap;
import sam.main.App;
import sam.main.Tabs;
import sam.myutils.MyUtilsCheck;

public class AnimeTab extends Tab  {
	@FXML private VBox aboutBox;
	private final FlowPane idsLinks = new FlowPane(5, 5);
	@FXML private TextField idTF;
	@FXML private Button idBtn;
	@FXML private Button multipleIdBtn;
	@FXML private Field titleF;
	@FXML private Field episodeF;
	@FXML private Field airedF;
	@FXML private Field genreF;
	@FXML private TextArea synopsisTa;
	//TODO @FXML private Button saveBtn;
	
	private final ReadOnlyObjectWrapper<Anime> currentAnime = new ReadOnlyObjectWrapper<>();
	private final AnimeDao loader ;
	private Tabs tabs;
	
	public AnimeTab(AnimeDao loader) throws IOException {
		super("Anime");
		this.loader = loader;
		
		FxFxml.load(this, true);
	}
	public void init(Tabs tabs) {
		this.tabs = tabs;
	}
	@FXML
	private void idGO(Event e) {
		if(idBtn.isDisable()) return;

		TextInputDialog dialog = new TextInputDialog();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(App.getStage());
		dialog.setHeaderText("Enter MyAnimeList id");
		dialog.setTitle("enter");

		String s = dialog.showAndWait().orElse(null);
		Object id = loader.toId(s);
		if(id.getClass() == String.class) {
			FxPopupShop.showHidePopup(id.toString(), 2500);
			return;
		}
		clear();
		openAnime((int)id);
	}
	
	public void openAnime(int malId) {
		loadAnimes(Collections.singletonList(malId), false);
	}

	public void clear() {
		titleF.tf.setText(null); 
		episodeF.tf.setText(null);
		airedF.tf.setText(null);
		genreF.tf.setText(null);

		idTF.setText(null);
		synopsisTa.clear();
		//TODO saveBtn.setVisible(false);
	}

	@FXML
	private void multipleIdGO(Event e) {
		TextArea ta = new TextArea();
		TextInputDialog dialog = new TextInputDialog();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setResultConverter(b -> b == ButtonType.OK ? ta.getText() : null);
		dialog.setHeaderText("Enter id separated by newline");
		BorderPane root = new BorderPane(ta);
		CheckBox check = new CheckBox("Are Animes Related? ");
		check.setPadding(new Insets(10));
		root.setBottom(check);
		dialog.getDialogPane().setContent(root);

		dialog.showAndWait().ifPresent(result -> {
			if(isEmptyTrimmed(result)) {
				FxPopupShop.showHidePopup("invalid input", 2000);
				return;
			}
			Map<Boolean, List<IdParseResult>> map = Pattern.compile("\r?\n")
					.splitAsStream(result.trim())
					.map(String::trim)
					.filter(MyUtilsCheck::isNotEmpty)
					.map(loader::toId)
					.collect(Collectors.partitioningBy(IdParseResult::isFailed));

			List<IdParseResult> list = map.get(true);
			if(isNotEmpty(list)) {
				if(list.size() == 1)
					FxPopupShop.showHidePopup(list.get(0).error, 2000);
				else
					FxAlert.showErrorDialog(String.join("\n", Iterables.map(list, r -> r.error)), "ERRORS", null);
			}

			list = map.get(false);
			if(isEmpty(list)) return;

			loadAnimes(CollectionUtils.map(list, r -> r.id), check.isSelected());
		});
	}
	
	public ReadOnlyObjectProperty<Anime> currentAnimeProperty() {
		return currentAnime.getReadOnlyProperty();
	}
	
	public Anime getCurrentAnime() {
		return currentAnime.get();
	} 

	@FXML
	private void saveAction(Event e) {
		/** TODO
		 * if(getCurrentAnime() == null) // || TODO saveBtn.isDisable())
			return;
		
		if(aboutBox.getChildren().get(0) == idsLinks)
			idsLinks.getChildren().forEach(n -> ((Anime)((Node)n).getUserData()).setSaved(true));
		else
			getCurrentAnime().setSaved(true);
		 */
		
		//TODO saveBtn.setText(currentAnime.isSaved() ? "UPDATE" : "Save");
	}
	public void setCurrentAnime(Event e) {
		setAnime((Anime)((Node)e.getSource()).getUserData());
	}
	private void loadAnimes(List<Integer> ids, boolean areAnimeRelated) {
		if(ids.isEmpty()) {
			setAnime((Anime)null);
			return;
		}
		tabs.setDisable(true);
		idBtn.setDisable(true);
		multipleIdBtn.setDisable(true);
		PopupWrap[] popup = {FxPopupShop.show("LOADING")};
		boolean[] b = new boolean[tabs.getTabs().size()];
		int nn = 0;
		for (Tab t : tabs.getTabs()) 
			b[nn++] = t.isDisable();
		
		tabs.getTabs().forEach(t -> t.setDisable(true));
		
		Runnable hide = () -> {
			if(popup[0] == null) return;
			
			tabs.getTabs().forEach(t -> t.setDisable(t != this));
			PopupWrap p = popup[0]; 
			Platform.runLater(() -> FxPopupShop.hide(p, 1500));
			popup[0] = null;
		};
		
		int size = ids.size();
		
		if(size == 1) {
			if(aboutBox.getChildren().get(0) == idsLinks)
				aboutBox.getChildren().remove(0);
		} else {
			if(aboutBox.getChildren().get(0) != idsLinks)
				aboutBox.getChildren().add(0, idsLinks);
			idsLinks.getChildren().clear();
		}
		
		boolean[] isfirst = {true};
		List<Anime> list = areAnimeRelated ? new ArrayList<>() : null;
		
		for (int i = 0; i < size; i++) {
			int n = i;
			int mal_id = ids.get(n);
			Hyperlink link = new Hyperlink(Integer.toString(mal_id));
			link.setDisable(true);
			idsLinks.getChildren().add(link);
			
			loader.loadAnime(mal_id, (anime, error) -> Platform.runLater(() -> {
				link.setDisable(false);
				if(anime == null) { 
					link.setOnAction(e -> FxAlert.showErrorDialog(error == null ? "REASON: UNKNOWN" : null, "failed to load anime: "+mal_id, error));
					link.setStyle("-fx-text-fill: red;");
				} else {
					if(areAnimeRelated )
						list.add(anime);
					link.setUserData(anime);
					link.setOnAction(this::setCurrentAnime);
					
					if(isfirst[0]) {
						hide.run();						
						isfirst[0] = false;
						Platform.runLater(link::fire);
					}
					//TODO
				}
				if(n == size - 1) {
					hide.run();
					idBtn.setDisable(false);
					multipleIdBtn.setDisable(false);
					
					int nn2 = 0;
					for (Tab t : tabs.getTabs()) 
						t.setDisable(b[nn2++]);
					
					if(areAnimeRelated) {
						for (Anime a : list) {
							for (Anime a2 : list) {
								if(a2 != a)
									loader.animeAreRelated(a2, a);
							}
						}
					}
				}
			}));
		}
		//TODO
		

	}
	public StringProperty  titleProperty() {
		return titleF.tf.textProperty();
	}
	private void setAnime(Anime anime) {
		if(anime == null) {
			clear();
			currentAnime.set(anime);
			return;
		}
		
		idTF.setText(String.valueOf(anime.getMalId()));
		titleF.setText(anime.getTitle());
		episodeF.setText(anime.getEpisodes());
		airedF.setText(anime.getAired());
		genreF.setText(anime.getGenres());
		synopsisTa.setText(anime.getSynopsis());
		//TODO saveBtn.setVisible(true); 
		//TODO saveBtn.setText(currentAnime.isSaved() ? "UPDATE" : "Save");
		
		currentAnime.set(anime);
	}
	public void stop() {
		loader.stop(); 
	}
	public String getTitle() { return titleF.tf.getText(); }
	public String getEpisode() { return episodeF.tf.getText(); }
	public String getAired() { return airedF.tf.getText(); }
	public String getGenre() { return genreF.tf.getText(); }
	public String getSynopsis() { return synopsisTa.getText(); }

	public Button createOpenButton(BooleanBinding disable, IntSupplier malIdGetter) {
		Button open = new Button("Open");
		open.setOnAction(e -> openAnime(malIdGetter.getAsInt()));
		open.disableProperty().bind(disable);
		
		return open;
	}
}
