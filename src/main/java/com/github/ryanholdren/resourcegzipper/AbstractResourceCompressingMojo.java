package com.github.ryanholdren.resourcegzipper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.meteogroup.jbrotli.Brotli;
import static org.meteogroup.jbrotli.Brotli.DEFAULT_LGBLOCK;
import static org.meteogroup.jbrotli.Brotli.DEFAULT_LGWIN;
import org.meteogroup.jbrotli.Brotli.Mode;
import static org.meteogroup.jbrotli.Brotli.Mode.FONT;
import static org.meteogroup.jbrotli.Brotli.Mode.GENERIC;
import static org.meteogroup.jbrotli.Brotli.Mode.TEXT;
import org.meteogroup.jbrotli.io.BrotliOutputStream;
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader;

public abstract class AbstractResourceCompressingMojo extends AbstractMojo {

	static {
		BrotliLibraryLoader.loadBrotli();
	}

	@Parameter(property="filter", defaultValue = "\\.(cs|j)s$")
	private String filter;

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
		if (Files.notExists(directory)) {
			throw new MojoExecutionException("The specified resource directory, '" + directory + "', does not exist!");
		}
		if (Files.isDirectory(directory)) {
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
			final Stream<Path> resources = Files.walk(directory);
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
				return new GZIPOutputStreamWithBestCompression(stream);
			}

		}.compress(resource);
	}

	private static final int MAX_BROTLI_QUALITY = 11;

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
					mode, MAX_BROTLI_QUALITY, DEFAULT_LGWIN, DEFAULT_LGBLOCK
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

	private abstract class Compressor {

		protected abstract String getFileExtension();
		protected abstract String getNameOfCompressionAlgorithm();
		protected abstract OutputStream compressedOutputStream(OutputStream stream) throws IOException;

		public void compress(Path resource) {
			final Path compressedResource = Paths.get(resource.toString() + '.' + getFileExtension());
			try {
				if (Files.exists(compressedResource)) {
					final Instant lastModified = Files.getLastModifiedTime(resource).toInstant();
					final Instant lastCompressed = Files.getLastModifiedTime(compressedResource).toInstant();
					if (lastCompressed.isAfter(lastModified)) {
						log.info("Skipped compressing resource file with " + getNameOfCompressionAlgorithm() + " because it has not been modified: '" + resource + "'.");
						return;
					}
				}
				try (
					final OutputStream output = compressedOutputStream(
						new FileOutputStream(compressedResource.toFile())
					)
				) {
					Files.copy(resource, output);
					log.info("Compressed resource file with " + getNameOfCompressionAlgorithm() + ": '" + resource + "'.");
				}
			} catch (Exception exception) {
				try {
					Files.deleteIfExists(compressedResource);
				} catch (Exception suppressed) {
					exception.addSuppressed(suppressed);
				}
				log.error("Unexpected error while compressing resource file with " + getNameOfCompressionAlgorithm() + ": '" + resource + "'.", exception);
			}
		}
	}

	private Predicate<Path> getFilter() {
		final Pattern pattern = Pattern.compile(filter);
		return resource -> {
			final Matcher matcher = pattern.matcher(resource.toString());
			return matcher.find();
		};
	}

}
