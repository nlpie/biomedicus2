/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.syntaxnet;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.umn.biomedicus.exc.BiomedicusException;

@Singleton
public class SyntaxnetParserService {
    private final Provider<ParserEval> parserEvalProvider;

    private final ThreadLocal<ParserEval> threadParserEvals = new ThreadLocal<>();

    @Inject
    public SyntaxnetParserService(Provider<ParserEval> parserEvalProvider) {
        this.parserEvalProvider = parserEvalProvider;
    }

    public ParserEval getParser() throws BiomedicusException {
        ParserEval parserEval = threadParserEvals.get();
        if (parserEval == null) {
            parserEval = parserEvalProvider.get();
            parserEval.start();
            threadParserEvals.set(parserEval);
        }
        return parserEval;
    }
}
