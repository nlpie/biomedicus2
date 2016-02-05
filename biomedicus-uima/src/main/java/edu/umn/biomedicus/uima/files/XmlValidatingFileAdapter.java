package edu.umn.biomedicus.uima.files;

import edu.umn.biomedicus.type.IllegalXmlCharacter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;

/**
 *
 */
public class XmlValidatingFileAdapter implements InputFileAdapter {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Date formatter for adding date to metadata.
     */
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG);

    /**
     * View to load data into.
     */
    private String viewName;

    private CharsetDecoder charsetDecoder;

    @Override
    public void initialize(UimaContext uimaContext, ProcessingResourceMetaData processingResourceMetaData) {
        LOGGER.info("Initializing xml validating file adapter.");

        charsetDecoder = StandardCharsets.ISO_8859_1.newDecoder();
    }

    @Override
    public void adaptFile(CAS cas, Path path) throws CollectionException, IOException {
        LOGGER.info("Reading text into a CAS view.");
        JCas defaultView;
        try {
            defaultView = cas.getJCas();
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        JCas targetView;
        try {
            targetView = defaultView.createView(viewName);
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        StringBuilder stringBuilder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        try (ReadableByteChannel readableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            byteBuffer.clear();
            while (readableByteChannel.read(byteBuffer) > 0) {
                CharBuffer charBuffer = charsetDecoder.decode(byteBuffer);
                charBuffer.rewind();

                while (charBuffer.hasRemaining()) {
                    char ch = charBuffer.get();
                    if (isValid(ch)) {
                        stringBuilder.append(ch);
                    } else {
                        LOGGER.warn("Encountered an illegal character: {}", ch);

                        int length = stringBuilder.length();
                        IllegalXmlCharacter illegalXmlCharacter = new IllegalXmlCharacter(targetView, length, length);
                        illegalXmlCharacter.setValue(ch);
                        illegalXmlCharacter.addToIndexes();
                    }
                }
            }
        }
    }

    @Override
    public void setTargetView(String viewName) {
        this.viewName = viewName;
    }

    private static boolean isValid(int ch) {
        return (ch >= 0x20 && ch <= 0xFF) || ch == '\n' || ch == '\t' || ch == '\r';
    }
}
