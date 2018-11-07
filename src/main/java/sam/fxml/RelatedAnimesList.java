package sam.fxml;

import javafx.scene.control.Button;
import sam.anime.dao.AnimeTitle;
import sam.fx.helpers.FxCell;

public class RelatedAnimesList extends CustomListVBox<AnimeTitle> {
	private  AnimeTab animeTab;
	private AnimeSearch animeSearch;
	
	public void init(AnimeTab animeTab, AnimeSearch animeSearch) {
		this.animeTab = animeTab;
		this.animeSearch = animeSearch;
		
		init(buttons(), FxCell.listCell(m -> m.mal_id +" | "+ m.title));
	}
	
	private Button[] buttons() {
		Button open = animeTab.createOpenButton(NOTHING_SELECTED, () -> selectedItem().getMalId());
		return new Button[] {add, delete, open};
	}
	
	@Override
	protected AnimeTitle getNewValue() {
		return animeSearch.openDialog("select related anime to:\n"+getAnime().getTitle());
	}
}
