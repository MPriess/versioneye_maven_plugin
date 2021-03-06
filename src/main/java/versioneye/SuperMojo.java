package versioneye;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import versioneye.dto.ProjectJsonResponse;
import versioneye.utils.PropertiesUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * The Mother of all Mojos!
 */
public class SuperMojo extends AbstractMojo {

    protected static final String propertiesFile = "versioneye.properties";

    @Component
    protected RepositorySystem system;

    @Parameter( defaultValue="${project}" )
    protected MavenProject project;

    @Parameter( defaultValue="${repositorySystemSession}" )
    protected RepositorySystemSession session;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}")
    protected List<RemoteRepository> repos;

    @Parameter( defaultValue = "${basedir}", property = "basedir", required = true)
    protected File projectDirectory;

    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    protected File outputDirectory;

    @Parameter( defaultValue = "${user.home}" )
    protected File homeDirectory;

    @Parameter( property = "baseUrl", defaultValue = "https://www.versioneye.com" )
    protected String baseUrl;

    @Parameter( property = "apiPath", defaultValue = "/api/v2" )
    protected String apiPath;

    @Parameter( property = "apiKey" )
    protected String apiKey;

    protected Properties properties = null;
    protected String propertiesPath = null;

    public void execute() throws MojoExecutionException, MojoFailureException {  }

    protected String fetchApiKey() throws Exception {
        if (apiKey != null && !apiKey.isEmpty() )
            return apiKey;
        Properties properties = fetchProperties();
        String key = properties.getProperty("api_key");
        if (key == null || key.isEmpty())
            throw new MojoExecutionException("versioneye.properties found but without an API Key! " +
                    "Read the instructions at https://github.com/versioneye/versioneye_maven_plugin");
        return key;
    }

    protected Properties fetchProperties() throws Exception {
        if (properties != null)
            return properties;
        String propertiesPath = getPropertiesPath();
        PropertiesUtils propertiesUtils = new PropertiesUtils();
        properties = propertiesUtils.readProperties(propertiesPath);
        return properties;
    }

    protected String getPropertiesPath() throws Exception {
        if (this.propertiesPath != null)
            return this.propertiesPath;
        String propertiesPath = projectDirectory + "/src/main/resources/" + propertiesFile;
        File file = new File(propertiesPath);
        if (!file.exists()){
            propertiesPath = homeDirectory + "/.m2/" + propertiesFile;
            file = new File(propertiesPath);
        }
        if (!file.exists())
            throw new MojoExecutionException(propertiesPath + " is missing! Read the instructions at " +
                    "https://github.com/versioneye/versioneye_maven_plugin");
        this.propertiesPath = propertiesPath;
        return propertiesPath;
    }

    protected void writeProperties(ProjectJsonResponse response) throws Exception {
        Properties properties = fetchProperties();
        properties.setProperty("project_key", response.getProject_key());
        properties.setProperty("project_id", response.getId());
        PropertiesUtils utils = new PropertiesUtils();
        utils.writeProperties(properties, getPropertiesPath());
    }

}
