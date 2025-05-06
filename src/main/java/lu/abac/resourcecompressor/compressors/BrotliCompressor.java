package lu.abac.resourcecompressor.compressors;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import static com.aayushatharva.brotli4j.encoder.Encoder.Mode;
import lu.abac.resourcecompressor.AbstractResourceCompressingMojo;
import lu.abac.resourcecompressor.CompressionLevel;
import lu.abac.resourcecompressor.Compressor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;


public class BrotliCompressor extends Compressor {

	static {
		Brotli4jLoader.ensureAvailability();
	}

    public BrotliCompressor(AbstractResourceCompressingMojo mojo) {
        super(mojo);
    }

    @Override
    protected String getFileExtension() {
        return "br";
    }

    @Override
    protected String getNameOfCompressionAlgorithm() {
        return "Brotli";
    }

    @Override
    protected OutputStream compressedOutputStream(Path inputResource, OutputStream outputStream,
            CompressionLevel level) throws IOException {
        
        final Mode mode;
        if (getMojo().isText(inputResource)) {
            mode = Mode.TEXT;
        } else if (getMojo().isFont(inputResource)) {
            mode = Mode.FONT;
        } else {
            mode = Mode.GENERIC;
        }
        
        final Encoder.Parameters parameters = new Encoder.Parameters().setQuality(getBrotliLevel(level)).setMode(mode);
        return new BrotliOutputStream(outputStream, parameters);
    }

	public int getBrotliLevel(CompressionLevel level) {
		switch (level) {
			case FASTEST:
				return 0;
			case BALANCED:
				return 6;
			case SMALLEST:
				return 11;
			default:
				throw new IllegalStateException();
		}
	}

}
