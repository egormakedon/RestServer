package com.epam.makedon.jersey.server;

import com.epam.makedon.jersey.server.dao.Dao;
import com.epam.makedon.jersey.server.pojo.Article;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/service")
public class Service {

    @GET
    @Path("/takeArticleList")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Article> takeArticleList() {
        return Dao.getInstance().takeArticleList();
    }

    @GET
    @Path("/takeArticle/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    public Article takeArticle(@PathParam("title") String title) {
        return Dao.getInstance().takeArticle(title);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") int id) {
        Article article = new Article();
        article.setArticleId(id);
        Dao.getInstance().deleteArticle(article);
    }

    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    public void add(Article article) {
        Dao.getInstance().addArticle(article);
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(Article article) {
        Dao.getInstance().updateArticle(article);
    }
}