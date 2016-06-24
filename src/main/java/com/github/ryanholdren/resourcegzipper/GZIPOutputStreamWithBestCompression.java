package com.github.ryanholdren.resourcegzipper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamWithBestCompression extends GZIPOutputStream {
	public GZIPOutputStreamWithBestCompression(OutputStream out) throws IOException {
		super(out);
		def.setLevel(Deflater.BEST_COMPRESSION);
	}
}
