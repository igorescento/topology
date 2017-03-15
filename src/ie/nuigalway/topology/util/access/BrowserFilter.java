package ie.nuigalway.topology.util.access;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BrowserFilter implements Filter {

	public BrowserFilter() { }
	
	@Override
	public void destroy() { }
	
	private boolean isUnsupportedBrowser(HttpServletRequest request){
		String userAgent = request.getHeader("User-Agent");
		if(userAgent != null && (userAgent.toLowerCase().indexOf("msie") != -1 || userAgent.toLowerCase().indexOf("trident") != -1)){
			return true;
		}
		
		return false;
	}
	
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            
            /*httpResponse.setHeader("Cache-Control","no-cache");
            httpResponse.setHeader("Pragma","no-cache");
            httpResponse.setDateHeader ("Expires", 0);
            
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
            httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with");*/
               
            if (isUnsupportedBrowser(httpRequest)) {
                request.getRequestDispatcher("unsupported-browser.jsp").forward(request, response);
            } else {
                chain.doFilter(request, response);
            }
            return;
        }
    
        throw new ServletException("Unauthorized access");
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }
}
