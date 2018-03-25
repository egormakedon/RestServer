package com.epam.makedon.jersey.server.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class ConnectionPool {
    private static final String FATAL_NAME = "FATAL";
    private static final Marker FATAL = MarkerFactory.getMarker(FATAL_NAME);
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

    private static ConnectionPool instance;
    private static AtomicBoolean instanceCreated = new AtomicBoolean(false);
    private static ReentrantLock lock = new ReentrantLock();
    private static BlockingQueue<ProxyConnection> connectionQueue;
    private static final String DATABASE_PROPERTY_PATH = "/property/database.properties";

    private ConnectionPool() {
        if (instanceCreated.get()) {
            LOGGER.error(FATAL, "Tried to clone connection pool with reflection api");
            throw new RuntimeException("Tried to clone connection pool with reflection api");
        }
    }

    public static ConnectionPool getInstance() {
        if (!instanceCreated.get()) {
            lock.lock();
            try {
                if (!instanceCreated.get()) {
                    instance = new ConnectionPool();
                    registerJDBCDriver();
                    initPool();
                    instanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public ProxyConnection getConnection() {
        ProxyConnection proxyConnection = null;
        try {
            proxyConnection = connectionQueue.take();
        } catch (InterruptedException e) {
            LOGGER.error("take connection problem", e);
        }
        return proxyConnection;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        if (instanceCreated.get()) {
            LOGGER.error(FATAL, "Tried to clone connection pool");
            throw new CloneNotSupportedException("Tried to clone connection pool");
        }
        return super.clone();
    }

    void releaseConnection(ProxyConnection proxyConnection) {
        try {
            connectionQueue.put(proxyConnection);
        } catch (InterruptedException e) {
            LOGGER.error("put connection problem", e);
        }
    }

    private static void registerJDBCDriver() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        } catch (SQLException e) {
            LOGGER.error(FATAL, "Mysql jdbc driver hasn't loaded");
            throw new RuntimeException();
        }
    }

    private static void initPool() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(DATABASE_PROPERTY_PATH);
        if (url == null) {
            LOGGER.error(FATAL, "database property hasn't found");
            throw new RuntimeException();
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(url.toURI())));
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("", e);
        }

        String urlDB = properties.getProperty("database.url");
        int poolSize = Integer.parseInt(properties.getProperty("database.poolSize"));

        Properties propDB = new Properties();
        propDB.put("user", properties.getProperty("database.user"));
        propDB.put("password", properties.getProperty("database.password"));
        propDB.put("characterEncoding", properties.getProperty("database.characterEncoding"));
        propDB.put("useUnicode", properties.getProperty("database.useUnicode"));

        connectionQueue = new ArrayBlockingQueue<ProxyConnection>(poolSize);
        for (int index = 0; index < poolSize; index++) {
            Connection connection;
            try {
                connection = DriverManager.getConnection(urlDB, propDB);
            } catch (SQLException e) {
                LOGGER.error(FATAL, "Hasn't found connection with database");
                throw new RuntimeException();
            }

            ProxyConnection proxyConnection = new ProxyConnection(connection);
            try {
                connectionQueue.put(proxyConnection);
            } catch (InterruptedException e) {
                LOGGER.error("", e);
            }
        }
    }
}