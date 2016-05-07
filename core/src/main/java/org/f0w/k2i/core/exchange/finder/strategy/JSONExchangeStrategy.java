package org.f0w.k2i.core.exchange.finder.strategy;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import lombok.val;
import org.f0w.k2i.core.model.entity.Movie;
import org.f0w.k2i.core.util.HttpUtils;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class JSONExchangeStrategy implements ExchangeStrategy {
    /**
     * {@inheritDoc}
     */
    @Override
    public URL buildSearchURL(@NonNull final Movie movie) {
        val searchLink = "http://www.imdb.com/xml/find";
        val queryParams = new ImmutableMap.Builder<String, String>()
                .put("q", movie.getTitle())
                .put("tt", "on")
                .put("nr", "1")
                .put("json", "1")
                .build();

        return HttpUtils.buildURL(searchLink, queryParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Movie> parseSearchResult(@NonNull final String data) {
        if ("".equals(data)) {
            return Collections.emptyList();
        }

        JSONMovieParser movieParser = new JSONMovieParser();
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(data)
                .getAsJsonObject()
                .entrySet()
                .stream()
                .map(e -> e.getValue().getAsJsonArray())
                .flatMap(a -> StreamSupport.stream(a.spliterator(), false))
                .map(e -> movieParser.parse(
                        e.getAsJsonObject(),
                        t -> t.get("title"),
                        t -> t.get("description"),
                        t -> t.get("description"),
                        t -> t.get("id")
                ))
                .collect(Collectors.toList());
    }

    private static final class JSONMovieParser implements MovieParser<JsonObject, JsonElement, JsonElement, JsonElement, JsonElement> {
        @Override
        public String prepareTitle(JsonElement element) {
            return getStringOrNull(element);
        }

        @Override
        public String prepareYear(JsonElement element) {
            return getStringOrNull(element);
        }

        @Override
        public Movie.Type parseType(JsonElement element) {
            val stringValue = Optional.ofNullable(element).map(JsonElement::getAsString).orElse("");

            if (stringValue.contains("TV series")) {
                return Movie.Type.SERIES;
            } else if (stringValue.contains("documentary")) {
                return Movie.Type.DOCUMENTARY;
            } else if (stringValue.contains("short")) {
                return Movie.Type.SHORT;
            } else if (stringValue.contains("video game")) {
                return Movie.Type.VIDEO_GAME;
            }

            return Movie.Type.MOVIE;
        }

        @Override
        public String prepareImdbId(JsonElement element) {
            return getStringOrNull(element);
        }

        private String getStringOrNull(JsonElement element) {
            return Optional.ofNullable(element)
                    .map(JsonElement::getAsString)
                    .orElse(null);
        }
    }
}
