package com.andrewliu.gamesession.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private String adminApiKey = "dev-admin-key";
  private Duration heartbeatTimeout = Duration.ofSeconds(45);
  private Duration activeSessionTtl = Duration.ofSeconds(60);
  private Store store = new Store();
  private Persistence persistence = new Persistence();
  private Scheduler scheduler = new Scheduler();
  private Firebase firebase = new Firebase();

  public String getAdminApiKey() {
    return adminApiKey;
  }

  public void setAdminApiKey(String adminApiKey) {
    this.adminApiKey = adminApiKey;
  }

  public Duration getHeartbeatTimeout() {
    return heartbeatTimeout;
  }

  public void setHeartbeatTimeout(Duration heartbeatTimeout) {
    this.heartbeatTimeout = heartbeatTimeout;
  }

  public Duration getActiveSessionTtl() {
    return activeSessionTtl;
  }

  public void setActiveSessionTtl(Duration activeSessionTtl) {
    this.activeSessionTtl = activeSessionTtl;
  }

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }

  public Persistence getPersistence() {
    return persistence;
  }

  public void setPersistence(Persistence persistence) {
    this.persistence = persistence;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public Firebase getFirebase() {
    return firebase;
  }

  public void setFirebase(Firebase firebase) {
    this.firebase = firebase;
  }

  public static class Store {
    private String type = "memory";

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  public static class Persistence {
    private String type = "memory";

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  public static class Scheduler {
    private Duration queueDrainDelay = Duration.ofSeconds(5);
    private Duration expirationDelay = Duration.ofSeconds(5);

    public Duration getQueueDrainDelay() {
      return queueDrainDelay;
    }

    public void setQueueDrainDelay(Duration queueDrainDelay) {
      this.queueDrainDelay = queueDrainDelay;
    }

    public Duration getExpirationDelay() {
      return expirationDelay;
    }

    public void setExpirationDelay(Duration expirationDelay) {
      this.expirationDelay = expirationDelay;
    }
  }

  public static class Firebase {
    private String projectId;
    private String serviceAccountJson;

    public String getProjectId() {
      return projectId;
    }

    public void setProjectId(String projectId) {
      this.projectId = projectId;
    }

    public String getServiceAccountJson() {
      return serviceAccountJson;
    }

    public void setServiceAccountJson(String serviceAccountJson) {
      this.serviceAccountJson = serviceAccountJson;
    }
  }
}
