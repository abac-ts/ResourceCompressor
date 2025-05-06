package com.github.ryanholdren.resourcegzipper;

import com.github.ryanholdren.resourcegzipper.compressors.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractResourceCompressingMojo extends AbstractMojo {

	@Parameter(property="filter", required = true)
	private String filter;

	@Parameter(property="compression", defaultValue = "SMALLEST")
	private CompressionLevel compression;

	@Parameter(property="enableGzip", defaultValue = "true")
	private boolean enableGzip;
    
	@Parameter(property="enableBrotli", defaultValue = "true")
	private boolean enableBrotli;
    
	@Parameter(property="textFileExtensions", defaultValue = ".js .css .svg")
	private String textFileExtensions;
    
	@Parameter(property="fontFileExtensions", defaultValue = ".woff")
	private String fontFileExtensions;
    

    private Set<String> parsedTextFileExtensions = null;
    private Set<String> parsedFontFileExtensions = null;
    

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
        if (!enableGzip && !enableBrotli) {
            throw new MojoExecutionException("Must enable at least one compression type");
        }
        
        final Path directory = getResourceDirectory();
		final Predicate<Path> predicate = getFilter();
		try (
			final Stream<Path> resources = Files.walk(directory);
		) {
			resources.filter(predicate).parallel().forEach(resource -> {
                if (enableGzip) {
                    compressWithGZip(resource);
                }
                if (enableBrotli) {
                    compressWithBrotli(resource);
                }
			});
		} catch (Exception exception) {
			throw new MojoExecutionException("Could not walk the specified resource directory, '" + directory + "'!", exception);
		}
	}

	private void compressWithGZip(Path resource) {
		new GzipCompressor(this).compress(resource, compression);
	}

	private void compressWithBrotli(Path resource) {
		new BrotliCompressor(this).compress(resource, compression);
	}

	private Predicate<Path> getFilter() {
		final Pattern pattern = Pattern.compile(filter);
		return resource -> {
			final Matcher matcher = pattern.matcher(resource.toString());
			return matcher.find();
		};
	}
    
    
    private static final Pattern SPLIT_SPACE_PATTERN = Pattern.compile("\\s+");

    private Set<String> getTextFileExtensions() {
        if (parsedTextFileExtensions == null) {
            String[] parts = SPLIT_SPACE_PATTERN.split(textFileExtensions.trim());
            parsedTextFileExtensions = new HashSet<>(Arrays.asList(parts));
        }
        return parsedTextFileExtensions;
    }

    public boolean isText(Path resource) {
        for (String extension : getTextFileExtensions()) {
            if (resource.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getFontFileExtensions() {
        if (parsedFontFileExtensions == null) {
            String[] parts = SPLIT_SPACE_PATTERN.split(fontFileExtensions.trim());
            parsedFontFileExtensions = new HashSet<>(Arrays.asList(parts));
        }
        return parsedFontFileExtensions;
    }

    public boolean isFont(Path resource) {
        for (String extension : getFontFileExtensions()) {
            if (resource.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    

}
