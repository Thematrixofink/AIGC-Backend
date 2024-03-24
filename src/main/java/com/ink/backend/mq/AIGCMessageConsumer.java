package com.ink.backend.mq;


import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.StatusCode;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.model.entity.AIPersonInfo;
import com.ink.backend.model.entity.User;
import com.ink.backend.service.AIPersonInfoService;
import com.ink.backend.service.GenRequestService;
import com.ink.backend.service.MessageService;
import com.ink.backend.service.UserService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Slf4j
public class AIGCMessageConsumer {

    @Resource
    private AIPersonInfoService aIPersonInfoService;
    @Resource
    private UserService userService;
    @Resource
    private GenRequestService genRequestService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private MessageService messageService;


    /**
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {MQConstant.AIGC_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("消息消费者收到消息 :"+ message);
        if(StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息队列消息错误!");
        }
        String[] split = message.split("&");
        if(split.length < 4){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息队列消息错误!");
        }
        String aiPersonInfoId = split[0];
        String userId = split[1];
        String messageId = split[2];
        Integer aigcCount = Integer.valueOf(split[3]);
        AIPersonInfo aiPersonInfo = aIPersonInfoService.getById(aiPersonInfoId);
        User user = userService.getById(userId);
        String aiVoice = aiPersonInfo.getAiVoice();
        String aiPicture = aiPersonInfo.getAiPicture();
        if(aiPersonInfo == null || user == null){
            channel.basicNack(deliveryTag,false,false);
        }
        //更新任务状态为生成种
        aiPersonInfo.setStatus(StatusCode.GENING.getCode());
        boolean b = aIPersonInfoService.updateById(aiPersonInfo);
        if(!b){
            updateAiInfoToError(aiPersonInfo);
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        HttpResponse response = null;
        try{
            response = genRequestService.upload(Long.valueOf(aiPersonInfoId), aiVoice, aiPicture);
        }catch (Exception e){
            updateAiInfoToError(aiPersonInfo);
            channel.basicNack(deliveryTag,false,false);
            messageService.removeById(messageId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"因为网络原因，预处理失败，请重试");
        }

        log.info("==============================上传文件的响应为===============================");
        log.info(String.valueOf(response.getStatus()));

        if(response.getStatus() != 200){
            updateAiInfoToError(aiPersonInfo);
            channel.basicNack(deliveryTag,false,false);
            messageService.removeById(messageId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"预处理失败，请重试");
        }
        //拿到预处理的key
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        if(!jsonObject.isEmpty()) {
            String videoKey = (String) jsonObject.get("key");
            //创建AI数字人成功，对话开启,用户的使用次数减1
            aiPersonInfo.setExecMessage(videoKey);
        }
        aiPersonInfo.setStatus(StatusCode.SUCCESS.getCode());
        boolean b1 = aIPersonInfoService.updateById(aiPersonInfo);
        if(!b1){
            updateAiInfoToError(aiPersonInfo);
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新任务状态失败");
        }

        aigcCount--;
        //更新缓存,更新数据库
        redisTemplate.opsForValue().getAndSet("user:useTimes:"+userId,String.valueOf(aigcCount));
        user.setAigcCount(aigcCount);
        boolean update = userService.updateById(user);
        if(!update){
            updateAiInfoToError(aiPersonInfo);
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新任务状态失败");
        }
        //消息确认
        log.info(aiPersonInfo.getId()+"预处理成功！可以进行生成操作");
        channel.basicAck(deliveryTag,false);
    }


    private boolean updateAiInfoToError(AIPersonInfo aiPersonInfo){
        aiPersonInfo.setStatus(StatusCode.FAIL.getCode());
        boolean update = aIPersonInfoService.updateById(aiPersonInfo);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新任务状态失败");
        }
        return true;
    }
}
