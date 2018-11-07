package sam.fxml;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import sam.anime.dao.AnimeDao;
import sam.anime.dao.AnimeTitle;
import sam.fx.helpers.FxCell;
import sam.fx.helpers.FxLabel;
import sam.fx.helpers.FxUtils;

public class AnimeSearch extends SearchList<AnimeTitle> {
	public AnimeSearch() {
		super("Anime Search");
	}
	public void init(AnimeDao dao, AnimeTab tab) {
		init("Anime Search", dao.allAnimeTitle(), bottom(tab), FxCell.listCell(t -> t.title));
	}

	private Node bottom(AnimeTab tab) {
		Label text = FxLabel.ofWrappedText("");
		HBox box = new HBox(5,text, FxUtils.longPaneHbox(), tab.createOpenButton(NOTHING_SELECTED, () -> selectedItem().mal_id));
		box.setAlignment(Pos.CENTER_RIGHT);
		selectedItem.addListener((p, o, n) -> text.setText(n == null ? null : toString(n)));
		return box;
	}
	private String toString(AnimeTitle m) {
		return m.mal_id+" | "+m.title;
	}
	@Override
	protected String mapToLowercase(AnimeTitle e) {
		return e.lowercased;
	}
	@Override
	protected String clipboardText(AnimeTitle item) {
		return item.title;
	}
}
