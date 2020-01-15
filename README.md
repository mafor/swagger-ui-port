An example of serving SpringFox' swagger-ui on a separate port.

## Step 1. Adding an additional Tomcat connector

To add a port to the embedded server an additional connector needs to be configured.
We will do it by providing custom WebServerFactoryCustomizer:
```java
@Component
public class TomcatContainerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${swagger.port}")
    private int swaggerPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {

        Connector swaggerConnector = new Connector();
        swaggerConnector.setPort(swaggerPort);
        factory.addAdditionalTomcatConnectors(swaggerConnector);
    }
}
```
Now Tomcat listens on two ports but it serves the same content on both of them. We need to filter it.

## Step 2. Adding a filter

Adding a servlet filter is pretty stright-forward with a FilterRegistrationBean.
It can be created anywhere, I added it directly to the TomcatContainerCustomizer.
```java
@Component
public class TomcatContainerCustomizer  implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${swagger.port}")
    private int swaggerPort;

    @Value("${swagger.paths}")
    private List<String> swaggerPaths;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {

        Connector swaggerConnector = new Connector();
        swaggerConnector.setPort(swaggerPort);
        factory.addAdditionalTomcatConnectors(swaggerConnector);
    }

    @Bean
    public FilterRegistrationBean<SwaggerFilter> swaggerFilterRegistrationBean() {

        FilterRegistrationBean<SwaggerFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SwaggerFilter());
        filterRegistrationBean.setOrder(-100);
        filterRegistrationBean.setName("SwaggerFilter");

        return filterRegistrationBean;
    }

    private class SwaggerFilter extends OncePerRequestFilter {

        private AntPathMatcher pathMatcher = new AntPathMatcher();

        @Override
        protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        FilterChain filterChain) throws ServletException, IOException {

            boolean isSwaggerPath = swaggerPaths.stream()
                    .anyMatch(path -> pathMatcher.match(path, httpServletRequest.getServletPath()));
            boolean isSwaggerPort = httpServletRequest.getLocalPort() == swaggerPort;

            if(isSwaggerPath == isSwaggerPort) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } else {
                httpServletResponse.sendError(404);
            }
        }
    }
}
```
The properties `swagger.port` and `swagger.paths` are configured in the application.yaml:
```yaml
server.port: 8080
swagger:
  port: 8088
  paths: |
    /swagger-ui.html,
    /webjars/springfox-swagger-ui/**/*,
    /swagger-resources,
    /swagger-resources/**/*,
    /v2/api-docs
```
So far so good: the swagger-ui is served on the port 8088, our api on the 8080.
But there is a problem: when we try to connect to the api from the swagger-ui,
the requests are sent to the 8088 instead of the 8080.

## Step 3. Adjusting SpringFox config.

Swagger assumes that the api runs on the same port as the swagger-ui.
We need to explicitly specify the port:
```java
@Value("${server.port}")
private int serverPort;

@Bean
public Docket docket() {
	return new Docket(DocumentationType.SWAGGER_2)
			.host("localhost:" + serverPort);
}
```

And the last problem: as the ui runs on a different port than the api,
the requests are considered cross-origin. We need to unblock them.
It can be done globally:
```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**/*").allowedOrigins("http://localhost:" + swaggerPort);
        }
    };
}
```
or by adding annotations to the controllers:
```java
@CrossOrigin(origins = "http://localhost:${swagger.port}")
```