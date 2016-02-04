package org.snia.cdmiserver;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import static com.jayway.jsonpath.Option.*;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;


/**
 * Useful matchers to verify the response from the CDMI server.
 */
public class Matchers
{
    private Matchers()
    {
        // private constructor to prevent initialisation.
    }

    private static abstract class AbstractJsonSingleValueMatcher extends TypeSafeMatcher<HttpEntity>
    {
        Configuration config = Configuration.defaultConfiguration().
                    addOptions(ALWAYS_RETURN_LIST, SUPPRESS_EXCEPTIONS);

        private final String path;

        AbstractJsonSingleValueMatcher(String path)
        {
            this.path = path;
        }

        @Override
        public boolean matchesSafely(HttpEntity entity)
        {
            List found;
            try {
                found = JsonPath.using(config).parse(entity.getContent()).read(path);
            } catch (IOException e) {
                return false;
            }

            return found.size() == 1 && matchesExpectedValue(found.get(0));
        }

        abstract boolean matchesExpectedValue(Object found);

        @Override
        public void describeTo(Description d)
        {
            d.appendText(" at " + path);
        }
    }


    public static Matcher<StatusLine> hasStatusCode(final int code)
    {
        return new TypeSafeMatcher<StatusLine>() {
            @Override
            public boolean matchesSafely(StatusLine status)
            {
                return status.getStatusCode() == code;
            }

            @Override
            public void describeTo(Description d)
            {
                d.appendText("status code " + code);
            }
        };
    }

    public static Matcher<Header[]> hasHeader(final String header, final String expectedValue)
    {
        return new TypeSafeMatcher<Header[]>() {
            @Override
            public boolean matchesSafely(Header[] headers)
            {
                Header found = null;

                for (Header h : headers) {
                    if (h.getName().equals(header)) {
                        if (found == null) {
                            found = h;
                        } else {
                            return false;
                        }
                    }
                }

                String actualValue = found == null ? null : found.getValue();
                return actualValue != null && actualValue.equals(expectedValue);
            }

            @Override
            public void describeTo(Description d)
            {
                d.appendText("Response header " + header + ": " + expectedValue);
            }
        };
    }

    public static Matcher<Header[]> hasHeader(final String header, final URI location)
    {
        return hasHeader(header, location.toASCIIString());
    }

    public static class JsonMatcherBuilder
    {
        private final String path;

        public JsonMatcherBuilder(String path)
        {
            this.path = path;
        }

        public Matcher<HttpEntity> of(final String expectedValue)
        {
            return new AbstractJsonSingleValueMatcher(path) {
                @Override
                public boolean matchesExpectedValue(Object value)
                {
                    return value != null && value.equals(expectedValue);
                }

                @Override
                public void describeTo(Description d)
                {
                    d.appendText("JSON with single String value \"" + expectedValue + "\"");
                    super.describeTo(d);
                }
            };
        }

        public Matcher<HttpEntity> of(final int expectedValue)
        {
            return new AbstractJsonSingleValueMatcher(path) {
                @Override
                public boolean matchesExpectedValue(Object value)
                {
                    return value instanceof Integer && ((Integer)value) == expectedValue;
                }

                @Override
                public void describeTo(Description d)
                {
                    d.appendText("JSON with single numerical value \"" + expectedValue + "\"");
                    super.describeTo(d);
                }
            };
        }
    }

    public static JsonMatcherBuilder hasJsonValueAt(String path)
    {
        return new JsonMatcherBuilder(path);
    }

    private static class JsonTypeMatcher extends AbstractJsonSingleValueMatcher
    {
        private final Class type;

        JsonTypeMatcher(String path, Class type)
        {
            super(path);
            this.type = type;
        }

        @Override
        public boolean matchesExpectedValue(Object value)
        {
            return value != null && type.isAssignableFrom(value.getClass());
        }


        @Override
        public void describeTo(Description d)
        {
            String jsonType;

            if (type == String.class) {
                jsonType = "String";
            } else if (type == List.class) {
                jsonType = "List";
            } else {
                jsonType = "Object";
            }
            d.appendText("JSON with ").appendText(jsonType).appendText(" value");
            super.describeTo(d);
        }
    }

    private static class JsonEmptyTypeMatcher extends AbstractJsonSingleValueMatcher
    {
        private final Class type;

        JsonEmptyTypeMatcher(String path, Class type)
        {
            super(path);
            this.type = type;
        }

        @Override
        public boolean matchesExpectedValue(Object value)
        {
            if (value instanceof List) {
                return ((List)value).isEmpty();
            } else if (value instanceof Map) {
                return ((Map)value).isEmpty();
            }
            return false;
        }

        @Override
        public void describeTo(Description d)
        {
            String jsonType;

            if (type == List.class) {
                jsonType = "List";
            } else {
                jsonType = "Object";
            }
            d.appendText("JSON with empty ").appendText(jsonType).appendText(" value");
            super.describeTo(d);
        }
    }

    public static Matcher<HttpEntity> hasJsonWithNullAt(final String path)
    {
        return new AbstractJsonSingleValueMatcher(path) {
            @Override
            public boolean matchesExpectedValue(Object value)
            {
                return value == null;
            }

            @Override
            public void describeTo(Description d)
            {
                d.appendText("JSON that has null");
                super.describeTo(d);
            }
        };
    }

    public static Matcher<HttpEntity> hasJsonStringAt(String path)
    {
        return new JsonTypeMatcher(path, String.class);
    }

    // FIXME: a String value is mistakenly identified as a List
    public static Matcher<HttpEntity> hasJsonListAt(String path)
    {
        return new JsonTypeMatcher(path, List.class);
    }

    public static Matcher<HttpEntity> hasJsonObjectAt(String path)
    {
        return new JsonTypeMatcher(path, Map.class);
    }

    // FIXME: a String value is mistakenly identified as a List
    public static Matcher<HttpEntity> hasJsonEmptyListAt(String path)
    {
        return new JsonEmptyTypeMatcher(path, List.class);
    }

    public static Matcher<HttpEntity> hasJsonEmptyObjectAt(String path)
    {
        return new JsonEmptyTypeMatcher(path, Map.class);
    }

}
