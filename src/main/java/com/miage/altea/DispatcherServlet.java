package com.miage.altea;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.altea.bo.PokemonType;
import com.miage.altea.controller.HelloController;
import com.miage.altea.controller.PokemonTypeController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/*", loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {

    private Map<String, Method> getUriMappings = new HashMap<>();
    private Map<String, Method> postUriMappings = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Getting request for " + req.getRequestURI());
        Method method = getUriMappings.get(req.getRequestURI());
        if(method == null){
            resp.sendError(404,"no mapping found for request uri "+req.getRequestURI());
            return;
        }
        Object retrunValue = null;
        try {
            if(method.getParameterCount()> 0){
                retrunValue = method.invoke(method.getDeclaringClass().newInstance(),req.getParameterMap());
            }else{
                retrunValue = method.invoke(method.getDeclaringClass().newInstance());
            }
        } catch (Exception e) {
            resp.sendError(500,"exception when calling method "+method.getName()+" : "+e.getCause().getMessage());
            return;
        }
        PrintWriter writer = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        writer.print(mapper.writeValueAsString(retrunValue));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Post request for " + req.getRequestURI());
        Method method = postUriMappings.get(req.getRequestURI());
        if(method == null){
            resp.sendError(404,"no mapping found for request uri "+req.getRequestURI());
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            method.invoke(method.getDeclaringClass().newInstance(),mapper.readValue(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())), method.getParameters()[0].getType()));
        } catch (Exception e) {
            resp.sendError(500,"exception when calling method "+method.getName()+" : "+e.getCause().getMessage());
            return;
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // on enregistre notre controller au démarrage de la servlet
        this.registerController(HelloController.class);
        this.registerController(PokemonTypeController.class);
    }

    protected void registerController(Class controllerClass){
        System.out.println("Analysing class " + controllerClass.getName());
        Annotation annotationController = controllerClass.getAnnotation(Controller.class);
        if(annotationController != null){
            Method[] methods = controllerClass.getDeclaredMethods();
            for(Method method:methods){
                if(method.getAnnotation(RequestMapping.class) != null && !method.getReturnType().getName().equals("void")){
                    registerMethod(method);
                }else if(method.getAnnotation(PostMapping.class) != null){
                    registerPostMethod(method);
                }
            }
        }else{
            throw new IllegalArgumentException();
        }
    }

    protected void registerMethod(Method method) {
        System.out.println("Registering method " + method.getName());
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        getUriMappings.put(requestMapping.uri(),method);
    }

    protected void registerPostMethod(Method method) {
        System.out.println("Registering method " + method.getName());
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        postUriMappings.put(postMapping.uri(),method);
    }

    protected Map<String, Method> getMappings(){
        return this.getUriMappings;
    }

    protected Method getMappingForUri(String uri){
        return this.getUriMappings.get(uri);
    }
}