package com.epam.makedon.jersey.server.dao;

import com.epam.makedon.jersey.server.pojo.Article;
import com.epam.makedon.jersey.server.pojo.Author;
import com.epam.makedon.jersey.server.pojo.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Dao {
    private static final Marker FATAL = MarkerFactory.getMarker("FATAL");
    private static final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

    private static final Dao INSTANCE = new Dao();
    private Dao() {}
    public static Dao getInstance() {
        return INSTANCE;
    }

    private static final String SQL_UPDATE_ARTICLE = "UPDATE article SET body=? WHERE article_id=?";
    private static final String SQL_DELETE_ARTICLE = "DELETE FROM article WHERE article_id=?";
    private static final String SQL_ADD_ARTICLE = "INSERT INTO article(title,body,date,fk_author_id,fk_resource_id) VALUES(?,?,?,?,?)";
    private static final String SQL_SELECT_ALL_ARTICLES = "SELECT title FROM article";
    private static final String SQL_SELECT_ARTICLE_BY_TITLE = "SELECT article_id,title,body,date,fk_author_id,fk_resource_id," +
                        "author.name a_name,surname,country,resource.name r_name,url FROM article INNER JOIN author ON fk_author_id=author_id "  +
                        "INNER JOIN resource ON fk_resource_id=resource_id WHERE title=?";

    public boolean updateArticle(Article article) {
        ProxyConnection connection = null;
        PreparedStatement statement = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            statement = connection.prepareStatement(SQL_UPDATE_ARTICLE);
            int articleId = article.getArticleId();
            String body = article.getBody();
            statement.setString(1, body);
            statement.setInt(2, articleId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.error(FATAL, "", e);
            throw new RuntimeException();
        } finally {
            close(statement);
            close(connection);
        }
    }

    public boolean deleteArticle(Article article) {
        ProxyConnection connection = null;
        PreparedStatement statement = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            statement = connection.prepareStatement(SQL_DELETE_ARTICLE);
            int articleId = article.getArticleId();
            statement.setInt(1, articleId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.error(FATAL, "", e);
            throw new RuntimeException();
        } finally {
            close(statement);
            close(connection);
        }
    }

    public boolean addArticle(Article article) {
        ProxyConnection connection = null;
        PreparedStatement statement = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            statement = connection.prepareStatement(SQL_ADD_ARTICLE);

            String title = article.getTitle();
            String body = article.getBody();
            Date date = new Date();
            int fk_author_id = 1;
            int fk_resource_id = 1;

            statement.setString(1, title);
            statement.setString(2, body);
            statement.setDate(3, new java.sql.Date(date.getTime()));
            statement.setInt(4, fk_author_id);
            statement.setInt(5, fk_resource_id);

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.error(FATAL, "", e);
            throw new RuntimeException();
        } finally {
            close(statement);
            close(connection);
        }
    }

    public List<Article> takeArticleList() {
        List<Article> articleList = new ArrayList<Article>();

        ProxyConnection connection = null;
        Statement statement = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL_ARTICLES);
            while (resultSet.next()) {
                Article article = new Article();
                article.setTitle(resultSet.getString("title"));
                articleList.add(article);
            }
            return articleList;
        } catch (SQLException e) {
            LOGGER.error(FATAL, "", e);
            throw new RuntimeException();
        } finally {
            close(statement);
            close(connection);
        }
    }

    public Article takeArticle(String title) {
        ProxyConnection connection = null;
        PreparedStatement statement = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            statement = connection.prepareStatement(SQL_SELECT_ARTICLE_BY_TITLE);
            statement.setString(1, title);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            Article article = new Article();
            article.setArticleId(resultSet.getInt("article_id"));
            article.setTitle(resultSet.getString("title"));
            article.setBody(resultSet.getString("body"));
            Date date = resultSet.getDate("date");
            article.setDate(date);

            Author author = new Author();
            author.setAuthorId(resultSet.getInt("fk_author_id"));
            author.setName(resultSet.getString("a_name"));
            author.setSurname(resultSet.getString("surname"));
            author.setCountry(resultSet.getString("country"));
            article.setAuthor(author);

            Resource resource = new Resource();
            resource.setResourceId(resultSet.getInt("fk_resource_id"));
            resource.setName(resultSet.getString("r_name"));
            resource.setUrl(resultSet.getString("url"));
            article.setResource(resource);

            return article;
        } catch (SQLException e) {
            LOGGER.error(FATAL, "", e);
            throw new RuntimeException();
        } finally {
            close(statement);
            close(connection);
        }
    }

    private void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.error("", e);
            }
        }
    }
    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("", e);
            }
        }
    }
}
