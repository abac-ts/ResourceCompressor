package com.github.ryanholdren.resourcegzipper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public abstract class Compressor {

    private AbstractResourceCompressingMojo mojo;

    protected abstract String getFileExtension();
    protected abstract String getNameOfCompressionAlgorithm();
    protected abstract OutputStream compressedOutputStream(Path inputResource, OutputStream outputStream,
            CompressionLevel level) throws IOException;

    public Compressor(AbstractResourceCompressingMojo mojo) {
        this.mojo = mojo;
    }
    
    public void compress(Path resource, CompressionLevel level) {
        final Path compressedResource = Paths.get(resource.toString() + '.' + getFileExtension());
        try {
            if (Files.exists(compressedResource)) {
                final Instant lastModified = Files.getLastModifiedTime(resource).toInstant();
                final Instant lastCompressed = Files.getLastModifiedTime(compressedResource).toInstant();
                if (lastCompressed.isAfter(lastModified)) {
                    mojo.getLog().info("Skipped compressing resource file with " + getNameOfCompressionAlgorithm()
                            + " because it has not been modified: '" + resource + "'.");
                    return;
                }
            }
            final byte[] contents = Files.readAllBytes(resource);
            try (
                final OutputStream output = compressedOutputStream(
                    resource,
                    new FileOutputStream(compressedResource.toFile()),
                    level
                )
            ) {
                output.write(contents);
                mojo.getLog().info("Compressed resource file with " + getNameOfCompressionAlgorithm() + ": '"
                        + resource + "'.");
            }
        } catch (Exception exception) {
            try {
                Files.deleteIfExists(compressedResource);
            } catch (Exception suppressed) {
                exception.addSuppressed(suppressed);
            }
            mojo.getLog().error("Unexpected error while compressing resource file with "
                    + getNameOfCompressionAlgorithm() + ": '" + resource + "'.", exception);
        }
    }
    
    protected AbstractResourceCompressingMojo getMojo() {
        return mojo;
    }
    
}

