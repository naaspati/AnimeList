package sam.anime.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import sam.anime.dao.ModificationListener.Type;


abstract class CustomCollection<E, C extends Collection<E>> implements Iterable<E> {
	protected C data;
	protected Collection<E> unmodifiable;
	@SuppressWarnings("rawtypes")
	protected ModificationListener[] listeners; 
	protected Anime anime;
	private Supplier<C> dataGetter;

	public CustomCollection(Supplier<C> dataGetter, Anime anime) {
		this.dataGetter = dataGetter;
		this.anime = anime;
	}
	public CustomCollection(C data, Anime anime) {
		this.anime = anime;
		this.data = data;
		this.unmodifiable = Collections.unmodifiableCollection(data);
	}

	public void addListener(ModificationListener<E> listener) {
		Objects.requireNonNull(listener);

		if(listeners == null) {
			listeners = new ModificationListener[] {listener};
		} else {
			for (int i = 0; i < listeners.length; i++) {
				if(listeners[i] == null) {
					listeners[i] = listener;
					return ;
				}
			}
			listeners = Arrays.copyOf(listeners, listeners.length+1);
			listeners[listeners.length - 1] = listener;
		}
	}
	public boolean removeListener(ModificationListener<E> listener) {
		if(listener == null || listeners == null) return false;
		for (int i = 0; i < listeners.length; i++) {
			if(listeners[i] == listener) {
				listeners[i] = null;
				return true;
			}
		}
		return false;

	}
	private void load() {
		if(dataGetter == null) return;
		
		Supplier<C> d = dataGetter;
		dataGetter = null;
		this.data = d.get();
		this.unmodifiable = Collections.unmodifiableCollection(data);
	}
	public Collection<E> get() {
		load();
		return unmodifiable;
	}
	public boolean  remove(E e) {
		load();
		
		boolean b = data.remove(e);
		notifyModification(e, Type.REMOVED);
		return b;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void notifyModification(E e, Type type) {
		if(listeners != null) {
			for (ModificationListener m : listeners) {
				if(m != null)
					m.onModify(anime, e, type);				
			}
		}
	}
	public boolean add(E e) {
		load();
		
		boolean b = data.add(e);
		notifyModification(e, Type.ADDED);
		return b;
	}
	public boolean isEmpty() {
		load();
		return data.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		load();
		return data.iterator();
	}
	public Stream<E> stream() {
		load();
		return data.stream();
	}
	public int size() {
		load();
		return data.size();
	}

}
