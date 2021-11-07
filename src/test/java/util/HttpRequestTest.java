package util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;
import webserver.HttpRequest;
import webserver.Method;

public class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
        HttpRequest request = HttpRequest.Companion.from(in);

        assertEquals(Method.GET, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("javajigi", request.getParameters().get("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = HttpRequest.Companion.from(in);

        assertEquals(Method.POST, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("javajigi", request.getParameters().get("userId"));
    }
}
