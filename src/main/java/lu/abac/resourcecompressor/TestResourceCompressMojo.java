package lu.abac.resourcecompressor;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "test-compress", defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES, threadSafe = true)
public class TestResourceCompressMojo extends AbstractResourceCompressingMojo {

	@Parameter(property="testDirectory", required=true)
	private String testDirectory;

	@Override
	protected String getPathToResourceDirectory() {
		return testDirectory;
	}

}
