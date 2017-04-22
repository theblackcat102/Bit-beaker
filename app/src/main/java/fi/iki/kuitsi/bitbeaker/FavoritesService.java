package fi.iki.kuitsi.bitbeaker;

import java.util.Collection;
import java.util.List;

import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

public interface FavoritesService {
	List<Repository> getFavorites();

	void saveFavoriteRepository(Repository repository);

	void removeFavoriteRepository(Repository repository);

	boolean isFavoriteRepository(Repository repository);

	void deleteFavorites();

	int addFavorites(Collection<Repository> repositories);
}
