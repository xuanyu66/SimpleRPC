import com.yangxin.simplerpc.service.HelloService;
import com.yangxin.simplerpc.rpcclient.RpcProxy;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * @author leon on 2018/9/28.
 * @version 1.0
 * @Description:
 */


public class HelloTest {

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/log4j.properties");
        GenericXmlApplicationContext ac = new GenericXmlApplicationContext();
        ac.setValidating(false);
        ac.load("classpath:springclient.xml");
        ac.refresh();
        RpcProxy rpcProxy = (RpcProxy) ac.getBean("rpcProxy");
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("world");
        System.out.println("*******************" + result);
        String result2 = helloService.hello("mom");
        System.out.println("*******************" + result2);
        String result1 = helloService.hello("bob");
        System.out.println("*******************" + result1);
        System.out.println("down");
        System.out.println(rpcProxy.client.getChannel().remoteAddress());
        rpcProxy.client.close();

    }

}
