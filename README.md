# ResourceGZipper

Simple Maven plugin to gzip JS & CSS files at build time. No other dependencies, other than Maven itself.

## Usage:
<plugin>
	<groupId>com.github.ryanholdren</groupId>
	<artifactId>resourcegzipper</artifactId>
	<version>0.0.1</version>
	<executions>
		<execution>
			<id>gzip-css</id>
			<goals>
				<goal>gzip</goal>
			</goals>
			<configuration>
				<resourcedir>${project.build.directory}/${project.build.finalName}/css</resourcedir>
			</configuration>
		</execution>
	</executions>
</plugin>

## License
Licensed under the MIT license.
