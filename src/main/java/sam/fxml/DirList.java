package sam.fxml;

import sam.anime.dao.DirSubpath;
import sam.fx.helpers.FxCell;

public class DirList extends CustomListVBox<DirSubpath>  {
	private DirSearch dirSearch;
	
	public void init(DirSearch dirSearch) {
		this.dirSearch = dirSearch;
		
		init(FxCell.listCell(d -> d.subpath));
		add.setText("Add New");
	}
	
	@Override
	protected DirSubpath getNewValue() {
		return dirSearch.openDialog("select dir to add:\n"+getAnime().getTitle());
	}
}

