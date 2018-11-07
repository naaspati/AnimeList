package sam.anime.dao;

import java.util.Collection;

public class UnmodifiableCustomCollection<E> extends CustomCollection<E, Collection<E>> {
	public UnmodifiableCustomCollection(Collection<E> set, Anime anime) {
		super(set, anime);
	}
	@Override
	public boolean add(E e) {
		throw new IllegalAccessError();
	}
	@Override
	public boolean remove(E e) {
		throw new IllegalAccessError();
	}

	boolean add0(E e) {
		return super.add(e);
	}
	boolean remove0(E e) {
		return super.remove(e);
	}
}
