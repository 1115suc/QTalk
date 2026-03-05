package course.QTalk.handler;

import cn.hutool.json.JSONUtil;
import course.QTalk.constant.RedisConstant;
import course.QTalk.pojo.dto.MessageSendDto;
import course.QTalk.websocket.ChannelContextUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTopicHandler {

    private final RedissonClient redissonClient;
    private final ChannelContextUtils channelContextUtils;

    @PostConstruct
    public void listenMessage() {
        RTopic rTopic = redissonClient.getTopic(RedisConstant.REDISSION_MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto, sendDto) -> {
            log.info("收到广播消息:{}", JSONUtil.toJsonPrettyStr(sendDto));
            channelContextUtils.sendMessage(sendDto);
        });
    }

    public void sendMessage(MessageSendDto sendDto) {
        RTopic rTopic = redissonClient.getTopic(RedisConstant.REDISSION_MESSAGE_TOPIC);
        rTopic.publish(sendDto);
    }
}
