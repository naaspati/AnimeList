package sam.anime.dao;

@FunctionalInterface
public interface ModificationListener<E> {
	public static enum Type {
		ADDED, REMOVED
	}
	
	public void onModify(Anime anime, E item, Type type);

}
