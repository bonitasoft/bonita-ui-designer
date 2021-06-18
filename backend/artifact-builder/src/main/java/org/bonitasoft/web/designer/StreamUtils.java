package org.bonitasoft.web.designer;

import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public final class StreamUtils {
    private StreamUtils() {
        // Utility class
    }

    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        return stream(iterable.spliterator(), false);
    }

}
