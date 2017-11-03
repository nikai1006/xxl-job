package com.xxl.job.core.rpc.netcom;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.server.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * netcom init
 *
 * @author xuxueli 2015-10-31 22:54:27
 */
public class NetComServerFactory {

    private static final Logger logger = LoggerFactory.getLogger(NetComServerFactory.class);

    // ---------------------- server start ----------------------
    JettyServer server = new JettyServer();

    /**
     * <pre>
     *
     * <b>该方法处理两件事情：</b>
     * <ol>
     *     <li>启动jetty服务器来监听调度中心的调度请求</li>
     *     <li>去调度中心进行执行器注册</li>
     * </ol>
     * </pre>
     *
     * @version 1.0 2017/11/2 16:04
     * @see com.xxl.job.core.executor.XxlJobExecutor#initExecutorServer(int, String, String, String)
     */
    public void start(int port, String ip, String appName) throws Exception {
        server.start(port, ip, appName);
    }

    // ---------------------- server destroy ----------------------
    public void destroy() {
        server.destroy();
    }

    // ---------------------- server instance ----------------------
    /**
     * init local rpc service map
     */
    private static Map<String, Object> serviceMap = new HashMap<String, Object>();
    private static String accessToken;

    /**
     * 添加执行器实例
     *
     * @param iface
     * @param serviceBean
     */
    public static void putService(Class<?> iface, Object serviceBean) {
        serviceMap.put(iface.getName(), serviceBean);
    }

    /**
     * 设置访问Token
     *
     * @param accessToken
     */
    public static void setAccessToken(String accessToken) {
        NetComServerFactory.accessToken = accessToken;
    }

    /**
     * 通过请求参数来解析调用相应的执行器实例
     *
     * @version 1.0 2017/11/2 9:25
     * @see com.xxl.job.core.biz.ExecutorBiz
     */
    public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
        if (serviceBean == null) {
            String className = request.getClassName();//代理实例名 com.xxl.job.core.biz.ExecutorBiz

            serviceBean = serviceMap.get(className);//获取相应的实例
        }
        if (serviceBean == null) {
            // TODO
        }

        RpcResponse response = new RpcResponse();

        if (System.currentTimeMillis() - request.getCreateMillisTime() > 180000) {
            response.setResult(new ReturnT<String>(ReturnT.FAIL_CODE,
                "The timestamp difference between admin and executor exceeds the limit."));
            return response;
        }
        if (accessToken != null && accessToken.trim().length() > 0 && !accessToken.trim()
            .equals(request.getAccessToken())) {
            response.setResult(
                new ReturnT<String>(ReturnT.FAIL_CODE, "The access token[" + request.getAccessToken() + "] is wrong."));
            return response;
        }

        try {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();

            FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

            Object result = serviceFastMethod.invoke(serviceBean, parameters);

            response.setResult(result);
        } catch (Throwable t) {
            t.printStackTrace();
            response.setError(t.getMessage());
        }

        return response;
    }

}
