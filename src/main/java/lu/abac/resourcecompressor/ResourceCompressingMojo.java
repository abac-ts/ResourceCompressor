package lu.abac.resourcecompressor;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "compress", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class ResourceCompressingMojo extends AbstractResourceCompressingMojo {

	@Parameter(property="directory", required=true)
	private String directory;

	@Override
	protected String getPathToResourceDirectory() {
		return directory;
	}

}
