package com.github.ryanholdren.resourcegzipper.compressors;

import com.github.ryanholdren.resourcegzipper.AbstractResourceCompressingMojo;
import com.github.ryanholdren.resourcegzipper.CompressionLevel;
import com.github.ryanholdren.resourcegzipper.Compressor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

public class GzipCompressor extends Compressor {

    public GzipCompressor(AbstractResourceCompressingMojo mojo) {
        super(mojo);
    }

    @Override
    protected String getFileExtension() {
        return "gz";
    }

    @Override
    protected String getNameOfCompressionAlgorithm() {
        return "GZip";
    }

    @Override
    protected OutputStream compressedOutputStream(Path inputResource, OutputStream outputStream,
            CompressionLevel level) throws IOException {
        return new GZIPOutputStreamWithCustomCompressionLevel(outputStream, getDeflaterLevel(level));
    }

    private int getDeflaterLevel(CompressionLevel level) {
		switch (level) {
			case FASTEST:
				return 0;
			case BALANCED:
				return 5;
			case SMALLEST:
				return 9;
			default:
				throw new IllegalStateException();
		}
	}

    
    public static class GZIPOutputStreamWithCustomCompressionLevel extends GZIPOutputStream {
        
        public GZIPOutputStreamWithCustomCompressionLevel(OutputStream out, int level) throws IOException {
            super(out);
            def.setLevel(level);
        }
        
    }

}
