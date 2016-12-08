package org.unidal.cat;

/**
 * Environment related settings for user to customize the CAT client and/or server.
 *
 * @author Frankie Wu (qmwu2000@gmail.com)
 */
public interface CatConstant {
   String CAT = "Cat";

   /**
    * Default CAT home directory if no CAT home specified by <code>-Dcat.home=...</code> in JVM arguments or no environment variable
    * <code>CAT_HOME=...</code> is set.
    */
   String DEFAULT_CAT_HOME = "/data/appdatas/cat";

   /**
    * CAT home directory.
    * <p>
    * 
    * Following files will be seeked under the CAT home:
    * <li>client.xml (Deprecated. Use resource /META-INF/app.properties instead)</li>
    */
   String ENV_CAT_HOME = "CAT_HOME";

   /**
    * CAT servers list set by environment variable <code>CAT_SERVERS</code>.
    */
   String ENV_CAT_SERVERS = "CAT_SERVERS";

   /**
    * Legacy client configuration file.
    * <p>
    * 
    * Deprecated. Use resource /META-INF/app.properties instead
    */
   String FILE_CLIENT_XML = "client.xml";

   /**
    * CAT domain specified by system properties(<code>-Dcat.domain=MyApp</code>) in the JVM arguments.
    */
   String PROPERTY_CAT_DOMAIN = "cat.domain";

   /**
    * CAT home directory specified by system properties(<code>-Dcat.home=.</code>) in the JVM arguments.
    */
   String PROPERTY_CAT_HOME = "cat.home";

   /**
    * CAT servers addresses specified by system properties(<code>-Dcat.servers=1.2.3.4:8080,1.2.3.5:8080</code>) in the JVM
    * arguments.
    */
   String PROPERTY_CAT_SERVERS = "cat.servers";

   /**
    * Server mode set by code <code>System.setProperty(CatConstant.PROPERTY_SERVER_MODE, "true")</code>, to indicate CAT client
    * initialization is triggered during CAT server initialization.
    * <p>
    * 
    * Reserved for internal use only.
    */
   String PROPERTY_SERVER_MODE = "ServerMode";

   /**
    * Test mode set by code <code>System.setProperty(CatConstant.PROPERTY_TEST_MODE, "true")</code>, to avoid StatusUpdateTask and
    * its related threads starting up.
    * <p>
    * 
    * For unit test scenarios only.
    */
   String PROPERTY_TEST_MODE = "TestMode";
}
