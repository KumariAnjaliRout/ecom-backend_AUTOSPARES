package com.automobile.ecom.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        String credentials = System.getenv("FIREBASE_CREDENTIALS");
        if (credentials == null) {
            throw new RuntimeException("FIREBASE_CREDENTIALS env variable not set");
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(credentials.getBytes());
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}