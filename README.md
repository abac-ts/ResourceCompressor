# ResourceCompressor

A simple Maven plugin for compressing static assets (e.g. CSS and JavaScript files) at build time using GZip and Brotli.

## Usage:
```
<plugin>
	<groupId>com.github.ryanholdren</groupId>
	<artifactId>resourcecompressor</artifactId>
	<version>2016-07-15</version>
	<executions>
		<execution>
			<goals>
				<goal>compress</goal>
			</goals>
			<configuration>
				<directory>${project.build.directory}/${project.build.finalName}/css</directory>
				<filter>\.css$</filter>
			</configuration>
		</execution>
	</executions>
</plugin>
```
