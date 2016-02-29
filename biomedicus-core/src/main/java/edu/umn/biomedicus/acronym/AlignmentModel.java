package edu.umn.biomedicus.acronym;

import java.io.*;
import java.util.*;

/**
 * Will attempt to determine a long form candidate for an unknown abbreviation by a sequence matching algorithm
 * Uses a modified Needleman-Wunsch algorithm, with parameters chosen with abbreviations/acronyms in mind
 *
 * Created by gpfinley on 12/14/15.
 */
public class AlignmentModel implements Serializable {

    private Set<String> longforms;

    // Defaults to false, which is the recommended value
    private boolean caseSensitive;

    /**
     * Load longforms that will be used by the aligner
     * @param longformsFilename the filename of longforms, each on its own line (no bars or other delimiters)
     * @throws IOException
     */
    public AlignmentModel(String longformsFilename, boolean caseSensitive) throws IOException {
        longforms = new HashSet<>();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(longformsFilename);
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream));
        String nextLine;
        while ((nextLine = fileReader.readLine()) != null) {
            longforms.add(nextLine);
        }
        this.caseSensitive = caseSensitive;
    }

    public AlignmentModel(String longformsFilename) throws IOException {
        this(longformsFilename, false);
    }

    /**
     * Calculate the score of an optimal alignment using the Needleman-Wunsch algorithm
     * @param abbr the abbreviation to align
     * @param longform the longform to align
     * @return the score of the match
     */
    public double align(String abbr, String longform) {

        if(!caseSensitive) {
            abbr = abbr.toLowerCase();
            longform = longform.toLowerCase();
        }

        // Scores and penalties:
        // For matching a character between abbreviation and longform
        double match = 1;
        // For matching after a space or hyphen in the longform
        double wordInitialMatch = 3;
        // For matching at the beginning of both strings (highly preferred)
        double initialMatch = 5;
        // For deleting a character from the abbreviation (abbrevs will rarely have additional characters)
        double delAbbr = -3;
        // For deleting a character from the long form (happens all the time)
        double delLong = 0;
        // For deleting a word-initial character from a long form (happens less often)
        double delLongWordInitial = -2;

        List<Character> breakingChars = new ArrayList<>();
        Collections.addAll(breakingChars, ' ' , '-', '/');

        int m = abbr.length();
        int n = longform.length();
        double[][] matrix = new double[m+1][n+1];

        for(int i=0; i<=m; i++) {

            for(int j=0; j<=n; j++) {

                double max=-Double.MAX_VALUE;
                if(i==0 && j==0)
                    max = 0;

                if(i>0 && j>0) {
                    // Don't even consider mismatches (departure from standard Needleman-Wunsch)
                    if(abbr.charAt(i-1) == longform.charAt(j-1)) {
                        double diagScore = matrix[i-1][j-1];
                        if(j==1 && i==1) {
                            diagScore += initialMatch;
                        }
                        else if(j<2 || breakingChars.contains(longform.charAt(j-2)) ) {
                            diagScore += wordInitialMatch;
                        }
                        else {
                            diagScore += match;
                        }
                        if (diagScore > max)
                            max = diagScore;
                    }
                }
                if(i>0) {
                    double downScore = matrix[i-1][j] + delAbbr;
                    if(downScore > max)
                        max = downScore;
                }
                if(j>0) {
                    double rightScore = matrix[i][j-1];
                    if(j<2 || breakingChars.contains(longform.charAt(j-2)) ) {
                        rightScore += delLongWordInitial;
                    }
                    else {
                        rightScore += delLong;
                    }
                    if (rightScore > max)
                        max = rightScore;
                }
                matrix[i][j] = max;
            }
        }

        return matrix[m][n];
    }

    /**
     * Will return the highest-scoring longform by alignment
     * If there are ties, only the last one will be returned
     * @param abbrev the abbreviation to expand
     * @return the last longform with the highest score
     */
    public String findBestLongform(String abbrev) {
        String best = abbrev;
        double maxScore = -Double.MAX_VALUE;
        for(String longform : longforms) {
            double thisScore = align(abbrev, longform);
            if(thisScore > maxScore) {
                maxScore = thisScore;
                best = longform;
            }
        }
        return best;
    }

    /**
     * Will return the highest-scoring longforms by alignment
     * All tied candidates will be present in the List
     * @param abbrev the abbreviation to expand
     * @return all longforms with the highest score
     */
    public List<String> findBestLongforms(String abbrev) {
        List<String> best = new ArrayList<>();
        double maxScore = -Double.MAX_VALUE;
        for(String longform : longforms) {
            double thisScore = align(abbrev, longform);
            if(thisScore > maxScore) {
                maxScore = thisScore;
                best = new ArrayList<>();
            }
            if(thisScore == maxScore)
                best.add(longform);
        }
        return best;
    }

    /**
     * Get longforms ranked by their score
     * @param abbrev the abbreviation to expand
     * @return a TreeMap of longforms to their scores, ordered by score
     */
    public Map<String,Double> rankLongforms(String abbrev) {
        HashMap<String,Double> longformScores = new LinkedHashMap<>();
        for(String longform : longforms) {
            longformScores.put(longform, align(abbrev, longform));
        }
        TreeMap<String,Double> sortedScores = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int comparison = longformScores.get(o2).compareTo(longformScores.get(o1));
                if(comparison == 0)
                    comparison = o1.compareTo(o2);
                return comparison;
            }
        });
        sortedScores.putAll(longformScores);

        return sortedScores;
    }

}
