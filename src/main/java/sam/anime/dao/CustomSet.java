package sam.anime.dao;

import java.util.Set;

public class CustomSet<E> extends CustomCollection<E, Set<E>> {
	CustomSet(Set<E> data, Anime anime) {
		super(data, anime);
	}
	
	
}
