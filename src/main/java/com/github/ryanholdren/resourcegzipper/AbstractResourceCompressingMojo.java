package com.github.ryanholdren.resourcegzipper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import static java.nio.file.Files.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.meteogroup.jbrotli.Brotli;
import static org.meteogroup.jbrotli.Brotli.*;
import org.meteogroup.jbrotli.Brotli.Mode;
import static org.meteogroup.jbrotli.Brotli.Mode.*;
import org.meteogroup.jbrotli.io.BrotliOutputStream;
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader;

public abstract class AbstractResourceCompressingMojo extends AbstractMojo {

	static {
		BrotliLibraryLoader.loadBrotli();
	}

	@Parameter(property="filter", defaultValue = "\\.(cs|j)s$")
	private String filter;

	@Parameter(property="compression", defaultValue = "SMALLEST")
	private CompressionLevel compression;

	private Log log;

	@Override
	public void setLog(Log log) {
		this.log = log;
	}

	protected abstract String getPathToResourceDirectory();

	private Path getResourceDirectory() throws MojoExecutionException {
		final String pathToDirectory = getPathToResourceDirectory();
		if (pathToDirectory == null) {
			throw new MojoExecutionException("No resource directory was not specified!");
		}
		final Path directory = Paths.get(pathToDirectory);
		if (notExists(directory)) {
			throw new MojoExecutionException("The specified resource directory, '" + directory + "', does not exist!");
		}
		if (isDirectory(directory)) {
			return directory;
		} else {
			throw new MojoExecutionException("The specified resource directory, '" + directory + "', is not a directory at all!");
		}
	}

	@Override
	public void execute() throws MojoExecutionException {
		final Path directory = getResourceDirectory();
		final Predicate<Path> predicate = getFilter();
		try (
			final Stream<Path> resources = walk(directory);
		) {
			resources.filter(predicate).forEach(resource -> {
				compressWithGZip(resource);
				compressWithBrotli(resource);
			});
		} catch (Exception exception) {
			throw new MojoExecutionException("Could not walk the specified resource directory, '" + directory + "'!", exception);
		}
	}

	private void compressWithGZip(Path resource) {
		new Compressor() {

			@Override
			protected String getFileExtension() {
				return "gz";
			}

			@Override
			protected String getNameOfCompressionAlgorithm() {
				return "GZip";
			}

			@Override
			protected OutputStream compressedOutputStream(OutputStream stream) throws IOException {
				return new GZIPOutputStreamWithCustomCompressionLevel(stream, getDeflaterLevel());
			}

		}.compress(resource);
	}

	private int getDeflaterLevel() {
		switch (compression) {
			case FASTEST:
				return 0;
			case BAlANCED:
				return 5;
			case SMALLEST:
				return 9;
			default:
				throw new IllegalStateException();
		}
	}

	private static final String[] TEXT_FILE_EXTENSION = {
		".js", ".css", ".svg"
	};

	private void compressWithBrotli(Path resource) {
		new Compressor() {

			@Override
			protected String getFileExtension() {
				return "br";
			}

			@Override
			protected String getNameOfCompressionAlgorithm() {
				return "Brotli";
			}

			@Override
			protected OutputStream compressedOutputStream(OutputStream stream) throws IOException {
				final Mode mode;
				if (isText(resource)) {
					mode = TEXT;
				} else if (isFont(resource)) {
					mode = FONT;
				} else {
					mode = GENERIC;
				}
				final Brotli.Parameter parameter = new Brotli.Parameter(
					mode, getBrotliLevel(), DEFAULT_LGWIN, DEFAULT_LGBLOCK
				);
				return new BrotliOutputStream(stream, parameter);
			}

			private boolean isText(Path resource) {
				for (String extension : TEXT_FILE_EXTENSION) {
					if (resource.endsWith(extension)) {
						return true;
					}
				}
				return false;
			}

			private boolean isFont(Path resource) {
				return resource.endsWith(".woff");
			}

		}.compress(resource);
	}

	public int getBrotliLevel() {
		switch (compression) {
			case FASTEST:
				return 0;
			case BAlANCED:
				return 6;
			case SMALLEST:
				return 11;
			default:
				throw new IllegalStateException();
		}
	}

	private abstract class Compressor {

		protected abstract String getFileExtension();
		protected abstract String getNameOfCompressionAlgorithm();
		protected abstract OutputStream compressedOutputStream(OutputStream stream) throws IOException;

		public void compress(Path resource) {
			final Path compressedResource = Paths.get(resource.toString() + '.' + getFileExtension());
			try {
				if (exists(compressedResource)) {
					final Instant lastModified = getLastModifiedTime(resource).toInstant();
					final Instant lastCompressed = getLastModifiedTime(compressedResource).toInstant();
					if (lastCompressed.isAfter(lastModified)) {
						log.info("Skipped compressing resource file with " + getNameOfCompressionAlgorithm() + " because it has not been modified: '" + resource + "'.");
						return;
					}
				}
				final byte[] contents = readAllBytes(resource);
				try (
					final OutputStream output = compressedOutputStream(
						new FileOutputStream(compressedResource.toFile())
					)
				) {
					output.write(contents);
					log.info("Compressed resource file with " + getNameOfCompressionAlgorithm() + ": '" + resource + "'.");
				}
			} catch (Exception exception) {
				try {
					deleteIfExists(compressedResource);
				} catch (Exception suppressed) {
					exception.addSuppressed(suppressed);
				}
				log.error("Unexpected error while compressing resource file with " + getNameOfCompressionAlgorithm() + ": '" + resource + "'.", exception);
			}
		}
	}

	private Predicate<Path> getFilter() {
		final Pattern pattern = compile(filter);
		return resource -> {
			final Matcher matcher = pattern.matcher(resource.toString());
			return matcher.find();
		};
	}

}
