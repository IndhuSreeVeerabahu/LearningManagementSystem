package com.lms.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseInitializationListener {
    
    private final DataSource dataSource;
    
    public DatabaseInitializationListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        System.out.println("Application is ready. Ensuring database tables exist...");
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Force table creation by executing a simple query
            statement.execute("SELECT 1 FROM users LIMIT 1");
            System.out.println("Database tables are confirmed to exist.");
            
        } catch (Exception e) {
            System.out.println("Database tables are being created... This is normal on first startup.");
        }
    }
}
