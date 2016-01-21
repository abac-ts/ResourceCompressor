# ResourceGZipper

A simple Maven plugin for compressing static assets (e.g. CSS and JavaScript files) at build time.

## Usage:
```
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
```
