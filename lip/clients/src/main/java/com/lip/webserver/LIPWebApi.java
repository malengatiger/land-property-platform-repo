package com.lip.webserver;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.lip.webserver.util.WorkerBee;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.boot.WebApplicationType.SERVLET;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
public class LIPWebApi {
    /**
     * Starts our Spring Boot application.
     */
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(LIPWebApi.class);

    public static void main(String[] args) {
        logger.info(" \uD83D\uDD06  \uD83D\uDD06 LIPWebApi starting   \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06️");
        SpringApplication app = new SpringApplication(LIPWebApi.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(SERVLET);
        app.run(args);

        logger.info(" \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  LIPWebApi:  started ....  ❤️ \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                new Date().toString() + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A");
    }


    @Bean
    public CorsFilter corsFilter() {
        logger.info("\uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  LIPWebApi:  corsFilter started ??? .... \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        logger.info(" \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  corsFilter: config.getAllowCredentials: "
                + config.getAllowCredentials().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        logger.info(" \uD83D\uDC9A \uD83D\uDC99  \uD83D\uDC9A \uD83D\uDC99  corsFilter: config.getAllowedMethods: "
                + config.getAllowedMethods().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        logger.info(" \uD83D\uDC9A \uD83D\uDC99  \uD83D\uDC9A \uD83D\uDC99  corsFilter: config.getAllowedOrigins: "
                + config.getAllowedOrigins().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        logger.info(" \uD83D\uDC9A \uD83D\uDC99  \uD83D\uDC9A \uD83D\uDC99  corsFilter: config.getAllowedHeaders: "
                + config.getAllowedHeaders().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");

        return new CorsFilter(source);
    }

    @Autowired
    @Value("${firebasePath}")
    private String firebasePath;

    @Bean
    public FirebaseApp firebaseBean() throws Exception {
        logger.info("\uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  LIPWebApi:  setting up Firebase ...." +
                " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");

        try {
            logger.info(("\uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06 " +
                    "properties PATH for Firebase Service Account: \uD83D\uDC99  ").concat(firebasePath));
            FileInputStream serviceAccount =
                    new FileInputStream(firebasePath);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://land-platform.firebaseio.com")
                    .build();

            logger.info("\uD83D\uDE21 \uD83D\uDE21 Firebase project url: \uD83D\uDE21 "
                    .concat(options.getDatabaseUrl()));
            FirebaseApp app =FirebaseApp.initializeApp(options);
            logger.info(" \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 " +
                    "Firebase Admin Setup OK:  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 name: "
                            .concat(app.getName()));
        } catch (Exception e) {
            logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Firebase Admin setup failed");
            throw new Exception(" \uD83D\uDC7F  \uD83D\uDC7F unable to set Firebase up",e);
        }
        return null;
    }
}
