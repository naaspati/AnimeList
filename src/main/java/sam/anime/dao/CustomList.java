package sam.anime.dao;

import java.util.List;

public class CustomList<E> extends CustomCollection<E, List<E>> {
	public CustomList(List<E> data, Anime anime) {
		super(data, anime);
	}
	
}
