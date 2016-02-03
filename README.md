# ResourceGZipper

A simple Maven plugin for compressing static assets (e.g. CSS and JavaScript files) at build time.

## Usage:
```
<plugin>
	<groupId>com.github.ryanholdren</groupId>
	<artifactId>resourcegzipper</artifactId>
	<version>2016-02-02</version>
	<executions>
		<execution>
			<id>gzip-css</id>
			<goals>
				<goal>gzip</goal>
			</goals>
			<configuration>
				<directory>${project.build.directory}/${project.build.finalName}/css</directory>
				<filter>\.css$</filter>
			</configuration>
		</execution>
	</executions>
</plugin>
```
