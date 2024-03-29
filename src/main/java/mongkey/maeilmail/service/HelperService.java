package mongkey.maeilmail.service;

import mongkey.maeilmail.config.ChatGPTConfig;
import mongkey.maeilmail.dto.helper.request.HelperRequestContentDto;
import mongkey.maeilmail.dto.helper.request.HelperRequestDto;
import mongkey.maeilmail.dto.helper.request.HelperToGptRequestDto;
import mongkey.maeilmail.dto.helper.response.HelperResponseDto;
import mongkey.maeilmail.dto.helper.response.HelperRetryResponseDto;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChatGPTService 구현체
 *
 */
@Slf4j
@Service
public class HelperService implements ChatGPTService {
    private final ChatGPTConfig chatGPTConfig;

    public HelperService(ChatGPTConfig chatGPTConfig) {
        this.chatGPTConfig = chatGPTConfig;
    }

    @Value("${openai.url.model-v1}")
    private String create_model;

    @Value("${openai.url.model-v2}")
    private String retry_model;

    @Value("${openai.url.prompt}")
    private String promptUrl;

    @Value("${instruction.basic}")
    private String basic;

    @Value("${instruction.title}")
    private String title;

    @Value("${instruction.greeting}")
    private String greeting;

    @Value("${instruction.body}")
    private String body;

    @Value("${instruction.closing}")
    private String closing;

    /**
     * ChatGTP 이메일 생성
     */
    @Override
    public HelperResponseDto createEmail(HelperRequestDto helperRequestDto) {
        log.debug("[+] 프롬프트를 수행합니다.");

        Map<String, Object> resultMap = new HashMap<>();

        // [STEP1] 토큰 정보가 포함된 Header를 가져옵니다.
        HttpHeaders headers = chatGPTConfig.httpHeaders();

        // HelperRequestContentDto 객체 생성
        HelperToGptRequestDto helperToGptRequestDto = setGptRequestDto( create_model,
                "    You are Korean Mail text generator AI for college students based on input information ‘sender, sender_info, receiver, receiver_info, purpose’\n" +
                        "    - You should divide it into (title), (greeting), (body), and (closing). You NEVER missing out any of these\n" +
                        "    - Your text should always be very polite.\n" +
                        "    - NEVER put in the fact of a user YOU DON'T KNOW. If you need to include specific information other than the input information, you have to write it in the form of [  additional information  ].\n" +
                        "\n" +
                        "    Each part(title, greeting, body, closing) has the main contents to deliver and should not be duplicated for each part.\n" +
                        "    - title: The title must be concise and clear and include the purpose of the email \n" +
                        "    The title begins with [email purpose]. It is \"purpose\" of the input.\n" +
                        "    - greeting: greeting must include the sender's self-introduction, including student number and name.\n" +
                        "    - body: You create the text according to the purpose of the email. Yopu should write as politely as possible and put the reciever's intention first. You Don't need self introduction \n" +
                        "    - closing:  You should include a thank you at the end. And You must match the format of the sender's name 드림 or sender's name 올림.\n" , helperRequestDto);
        log.debug("gpt 전송용 객체 생성 성공");

        // [STEP5] 통신을 위한 RestTemplate을 구성합니다.
        HttpEntity<HelperToGptRequestDto> requestEntity = new HttpEntity<>(helperToGptRequestDto, headers);
        ResponseEntity<String> response = chatGPTConfig
                .restTemplate()
                .exchange(promptUrl, HttpMethod.POST, requestEntity, String.class);
        log.debug("gpt 통신 성공");

        try {
            // [STEP6] String -> HashMap 역직렬화를 구성합니다.
            ObjectMapper om = new ObjectMapper();
            resultMap = om.readValue(response.getBody(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }

        //Get Whole Email
        String fullContent = getFullEmail(resultMap);
        log.debug("이메일 작성 성공: "+ fullContent);

        // Separate part
        String processedContent = fullContent.replace("{", "").replace("}", "");
        log.debug("전처리 완료된 본문: "+ processedContent);
        String[] sectionStarts = {"(title)", "(greeting)", "(body)", "(closing)"};


        return HelperResponseDto.builder()
                .user_id(1L)
                .subject(extractSection(processedContent, sectionStarts[0], sectionStarts[1]))
                .greeting(extractSection(processedContent, sectionStarts[1], sectionStarts[2]))
                .body(extractSection(processedContent, sectionStarts[2], sectionStarts[3]))
                .closing(extractSection(processedContent, sectionStarts[3], null))
                .build();
    }


    @Override
    public HelperRetryResponseDto retryEmail(String contentPart, HelperRequestDto helperRequestDto) {
        log.debug("[+] 프롬프트를 수행합니다.");

        Map<String, Object> resultMap = new HashMap<>();

        // [STEP1] 토큰 정보가 포함된 Header를 가져옵니다.
        HttpHeaders headers = chatGPTConfig.httpHeaders();


        // HelperRequestContentDto 객체 생성
        String instruction = getInstruction(contentPart);
        log.info("instruction->{} ", instruction);
        HelperToGptRequestDto helperToGptRequestDto = setRecreateGptRequestDto(contentPart, retry_model,
                instruction,
                helperRequestDto);
        log.debug("gpt 전송용 객체 생성 성공");

        // [STEP5] 통신을 위한 RestTemplate을 구성합니다.
        HttpEntity<HelperToGptRequestDto> requestEntity = new HttpEntity<>(helperToGptRequestDto, headers);
        ResponseEntity<String> response = chatGPTConfig
                .restTemplate()
                .exchange(promptUrl, HttpMethod.POST, requestEntity, String.class);
        log.debug("gpt 통신 성공");

        try {
            // [STEP6] String -> HashMap 역직렬화를 구성합니다.
            ObjectMapper om = new ObjectMapper();
            resultMap = om.readValue(response.getBody(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }

        //Get Whole Email
        String fullContent = getFullEmail(resultMap);
        log.debug("이메일 작성 성공: "+ fullContent);

        // Separate part
        String processedContent = fullContent.replace("{", "").replace("}", "");
        log.info("전처리 완료된 본문 ->{} ", processedContent);
        String[] sectionStarts = {"(version1)", "(version2)", "(version3)"};


        return HelperRetryResponseDto.builder()
                .user_id(1L)
                .version1(extractSection(processedContent, sectionStarts[0], sectionStarts[1]))
                .version2(extractSection(processedContent, sectionStarts[1], sectionStarts[2]))
                .version3(extractSection(processedContent, sectionStarts[2], null))
                .build();
    }

    private String getInstruction(String contentPart){
        String instruction = basic;
        String title1 = "title";
        if(contentPart.equals(title1)) {
            instruction = basic + title;
        }
        if(contentPart.equals("greeting"))
            instruction = basic+ greeting;
        if(contentPart.equals("body"))
            instruction = basic + body;
        if(contentPart.equals("closing"))
            instruction = basic+closing;
        System.out.println(instruction);
        return instruction;
    }

    private HelperToGptRequestDto setGptRequestDto(String model, String content, HelperRequestDto helperRequestDto){
        HelperRequestContentDto systemMessage = HelperRequestContentDto.builder()
                .role("system")
                .content(content)
                .build();

        HelperRequestContentDto userMessage = HelperRequestContentDto.builder()
                .role("user")
                .content(String.format("sender: %s\nsender_info: %s\nreceiver: %s\nreceiver_info: %s\npurpose: %s",
                        helperRequestDto.getSender(),
                        helperRequestDto.getSender_info(),
                        helperRequestDto.getReceiver(),
                        helperRequestDto.getReceiver_info(),
                        helperRequestDto.getPurpose()))
                .build();

        // HelperToGptRequestDto 객체 생성
        return HelperToGptRequestDto.builder()
                .model(model)
                .messages(Arrays.asList(systemMessage, userMessage))
                .build();
    }

    private HelperToGptRequestDto setRecreateGptRequestDto(String contentPart, String model, String content, HelperRequestDto helperRequestDto){
        HelperRequestContentDto systemMessage = HelperRequestContentDto.builder()
                .role("system")
                .content(content)
                .build();

        HelperRequestContentDto userMessage = HelperRequestContentDto.builder()
                .role("user")
                .content(String.format("email_part: %s\nsender: %s\nsender_info: %s\nreceiver: %s\nreceiver_info: %s\npurpose: %s",
                        contentPart,
                        helperRequestDto.getSender(),
                        helperRequestDto.getSender_info(),
                        helperRequestDto.getReceiver(),
                        helperRequestDto.getReceiver_info(),
                        helperRequestDto.getPurpose()))
                .build();

        // HelperToGptRequestDto 객체 생성
        return HelperToGptRequestDto.builder()
                .model(model)
                .messages(Arrays.asList(systemMessage, userMessage))
                .build();
    }

    private String getFullEmail(Map<String, Object> resultMap){
        List<Map<String, Object>> choices = (List<Map<String, Object>>) resultMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return  (String) message.get("content");
    }

    private String extractSection(String email, String start, String end) {
        int startIndex = email.indexOf(start) + start.length();
        int endIndex = (end != null) ? email.indexOf(end) : email.length();

        // 시작 문자열이 존재하지 않는 경우
        if (startIndex == -1 ) {
            // 에러 로그 출력 또는 예외 처리
            log.debug("시작 섹션 '" + start + "'을(를) 찾을 수 없습니다.");
            return "해당 문단을 생성하는 데 실패했어요:( 다시 생성해 주세요"; // 또는 적절한 기본값 반환
        }

        // 끝 문자열이 존재하지 않는 경우 (end가 null이 아닌데도 endIndex가 -1인 경우)
        if (end != null && endIndex == -1) {
            // 에러 로그 출력 또는 예외 처리
            log.debug("끝 섹션 '" + end + "'을(를) 찾을 수 없습니다.");
            return "해당 문단을 생성하는 데 실패했어요:( 다시 생성해 주세요";
        }
        return email.substring(startIndex, endIndex).trim();
    }
}