package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            Map<String, String> responseParams = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            String contentType = "text/html;charset=utf-8";

            String line;
            while(!(line = reader.readLine()).isEmpty()) {
                if (line == null) {
                    return;
                }
                builder.append(line).append("\n");
            }

            List<String> requestLines = Arrays.asList(builder.toString().split("\n"));
            String requestUrl = requestLines.get(0).split(" ")[1];

            if (requestUrl.equals("/")) {
                requestUrl = "/index.html";
            }

            System.out.println("request URL: " + requestUrl);

            if (requestUrl.equals("/user/create")) {
                int contentLength = Integer.parseInt(requestLines.get(3).split(": ")[1]);
                String content = IOUtils.readData(reader, contentLength);
                processSignUp(content);
                response302Header(dos);
                return;
            }

            if(requestUrl.equals("/user/login")) {
                int contentLength = Integer.parseInt(requestLines.get(3).split(": ")[1]);
                String content = IOUtils.readData(reader, contentLength);
                boolean status = processLogin(content);

                if (status) {
                    requestUrl = "/index.html";
                    responseParams.put("Set-Cookie", "logined=true");
                } else {
                    responseParams.put("Set-Cookie", "logined=false");
                    requestUrl = "/user/login_failed.html";
                }
            }


            if (requestUrl.equals("/user/list")) {
                String cookieString = requestLines.get(requestLines.size() - 1).split(": ")[1];
                boolean logined = isLogined(cookieString);

                if (logined) {
                    StringBuilder bodyBuilder = new StringBuilder();
                    Collection<User> users = DataBase.findAll();
                    for (User user : users) {
                        bodyBuilder.append(String.format("<p>%s</p>", user.getName()));
                    }
                    byte[] body = bodyBuilder.toString().getBytes();
                    response200Header(dos, body.length, responseParams, contentType);
                    responseBody(dos, body);
                    return;
                } else {
                    requestUrl = "/user/login.html";
                }
            }

            byte[] body = Files.readAllBytes(new File("./webapp" + requestUrl).toPath());

            if (requestUrl.contains("css")) {
                contentType = "text/css;charset=utf-8";
            }

            response200Header(dos, body.length, responseParams, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processSignUp(String content) {
        Map<String, String> map = HttpRequestUtils.parseQueryString(content);

        User user = new User(
                map.get("userId"),
                map.get("password"),
                map.get("name"),
                map.get("email")
        );

        DataBase.addUser(user);
    }

    private boolean processLogin(String content) {
        Map<String, String> stringStringMap = HttpRequestUtils.parseQueryString(content);

        String userId = stringStringMap.get("userId");
        String password = stringStringMap.get("password");

        User userById = DataBase.findUserById(userId);

        return userById != null && userById.getPassword().equals(password);
    }

    private boolean isLogined(String cookieString) {
        Map<String, String> cookiesMap = HttpRequestUtils.parseCookies(cookieString);

        return cookiesMap.containsKey("logined") && cookiesMap.get("logined").equals("true");
    }

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location:  http://localhost:8080/index.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, Map<String, String> responseParams,
                                   String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType +"\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");

            responseParams.forEach((key, value) ->
            {
                try {
                    dos.writeBytes(key + ": " + value + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
