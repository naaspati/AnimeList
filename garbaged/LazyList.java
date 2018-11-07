package sam.anime.dao;

import static sam.myutils.MyUtilsCheck.isNotEmpty;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * replaced with UnmodifiableSetWrapper
 * @author Sameer
 *
 * @param <E>
 */
@Deprecated
public final class LazyList<E > {
	private Set<E> added;
	private Set<E> removed;
	private Set<E> current;
	
	private Set<E> create(Set<E> set) {
		return set != null ? set : set();
	}
	Set<E> getAdded(boolean create) {
		return added = create(added);
	}
	Set<E> getRemoved(boolean create) {
		return removed = create(removed);
	}
	Set<E> getCurrent(boolean create) {
		return current = create(current);
	}
	public void add(E e) {
		if(contains(e, added) || contains(e, current))
			return;

		added = add(added, e);
		remove(removed, e);
		remove(current, e);
	}
	private void remove(Set<E> set, E e) {
		if(set != null)
			set.remove(e);
	}
	private Set<E> add(Set<E> set, E e) {
		if(set == null)
			set = set();
		
		set.add(e);
		return set;
	}
	private boolean contains(E e, Set<E> set) {
		return isNotEmpty(set) && set.contains(e);
	}
	private Set<E> set() {
		return new TreeSet<E>();
	}
	public void remove(E e) {
		if(e == null) return;

		removed = add(removed, e);
		remove(added, e);
		remove(current, e);
	}
	public boolean isModified() {
		return isNotEmpty(added) || isNotEmpty(removed); 
	}
	public void forEach(Consumer<E> action) {
		if(current != null) {
			for (E e : current) 
				action.accept(e);
		}
		if(added != null) {
			for (E e : added) 
				action.accept(e);
		}
	}
	public Stream<E> stream() {
		return Stream.of(current, added).filter(Objects::nonNull).flatMap(Set::stream);
	}
}
