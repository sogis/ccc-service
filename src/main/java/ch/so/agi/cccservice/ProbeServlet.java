package ch.so.agi.cccservice;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.so.agi.cccprobe.ProbeTool;

public class ProbeServlet extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(ProbeTool.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try {
            String contextPath=request.getContextPath();
            int port=request.getLocalPort();
            String endpoint="ws://localhost:"+port+contextPath+"/ccc-service";
            logger.info("endpoint <"+endpoint+">");
            int exitCode=new ProbeTool().mymain(new String[] {
                    endpoint
            });
            if(exitCode!=0) {
                // failed
                out.println("<p>ccc-service failed!</p>");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }else {
                out.println("<p>ccc-service ok!</p>");
            }
        } catch (Exception e) {
            // failed
            logger.error("probe tool failed",e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"ccc-service probe tool failed! "+e);
        }
    }

}
