package kr.kickon.api.global.util.slack;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.InternalServerException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

@Component
@NoArgsConstructor
public class SlackService {
    @Value("${spring.config.activate.on-profile}")
    private String env;
    Slack slack = Slack.getInstance();
    @Value("${api.slack-key}")
    private String token;

    public void sendErrorMessage(String text, String log) {
        try {

            String channelAddress;

            if(env.equals("dev")){
                channelAddress = "C08DTASMF6Y";
            }else if (env.equals("prod")){
                channelAddress = "C08E3F72GG1";
            } else {
                return;
            }
            List<LayoutBlock> layoutBlocks = new ArrayList<>();
            layoutBlocks.add(section(section -> section.text(markdownText("*🚨 [" + env + "] 에러가 발생했습니다!" + "🚨*" ))));
            layoutBlocks.add(divider());
            layoutBlocks.add(section(section -> section.text(markdownText("Error message : " + text))));
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelAddress)
                    .blocks(layoutBlocks)
                    .build();
            MethodsClient slackClient = slack.methods(token);
            ChatPostMessageResponse chatPostMessageResponse = slackClient.chatPostMessage(request);
            request = ChatPostMessageRequest.builder()
                    .channel(channelAddress)
                    .threadTs(chatPostMessageResponse.getTs())
                    .text(log)
                    .build();
            slackClient.chatPostMessage(request);
        }catch (Exception e) {
            throw new InternalServerException(ResponseCode.SLACK_SERVER_ERROR);
        }
    }

    public void sendLogMessage(String text) {
        try {
            String channelAddress;
            if(env.equals("dev")){
                channelAddress = "C08DTASMF6Y";
            }else if (env.equals("prod")){
                channelAddress = "C08E3F72GG1";
            } else {
                return;
            }
            List<LayoutBlock> layoutBlocks = new ArrayList<>();
            layoutBlocks.add(section(section -> section.text(markdownText("*✅ [" + env + "] "+ "로그 알림!" + "✅*" ))));
            layoutBlocks.add(divider());
            layoutBlocks.add(section(section -> section.text(markdownText("message : " + text))));
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelAddress)
                    .blocks(layoutBlocks)
                    .build();
            MethodsClient slackClient = slack.methods(token);
            slackClient.chatPostMessage(request);
        }catch (Exception e) {
            throw new InternalServerException(ResponseCode.SLACK_SERVER_ERROR, e.getMessage());
        }
    }
}
