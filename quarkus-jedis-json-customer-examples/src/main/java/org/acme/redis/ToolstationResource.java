package org.acme.redis;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import redis.clients.jedis.search.SearchResult;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path("/student")
public class ToolstationResource {

//    @Inject
//    StudentService jsonService;
//
//    @GET
//    @Path("/ping")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String hello() {
//        return "Pong";
//    }
//
//    @POST
//    @Path("/new")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String createNewStudent(Student student) {
//        return jsonService.insertStudent(student);
//    }
//
//    @GET
//    @Path("/id/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Student getStudent(@RestPath int id) {
//        return jsonService.getStudent("student:" + id);
//    }
//
//    @GET
//    @Path("/example/maya")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Student maya() {
//        return jsonService.getStudent("student:1");
//    }
//
//    @GET
//    @Path("/example/oliwia")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Student oliwia() {
//        return jsonService.getStudent("student:2");
//    }
//
//    /**
//     *
//     * @param name
//     * @param page
//     * @param offset
//     * @return
//     */
//    @GET
//    @Path("/search/name/{name}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public SearchResult searchStudentByName(@RestPath String name,
//                                            @RestQuery Integer page,
//                                            @RestQuery Integer offset) throws Exception {
//        return jsonService.searchByName(name, page, offset);
//    }
//
//    /**
//     * curl GET 'http://localhost:8080/student/search/generic/Paris?offset=1&page=1'
//     * @param query
//     * @param page
//     * @param offset
//     * @return
//     */
//    @GET
//    @Path("/search/generic/{query}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public SearchResult searchStudentByString(@RestPath String query,
//                                              @RestQuery Integer page,
//                                              @RestQuery Integer offset) {
//        return jsonService.genericSearch(query, page, offset);
//    }
//
//    /**
//     * curl GET 'http://localhost:8080/student/search/city/Paris?offset=1&page=1'
//     * @param city
//     * @param page
//     * @param offset
//     * @return
//     */
//    @GET
//    @Path("/search/city/{city}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public SearchResult searchStudentByCity(@RestPath String city,
//                                            @RestQuery Integer page,
//                                            @RestQuery Integer offset) throws Exception {
//        return jsonService.searchByCity(city, page, offset);
//    }

}