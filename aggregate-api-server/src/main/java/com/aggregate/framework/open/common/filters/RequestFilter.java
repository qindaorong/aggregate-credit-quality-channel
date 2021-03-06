package com.aggregate.framework.open.common.filters;

import com.aggregate.framework.open.common.components.RedisHandler;
import com.aggregate.framework.open.common.constants.ClientConstant;
import com.aggregate.framework.open.common.constants.SecurityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Order(9)
@WebFilter(filterName="RequestFilter",urlPatterns={"/open/api/*"})
@Slf4j
public class RequestFilter implements Filter {

    private final static String SECURITY_MSG = "the security code is incorrect";

    @Override
    public void init(FilterConfig filterConfig){
        log.info("RequestFilter init......");
    }


    @Autowired
    RedisHandler redisHandler;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Map<String,String> requestSecurityValue = this.getHeaderMap(request);
        if(!checkHeaderSecurityKey(requestSecurityValue)){
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,SECURITY_MSG);
            return ;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        log.info("RequestFilter destroy.....");
    }

    /**
     *
     * @param headerMap
     * @return
     */
    private Boolean checkHeaderSecurityKey(Map<String,String> headerMap){
        String clientId = headerMap.get(SecurityConstant.REQUEST_SECURITY_CLIENT_ID);
        String clientSecret = headerMap.get(SecurityConstant.REQUEST_SECURITY_CLIENT_SECRET);

        //redis 中保存的数据decodeSecret
        String decodeSecret  = String.valueOf(redisHandler.getHashKey(clientId, ClientConstant.CLIENT_SECRET));

        //缓存中存在上游client_id，加密后client_secret值相同
        if(redisHandler.hashKey(clientId, ClientConstant.CLIENT_ID) && StringUtils.equals(decodeSecret,clientSecret)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 获得用户验证信息
     * @param request
     * @return
     */
    private Map<String,String> getHeaderMap(HttpServletRequest request){

        Map<String,String> headerMap = new HashMap<>(2);
        String clientId = request.getHeader(SecurityConstant.REQUEST_SECURITY_CLIENT_ID);
        String clientSecret = request.getHeader(SecurityConstant.REQUEST_SECURITY_CLIENT_SECRET);

        headerMap.put(SecurityConstant.REQUEST_SECURITY_CLIENT_ID,clientId);
        headerMap.put(SecurityConstant.REQUEST_SECURITY_CLIENT_SECRET,clientSecret);
        return headerMap;
    }
}
