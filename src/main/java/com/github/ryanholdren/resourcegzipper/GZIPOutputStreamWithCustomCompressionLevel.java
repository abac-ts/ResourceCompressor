package com.github.ryanholdren.resourcegzipper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamWithCustomCompressionLevel extends GZIPOutputStream {
	public GZIPOutputStreamWithCustomCompressionLevel(OutputStream out, int level) throws IOException {
		super(out);
		def.setLevel(level);
	}
}
