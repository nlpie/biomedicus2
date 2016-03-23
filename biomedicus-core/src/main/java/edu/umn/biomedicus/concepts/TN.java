package edu.umn.biomedicus.concepts;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class TN {
    public static final Pattern TN_PATTERN = Pattern.compile("([A-Z])(([1-9]\\.)*[1-9])?");

    private final char category;

    private final byte[] number;

    public TN(String tnString) {
        Matcher matcher = TN_PATTERN.matcher(tnString);
        if (matcher.find()) {
            category = matcher.group(1).charAt(0);
            String group = matcher.group(2);
            List<Byte> bytes;
            if (group == null || group.isEmpty()) {
                bytes = Collections.emptyList();
            } else {
                bytes = Arrays.stream(group.split("\\."))
                        .map(Byte::parseByte)
                        .collect(Collectors.toList());
            }
            number = new byte[bytes.size()];
            for (int i = 0; i < number.length; i++) {
                number[i] = bytes.get(i);
            }
        } else {
            throw new IllegalArgumentException("String is not a semantic network RTN or STN: " + tnString);
        }
    }

    public boolean isA(TN other) {
        if (category != other.category) {
            return false;
        }

        if (other.number.length > number.length) {
            return false;
        }

        for (int i = 0; i < other.number.length; i++) {
            if (number[i] != other.number[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TN tn = (TN) o;

        if (category != tn.category) return false;
        return Arrays.equals(number, tn.number);
    }

    @Override
    public int hashCode() {
        int result = (int) category;
        result = 31 * result + Arrays.hashCode(number);
        return result;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(".", "" + category, "");
        for (byte aNumber : number) {
            joiner.add(Byte.toString(aNumber));
        }
        return joiner.toString();
    }
}
