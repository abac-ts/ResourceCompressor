package com.github.ryanholdren.resourcegzipper;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "gzip", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class ResourceGZipperMojo extends AbstractResourceGZipperMojo {

	@Parameter(property="directory", required=true)
	private String directory;

	@Override
	protected String getPathToResourceDirectory() {
		return directory;
	}

}
