package com.dfocus.qrcode.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.dfocus.common.base.JSONResult;
import com.dfocus.common.util.DateUtils;
import com.dfocus.qrcode.base.QRcodeEnum;
import com.dfocus.qrcode.base.QRcodeResult;
import com.dfocus.qrcode.mq.Recv;
import com.dfocus.qrcode.mq.Send;
import com.dfocus.qrcode.vo.Info;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User: qfwang
 * Date: 2017-09-26
 * Time: 下午5:55
 */
@RestController
public class TestController {

    private final Logger logger = LoggerFactory.getLogger(TestController.class);
    @Autowired
    private Send sender;

    /**
     * 扫码登录
     * @param info
     * @param request
     * @return
     */
    @ApiOperation(value = "扫码登录")
    @ApiImplicitParam(name = "info",value = "json格式的客户端信息",required = true,dataType = "String",paramType = "path")
    @ApiResponse(code = 200, message = "成功",response = JSONResult.class)
    @GetMapping("user/login/{info}")
    public JSONResult obj(@PathVariable String info, HttpServletRequest request){

        Info client = null;
        try{
            client =JSON.parseObject(info, Info.class);
        }catch (JSONException e){
            logger.error(e.toString());
            return QRcodeResult.error(QRcodeEnum.PL_UPDATE_QR);
        }

        System.out.println(client.toString());
        String token = request.getHeader("Authorization");
        if (token == null || token.equals("")) {
            return QRcodeResult.error(QRcodeEnum.INVALID_TOKEN);
        }

        if(DateUtils.isExpired(client.getCreateTime(),60)){
            return QRcodeResult.error(QRcodeEnum.EXPIRED);
        }
        try {
/*            Map<String, Claim> map= JwtUtils.verifyToken(token);
            if (map == null) {
                return QRcodeResult.error(QRcodeEnum.INVALID_TOKEN);
            }
            String username= map.get("username").asString();*/
            boolean flag=sender.isExistsClient(client.getClientId());
            if(flag){
                sender.sendMsg(client.getClientId(),token);
            }else {
                return QRcodeResult.error(QRcodeEnum.PL_UPDATE_QR);
            }
        }catch (Exception e){
            System.out.println(e.toString());
        }
        return JSONResult.ok();
    }

    @GetMapping("send")
    public String send() throws Exception{
/*        new Queue("hello", false);
        sender.send("hello");*/
        sender.sendMsg("hello","测试");
        return "send";
    }
    @GetMapping("recv")
    public String recv() throws Exception{
        Recv.recvMsg();
        return "send";
    }

    @GetMapping("uuid")
    public String createUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
    @GetMapping("json")
    public JSONResult json(){
        Map<String,Object> map =new HashMap<>();
        map.put("uuid",UUID.randomUUID().toString().replace("-",""));
        map.put("isCreated",true);
        List<String> list = new ArrayList<>();
        list.add("wang");
        list.add("qing");
        list.add("fei");
        return JSONResult.ok(list);
    }
    /**
     * 根据UUID创建消息队列，并返回UUID
     * @return
     */
    @GetMapping("qrcode/uuid")
    public JSONResult qrcodeUUID(){
        logger.info("=============执行获取uuid============");
        Map<String,Object> map =new HashMap<>();
        String uuid = "";
        try{
            uuid = createUUID();
            sender.createQueue(uuid);
        }catch (Exception e){
            System.out.println(e.toString());
        }
        map.put("uuid",uuid);
        map.put("type","login");
        return JSONResult.ok(map);
    }
}
