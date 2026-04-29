package umu.pds.app;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
//import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines( "cucumber" )
@SelectClasspathResource( "features" ) // Busca en src/test/resources/features las features.
@ConfigurationParameter( key = GLUE_PROPERTY_NAME, value = "umu.pds.app.steps" ) // Donde estan los steps
// @ConfigurationParameter( key = PLUGIN_PROPERTY_NAME, value = "html:target/cucumber.html,json:target/cucumber.json,junit:target/cucumber.xml,com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:" )
public class Runner {
	// Esta clase se deja vacía
}