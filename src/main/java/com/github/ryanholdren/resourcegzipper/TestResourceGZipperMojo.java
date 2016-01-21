package com.github.ryanholdren.resourcegzipper;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "test-gzip", defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES, threadSafe = true)
public class TestResourceGZipperMojo extends AbstractResourceGZipperMojo {

	@Parameter(property="testDirectory", required=true)
	private String testDirectory;

	@Override
	protected String getPathToResourceDirectory() {
		return testDirectory;
	}
	
}
