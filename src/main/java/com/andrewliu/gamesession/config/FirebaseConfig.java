package com.andrewliu.gamesession.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "firestore")
public class FirebaseConfig {

  @Bean
  Firestore firestore(AppProperties properties) throws IOException {
    FirebaseApp app = FirebaseApp.getApps().stream()
        .filter(existing -> FirebaseApp.DEFAULT_APP_NAME.equals(existing.getName()))
        .findFirst()
        .orElseGet(() -> initialize(properties));
    return FirestoreClient.getFirestore(app);
  }

  private FirebaseApp initialize(AppProperties properties) {
    try {
      FirebaseOptions.Builder builder = FirebaseOptions.builder()
          .setCredentials(credentials(properties));
      if (StringUtils.hasText(properties.getFirebase().getProjectId())) {
        builder.setProjectId(properties.getFirebase().getProjectId());
      }
      return FirebaseApp.initializeApp(builder.build());
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to initialize Firebase", ex);
    }
  }

  private GoogleCredentials credentials(AppProperties properties) throws IOException {
    String serviceAccountJson = properties.getFirebase().getServiceAccountJson();
    if (StringUtils.hasText(serviceAccountJson)) {
      return GoogleCredentials.fromStream(
          new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
    }
    return GoogleCredentials.getApplicationDefault();
  }
}
