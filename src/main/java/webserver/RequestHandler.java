package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
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
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	
        	String url = "";
        	String line;
        	do {
        		line = br.readLine();
        		if (line == null) {
        			return;
        		}
        		if (line.startsWith("GET")) {
        			String[] tokens = line.split(" ");
        			url = tokens[1];
        			if (url.startsWith("/user/create")) {
        				int index = url.indexOf("?");
        				String requestPath = url.substring(0, index);
        				String params = url.substring(index + 1);
        				
        				Map<String, String> query = HttpRequestUtils.parseQueryString(params);
        				
        				User user = new User(query.get("userId"), query.get("password"), query.get("name"), query.get("email"));
        				System.out.println(user.toString());
        			}
        		}
        		else if (line.startsWith("POST")) {
        			String postLine;
        			String contentLength = "";
        			do {
        				postLine = br.readLine();
        				if (postLine.startsWith("Content-Length")) {
        					contentLength = postLine.split(" ")[1];
        				}
        			} while (!"".equals(postLine));
        			
        			String params = util.IOUtils.readData(br, Integer.parseInt(contentLength));
        			System.out.println(params);
        			
        			Map<String, String> query = HttpRequestUtils.parseQueryString(params);
        			User user = new User(query.get("userId"), query.get("password"), query.get("name"), query.get("email"));
    				System.out.println(user.toString());
    		
    				byte[] body;
    				url = "/index.html";
    				body = Files.readAllBytes(new File("./webapp" + url).toPath());
    				
    	            DataOutputStream dos = new DataOutputStream(out);
    				response302Header(dos, body.length);
    				responseBody(dos, body);
    				return;
        		}
        	}
        	while (!"".equals(line));
        	
        	byte[] body;
            DataOutputStream dos = new DataOutputStream(out);
            if ("/".equals(url)) {
            	body = "Hello World".getBytes();
            } else {
            	body = Files.readAllBytes(new File("./webapp" + url).toPath());
            }
            
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location: /index.html\r\n");
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
