package org.f0w.k2i.core.exchange.finder;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import org.f0w.k2i.core.model.entity.Movie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.f0w.k2i.core.util.HttpUtils.buildURL;
import static org.f0w.k2i.core.util.MovieUtils.*;

final class XMLMovieFinder extends AbstractMovieFinder {
    public XMLMovieFinder(Config config) {
        super(config);
    }

    /** {@inheritDoc} */
    @Override
    protected String buildSearchQuery(Movie movie) {
        String movieSearchLink = "http://www.imdb.com/xml/find?";

        Map<String, String> query = new ImmutableMap.Builder<String, String>()
                .put("q", movie.getTitle())
                .put("tt", "on")
                .put("nr", "1")
                .build();

        return buildURL(movieSearchLink, query);
    }

    /** {@inheritDoc} */
    @Override
    protected List<Movie> parseSearchResult(String result) {
        Document document = Jsoup.parse(result);

        return document.getElementsByTag("ImdbEntity")
                .stream()
                .map(e -> new Movie(
                        parseTitle(e.ownText()),
                        parseYear(
                                Optional.ofNullable(e.getElementsByTag("Description").first())
                                        .map(Element::text)
                                        .orElse(null)
                        ),
                        parseIMDBId(e.attr("id"))
                ))
                .collect(Collectors.toList());
    }
}
