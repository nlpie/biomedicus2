package edu.umn.biomedicus.tools;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;
import edu.umn.biomedicus.common.terms.DAWGMap;
import edu.umn.biomedicus.common.terms.DirectedAcyclicWordGraph;
import edu.umn.biomedicus.common.terms.MappedCharacterSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
@VmOptions("-XX:-TieredCompilation")
public class DAWGBenchmark {
    @Param private Implementation implementation;

    private static final int STRINGS = 1_000_000;

    private static final int TIMES = 10_000;

    private static final MappedCharacterSet CHARACTER_SET = MappedCharacterSet.builder()
            .addAll(IntStream.range('a', 'z').mapToObj(c -> Character.toString((char) c)).collect(Collectors.joining()))
            .build();

    private final Random random = new Random();

    private Collection<String> strings = new HashSet<>();

    {
        for (int i = 0; i < STRINGS; i++) {
            int size = random.nextInt(12);
            StringBuilder stringBuilder = new StringBuilder(size);
            for (int j = 0; j < size; j++) {
                stringBuilder.append((char)((int)'a' + random.nextInt(25)));
            }
            strings.add(stringBuilder.toString());
        }
    }

    private Map<CharSequence, Integer> map;

    @BeforeExperiment void setUp() {
        map = implementation.create(strings);
    }

    @Benchmark void contains() {
        Iterator<String> iterator = strings.iterator();
        for (int i = 0; i < TIMES && iterator.hasNext(); i++) {
            String next = iterator.next();
            map.containsKey(next);
        }
    }

    @Benchmark void get() {
        Iterator<String> iterator = strings.iterator();
        for (int i = 0; i < TIMES && iterator.hasNext(); i++) {
            map.get(iterator.next());
        }
    }

    public static void main(String[] args) {
        CaliperMain.main(DAWGBenchmark.class, args);
    }

    public enum Implementation {
        DAWG {
            @Override
            Map<CharSequence, Integer> create(Collection<String> strings) {
                return new DAWGMap(DirectedAcyclicWordGraph.builder(CHARACTER_SET).addAll(strings).build());
            }
        },
        HASHMAP {
            @Override
            Map<CharSequence, Integer> create(Collection<String> strings) {
                Map<CharSequence, Integer> hashMap = new HashMap<>();
                int count = 0;
                for (String string : strings) {
                    hashMap.put(string, count++);
                }
                return hashMap;
            }
        },
        TREEMAP {
            @Override
            Map<CharSequence, Integer> create(Collection<String> strings) {
                Map<CharSequence, Integer> hashMap = new TreeMap<>();
                int count = 0;
                for (String string : strings) {
                    hashMap.put(string, count++);
                }
                return hashMap;
            }
        };

        abstract Map<CharSequence, Integer> create(Collection<String> strings);
    }
}
